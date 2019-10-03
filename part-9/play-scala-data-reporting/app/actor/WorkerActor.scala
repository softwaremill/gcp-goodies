package actor

import java.util.Date

import akka.actor.{Actor, ActorLogging}
import com.google.cloud.bigtable.data.v2.models.RowMutation
import com.google.cloud.bigtable.data.v2.{BigtableDataClient, BigtableDataSettings}
import javax.inject.Inject
import play.api.Configuration
import services.OccupancyDataProvider

import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal
import scala.util.{Failure, Random, Success}

sealed trait Command
case object PushToBigTableCommand extends Command

class WorkerActor @Inject()(
  conf: Configuration,
  occupancyDataProvider: OccupancyDataProvider
)(implicit val ec: ExecutionContext)
    extends Actor
    with ActorLogging {

  val projectId = conf.get[String]("gcp.projectId")
  val instanceId = conf.get[String]("gcp.instanceId")
  val tableId = conf.get[String]("gcp.tableId")
  val columnFamilyName = conf.get[String]("gcp.columnFamilyName")

  val TemperatureColumn = "temp"
  val HumidityColumn = "hum"
  val LightColumn = "light"
  val Co2Column = "co2"
  val HumidityRatioColumn = "humRatio"

  override def receive: Receive = {
    case PushToBigTableCommand =>
      log.info("Received PushToBigTableCommand")
      pushToBigtable()
  }

  def pushToBigtable(): Unit = {
    val random = new Random
    occupancyDataProvider.readFile().onComplete {
      case Success(data) => {
        val settings = BigtableDataSettings
          .newBuilder()
          .setProjectId(projectId)
          .setInstanceId(instanceId)
          .build()
        withResources(BigtableDataClient.create(settings)) { dataClient =>
          val occupancyData = data(random.nextInt(data.size))
          val mutation = RowMutation
            .create(tableId,  new Date().getTime.toString)
            .setCell(
              columnFamilyName,
              TemperatureColumn,
              occupancyData.temperature.toString
            )
            .setCell(
              columnFamilyName,
              HumidityColumn,
              occupancyData.humidity.toString
            )
            .setCell(
              columnFamilyName,
              LightColumn,
              occupancyData.light.toString
            )
            .setCell(
              columnFamilyName,
              Co2Column,
              occupancyData.co2.toString
            )
            .setCell(
              columnFamilyName,
              HumidityRatioColumn,
              occupancyData.humidityRatio.toString
            )
          dataClient.mutateRow(mutation)
        }

      }
      case Failure(exception) =>
        log.error(s"WorkerActor exception ${exception.getMessage}")
    }
  }

  def withResources[T <: AutoCloseable, V](r: => T)(f: T => V): V = {
    val resource: T = r
    require(resource != null, "resource is null")
    var exception: Throwable = null
    try {
      f(resource)
    } catch {
      case NonFatal(e) =>
        exception = e
        throw e
    } finally {
      closeAndAddSuppressed(exception, resource)
    }
  }

  private def closeAndAddSuppressed(e: Throwable,
                                    resource: AutoCloseable): Unit = {
    if (e != null) {
      try {
        resource.close()
      } catch {
        case NonFatal(suppressed) =>
          e.addSuppressed(suppressed)
      }
    } else {
      resource.close()
    }
  }
}
