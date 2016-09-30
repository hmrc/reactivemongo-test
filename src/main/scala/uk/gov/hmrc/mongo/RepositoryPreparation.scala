package uk.gov.hmrc.mongo

import org.scalatest.Suite

trait RepositoryPreparation extends Awaiting {
  this: Suite =>

  def prepare[A <: ReactiveRepository[_, _]](repo: A): A = {
    val db = repo.collection.db.name + "." + repo.collection.name

    println(s"Dropping '$db'")
    await(repo.drop.recover {
      case _ => fail(s"Failed to drop '$db'")
    })

    println(s"Applying indexes on '$db'")
    await(repo.ensureIndexes.recover {
      case _ => fail(s"Failed to ensureIndexes on '$db'")
    })

    repo
  }

}
