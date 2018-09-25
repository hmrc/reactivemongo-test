/*
 * Copyright 2018 HM Revenue & Customs
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
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, WordSpec}
import reactivemongo.api.DefaultDB
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.BSONDocument

import scala.concurrent.ExecutionContext.Implicits.global

class MongoSpecSupportSpec extends WordSpec with MongoSpecSupport with ScalaFutures with BeforeAndAfterAll {

  self =>

  "MongoSpecSupport" should {

    "construct 'databaseName' by prefixing spec class' name with 'test-'" in {
      databaseName shouldBe s"test-${self.getClass.getSimpleName}"
    }

    "construct 'mongoUri' using the 'mongodb://127.0.0.1:27017/<databaseName>' pattern" in {
      mongoUri shouldBe s"mongodb://127.0.0.1:27017/$databaseName"
    }

    "make MongoConnector implicitly available in the class extending the trait" in {
      val connector = implicitly[MongoConnector]

      connector                    shouldBe mongoConnectorForTest
      connector.mongoConnectionUri shouldBe mongoUri
    }

    "make '() => DefaultDB' function implicitly available in the class extending the trait" in {
      val dbFunction = implicitly[() => DefaultDB]

      dbFunction   shouldBe mongo
      dbFunction() shouldBe mongoConnectorForTest.db()
    }

    "provide a 'bsonCollection' method to return BSONCollection with the given name" in {
      val collection = bsonCollection(databaseName)()

      collection      shouldBe a[BSONCollection]
      collection.name shouldBe databaseName
    }

    "provide a 'dropTestCollection' method to delete a collection created by the MongoSpecSupport" in {
      testCollection.insert(BSONDocument("property" -> "value")).futureValue
      testCollection.find(BSONDocument(), projection = None).one.futureValue should not be empty

      testCollection      shouldBe a[BSONCollection]
      testCollection.name shouldBe databaseName

      dropTestCollection()

      testCollection.find(BSONDocument(), projection = None).one.futureValue shouldBe empty
    }

    "provide a 'testCollection' method to access a collection created by the MongoSpecSupport" in {
      testCollection.name shouldBe bsonCollection(databaseName)().name
    }
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    dropTestCollection()
  }
}
