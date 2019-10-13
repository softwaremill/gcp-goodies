package actor

import java.util.Date

import akka.actor.{Actor, ActorLogging}
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import javax.inject.Inject
import play.api.Configuration
import repositories.RoomDataRepository
import services.OccupancyDataProvider

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Random, Success}

sealed trait Command
case object PushToDatabaseCommand extends Command

class WorkerActor @Inject()(
  conf: Configuration,
  occupancyDataProvider: OccupancyDataProvider,
  roomDataRepository: RoomDataRepository
)(implicit val ec: ExecutionContext, mat: Materializer)
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
      case Success(data) =>
        val dt = data(random.nextInt(data.size))
        roomDataRepository.create(temperature = dt.temperature,
          humidity = dt.humidity,
          light = dt.light,
          co2 = dt.co2,
          humidityRatio = dt.humidityRatio).map(_ => ())
      case Failure(exception) =>
        log.error(s"WorkerActor exception ${exception.getMessage}")
    }
  }
}
