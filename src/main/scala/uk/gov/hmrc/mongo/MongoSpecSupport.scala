package uk.gov.hmrc.mongo

import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.BSONDocument
import reactivemongo.core.commands.LastError

import scala.concurrent.duration._
import reactivemongo.api.{DefaultDB, FailoverStrategy}

trait MongoSpecSupport {

  protected val databaseName: String = "test-" + this.getClass.getSimpleName

  protected val mongoUri: String = s"mongodb://127.0.0.1:27017/$databaseName"

  implicit val mongoConnectorForTest: MongoConnector = MongoConnector(mongoUri)

  implicit val mongo: () => DefaultDB = mongoConnectorForTest.db

  def bsonCollection(name: String)(
    failoverStrategy: FailoverStrategy = mongoConnectorForTest.helper.db.failoverStrategy): BSONCollection =
    mongoConnectorForTest.helper.db(name, failoverStrategy)

  def lastError(successful: Boolean, updated: Boolean = false, originalDoc: Option[BSONDocument] = None) =
    LastError(
      ok               = successful,
      err              = None,
      code             = None,
      errMsg           = None,
      originalDocument = originalDoc,
      updated          = if (updated) 1 else 0,
      updatedExisting  = updated
    )

}

trait Awaiting {

  import scala.concurrent._

  implicit val ec: ExecutionContextExecutor = ExecutionContext.Implicits.global

  val timeout: FiniteDuration = 5.seconds

  def await[A](future: Future[A])(implicit timeout: Duration = timeout) = Await.result(future, timeout)
}
