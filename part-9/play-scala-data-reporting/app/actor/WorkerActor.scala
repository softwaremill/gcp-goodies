package actor

import java.util.Date

import akka.actor.{Actor, ActorLogging}
import javax.inject.Inject
import play.api.Configuration
import services.OccupancyDataProvider

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Random, Success}

sealed trait Command
case object PushToDatabaseCommand extends Command

class WorkerActor @Inject()(
  conf: Configuration,
  occupancyDataProvider: OccupancyDataProvider
)(implicit val ec: ExecutionContext)
    extends Actor
    with ActorLogging {

  val TemperatureColumn = "temp"
  val HumidityColumn = "hum"
  val LightColumn = "light"
  val Co2Column = "co2"
  val HumidityRatioColumn = "humRatio"

  override def receive: Receive = {
    case PushToDatabaseCommand =>
      log.info("Received PushToDatabaseCommand")
      pushToDatabase()
  }

  def pushToDatabase(): Unit = {
    val random = new Random
    occupancyDataProvider.readFile().onComplete {
      case Success(data) => ()
      case Failure(exception) =>
        log.error(s"WorkerActor exception ${exception.getMessage}")
    }
  }
}
