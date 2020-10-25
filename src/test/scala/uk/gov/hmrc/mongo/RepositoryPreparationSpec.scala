/*
 * Copyright 2020 HM Revenue & Customs
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
import org.scalatest.{BeforeAndAfterAll, WordSpec, WordSpecLike}
import play.api.libs.json._
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.{BSONDocument, BSONObjectID}

class RepositoryPreparationSpec extends WordSpec with MongoSpecSupport with BeforeAndAfterAll {

  "prepare" should {

    "drop the old repository defining collection and create indexes on a newly created one" in new Setup {
      await(repository.removeAll())

      val document = Json.obj("field" -> "value")
      await(repository.insert(document))

      await(repository.findAll()).map(json => (json \ "field").as[String]) shouldBe List("value")

      prepare(repository)

      await(repository.findAll()) shouldBe empty

      val indexes: List[(String, IndexType)] = await {
        repository.collection.indexesManager.list()
      }.flatMap(_.key)

      indexes should contain theSameElementsAs Seq("_id" -> IndexType.Ascending, "field" -> IndexType.Ascending)
    }
  }

  private lazy val jsObjectFormat = OFormat((x: JsValue) => x match {
    case o: JsObject => JsSuccess(o)
    case x => JsError(s"JsObject expected but found $x")
  }, (o: JsObject) => o)

  private lazy val collectionName = "test-collection"

  private lazy val repository = new ReactiveRepository[JsObject, BSONObjectID](collectionName, mongo, jsObjectFormat) {
    override def indexes: Seq[Index.Default] = Seq(
      Index(
        key = Seq("field" -> IndexType.Ascending),
        name = None,
        unique = false,
        background = false,
        sparse = false,
        expireAfterSeconds = None,
        storageEngine = None,
        weights = None,
        defaultLanguage = None,
        languageOverride = None,
        textIndexVersion = None,
        sphereIndexVersion = None,
        bits = None,
        min = None,
        max = None,
        bucketSize = None,
        collation = None,
        wildcardProjection = None,
        version = None,
        partialFilter = None,
        options = BSONDocument.empty
      )

    )
  }

  private trait Setup extends WordSpecLike with RepositoryPreparation {}

  override protected def afterAll(): Unit = {
    super.afterAll()
    dropTestCollection(collectionName)
  }
}
