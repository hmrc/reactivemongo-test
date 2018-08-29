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

import java.util.UUID.randomUUID

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{Matchers, WordSpec}
import play.api.libs.json.{Json, OFormat}
import reactivemongo.api.DB
import reactivemongo.api.indexes.{Index, IndexType}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class FailOnUnindexedQueriesSpec
    extends WordSpec
    with Matchers
    with MongoSpecSupport
    with ScalaFutures
    with IntegrationPatience
    with FailOnUnindexedQueries {

  "Running mongo queries" should {

    "fail if query didn't use an index" in {
      val repo = new TestRepo(mongo)
      repo.insert(TestModel.random).futureValue

      val exception = repo.find("unindexedField" -> "n/a").failed.futureValue
      exception.getMessage should include("No query solutions")
    }

    "succeed if query used an index" in {
      val repo = new TestRepo(mongo)
      repo.insert(TestModel.random).futureValue

      noException shouldBe thrownBy(repo.find("indexedField" -> "n/a").futureValue)
    }
  }

}

final case class TestModel(indexedField: String, unindexedField: String)

object TestModel {
  def random: TestModel                   = TestModel(randomUUID().toString, randomUUID().toString)
  implicit val format: OFormat[TestModel] = Json.format[TestModel]
}

class TestRepo(mongo: () => DB) extends ReactiveRepository("test-collection", mongo, TestModel.format) {

  override def ensureIndexes(implicit ec: ExecutionContext): Future[Seq[Boolean]] =
    Future.sequence(
      Seq(
        collection.indexesManager
          .ensure(Index(Seq("indexedField" -> IndexType.Hashed), name = Some(s"indexedField-idx")))
      ))

}
