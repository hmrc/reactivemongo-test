/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.mongo

import org.scalatest.{BeforeAndAfterAll, Failed, Outcome, TestSuite, TestSuiteMixin}
import org.scalatest.exceptions.TestFailedException
import org.scalatest.concurrent.ScalaFutures
import reactivemongo.api.commands.Command
import reactivemongo.api.{BSONSerializationPack, DefaultDB, FailoverStrategy, ReadPreference}
import reactivemongo.bson.BSONDocument
import reactivemongo.core.errors.{DatabaseException, ReactiveMongoException}

trait FailOnUnindexedQueries extends BeforeAndAfterAll with ScalaFutures with TestSuiteMixin {
  this: TestSuite =>

  def mongo: () => DefaultDB
  protected def databaseName: String

  import scala.concurrent.ExecutionContext.Implicits.global

  private val commandRunner = Command.run(BSONSerializationPack, FailoverStrategy())

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    mongo().connection.database(databaseName).map(_.drop()).futureValue
    commandRunner(
      db      = mongo().connection.database("admin").futureValue,
      command = commandRunner.rawCommand(BSONDocument("setParameter" -> 1, "notablescan" -> 1))
    ).one[BSONDocument](ReadPreference.primaryPreferred).futureValue
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    commandRunner(
      db      = mongo().connection.database("admin").futureValue,
      command = commandRunner.rawCommand(BSONDocument("setParameter" -> 1, "notablescan" -> 0))
    ).one[BSONDocument](ReadPreference.primaryPreferred).futureValue
  }

  abstract override def withFixture(test: NoArgTest): Outcome =
    super.withFixture(test) match {
      case Failed(NotableScanError(e)) => Failed("Mongo query could not be satisfied by an index:\n" + e.getMessage, e)
      case other                       => other
    }

  private[mongo] object NotableScanError {
    def unapply(e: Throwable): Option[ReactiveMongoException] =
      e match {
        // withFixture(Failed) catches the TestFailedException.
        case e: TestFailedException => Option(e.getCause).flatMap(NotableScanError.unapply)
        case e: DatabaseException if e.code == Some(291) => Some(e)
        // pre Mongo 4.4, was a generic exception - can be identified by message
        case e: ReactiveMongoException if e.getMessage.contains("No query solutions") => Some(e)
        case _ => None
      }
  }
}
