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

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import reactivemongo.api.commands.Command
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.api.{BSONSerializationPack, FailoverStrategy, ReadPreference}
import reactivemongo.bson.{BSONBoolean, BSONDocument, BSONValue}

class FailOnUnindexedQueriesSpec
  extends AnyWordSpec
     with Matchers
     with FailOnUnindexedQueries
     with MongoSpecSupport
     with ScalaFutures
     with IntegrationPatience {

  import scala.concurrent.ExecutionContext.Implicits.global

  "FailOnUnindexedQueries" should {
    "cause an exception be thrown when a query on unindexed property is performed" in {
      testCollection.insert(ordered = false).one(BSONDocument("unidexed" -> "value")).futureValue

      // we shouldn't get a match error on extracting the result
      val NotableScanError(_) = testCollection.find(BSONDocument("unidexed" -> "value"), projection = None).one.failed.futureValue
    }

    "cause no exception be thrown when a query on indexed property is performed" in {
      testCollection.indexesManager.create(Index(Seq("indexed" -> IndexType.Ascending))).futureValue

      testCollection.insert(ordered = false).one(BSONDocument("indexed" -> "value")).futureValue

      testCollection.find(BSONDocument("indexed" -> "value"), projection = None).one.futureValue should not be empty
    }
  }

  "beforeAll" should {
    "set parameter 'notablescan' on 'admin' database" in new ReadNotableScanValue {
      beforeAll()
      maybeNotableScanValue shouldBe Some(true)
    }
  }

  "afterAll" should {
    "unset parameter 'notablescan' on 'admin' database" in new ReadNotableScanValue {
      beforeAll()
      afterAll()
      maybeNotableScanValue shouldBe Some(false)
    }
  }

  private trait ReadNotableScanValue {

    private val runner = Command.run(BSONSerializationPack, FailoverStrategy())

    def maybeNotableScanValue: Option[Boolean] =
      (for {
         adminDb <- mongo().connection.database("admin")
         maybeNotableScanValue <- runner(
                                   adminDb,
                                   runner.rawCommand(BSONDocument("getParameter" -> 1, "notablescan" -> 1)))
                                   .one[BSONDocument](ReadPreference.primaryPreferred)
                                   .map(_.get("notablescan"))
                                   .map(_.map(toBoolean))
       } yield maybeNotableScanValue
      ).futureValue

    private val toBoolean: BSONValue => Boolean = {
      case BSONBoolean(boolean) => boolean
      case other                => throw new IllegalStateException(s"Boolean expected but got $other")
    }
  }

  private lazy val collectionName = "test-collection"
  private lazy val testCollection = bsonCollection(collectionName)()

  override protected def afterAll(): Unit = {
    super.afterAll()
    dropTestCollection(collectionName)
  }
}
