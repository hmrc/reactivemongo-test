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
import reactivemongo.bson.BSONObjectID

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

  private lazy val jsObjectFormat = Format[JsValue](
    Reads[JsValue](JsSuccess(_)),
    Writes[JsValue](identity)
  )

  private lazy val collectionName = "test-collection"

  private lazy val repository = new ReactiveRepository[JsValue, BSONObjectID](collectionName, mongo, jsObjectFormat) {
    override def indexes: Seq[Index] = Seq(
      Index(Seq("field" -> IndexType.Ascending))
    )
  }

  private trait Setup extends WordSpecLike with RepositoryPreparation {}

  override protected def afterAll(): Unit = {
    super.afterAll()
    dropTestCollection(collectionName)
  }
}
