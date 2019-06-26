/*
 * Copyright 2019 HM Revenue & Customs
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

import org.scalatest.Matchers._
import org.scalatest.WordSpec
import reactivemongo.api.commands.Command
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.api.{BSONSerializationPack, FailoverStrategy, ReadPreference}
import reactivemongo.bson.{BSONBoolean, BSONDocument, BSONValue}
import reactivemongo.core.errors.DetailedDatabaseException

class FailOnUnindexedQueriesSpec extends WordSpec with FailOnUnindexedQueries with MongoSpecSupport with Awaiting {

  "FailOnUnindexedQueries" should {

    "cause an exception be thrown when a query on unindexed property is performed" in {
      testCollection.insert(ordered = false).one(BSONDocument("unidexed" -> "value")).futureValue

      intercept[DetailedDatabaseException] {
        await(testCollection.find(BSONDocument("unidexed" -> "value"), projection = None).one)
      }.getMessage() should include("No query solutions")
    }

    "cause no exception be thrown when a query on indexed property is performed" in {
      await {
        testCollection.indexesManager.create(Index(Seq("indexed" -> IndexType.Ascending)))
      }

      testCollection.insert(ordered = false).one(BSONDocument("indexed" -> "value")).futureValue

      await {
        testCollection.find(BSONDocument("indexed" -> "value"), projection = None).one
      } should not be empty
    }
  }

  "beforeAll" should {

    "set parameter 'notablescan' on 'admin' database" in new BeforeAndAfterSetup {
      beforeAll()
      maybeNotableScanValue shouldBe Some(true)
    }
  }

  "afterAll" should {

    "unset parameter 'notablescan' on 'admin' database" in new BeforeAndAfterSetup {
      beforeAll()
      afterAll()
      maybeNotableScanValue shouldBe Some(false)
    }
  }

  private trait BeforeAndAfterSetup {

    private val runner = Command.run(BSONSerializationPack, FailoverStrategy())

    def maybeNotableScanValue: Option[Boolean] = await {
      for {
        adminDb <- mongo().connection.database("admin")
        maybeNotableScanValue <- runner(
                                  adminDb,
                                  runner.rawCommand(BSONDocument("getParameter" -> 1, "notablescan" -> 1)))
                                  .one[BSONDocument](ReadPreference.primaryPreferred)
                                  .map(_.get("notablescan"))
                                  .map(_.map(toBoolean))
      } yield maybeNotableScanValue
    }

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
