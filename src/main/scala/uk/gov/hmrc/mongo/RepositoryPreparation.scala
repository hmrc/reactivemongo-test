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

import org.scalatest.Suite

trait RepositoryPreparation extends Awaiting {
  this: Suite =>

  def prepare[A <: ReactiveRepository[_, _]](repo: A): A = {
    val db = repo.collection.db.name + "." + repo.collection.name

    println(s"Dropping '$db'")
    await {
      repo.drop.recover {
        case _ => fail(s"Failed to drop '$db'")
      }
    }

    println(s"Applying indexes on '$db'")
    await {
      repo.ensureIndexes.recover {
        case _ => fail(s"Failed to ensureIndexes on '$db'")
      }
    }

    repo
  }
}

trait Awaiting {

  import scala.concurrent._
  import scala.concurrent.duration._
  import scala.language.postfixOps

  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

  val timeout: FiniteDuration = 5 seconds

  def await[A](future: Future[A])(implicit timeout: Duration = timeout): A =
    Await.result(future, timeout)
}
