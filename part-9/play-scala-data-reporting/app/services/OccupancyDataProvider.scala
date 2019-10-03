package services

import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import akka.stream.Materializer
import akka.stream.scaladsl.{FileIO, Framing, Sink}
import akka.util.ByteString
import javax.inject.Inject
import play.api.Configuration

import scala.concurrent.{ExecutionContext, Future}

case class OccupancyData(id: Long,
                         date: LocalDateTime,
                         temperature: Double,
                         humidity: Double,
                         light: Double,
                         co2: Double,
                         humidityRatio: Double)

trait OccupancyDataProvider {
  def readFile(): Future[List[OccupancyData]]
}

class OccupancyDataProviderImpl @Inject()(conf: Configuration)(
  implicit val mat: Materializer,
  val ec: ExecutionContext
) extends OccupancyDataProvider {

  val dataFile = conf
    .get[String]("occupancy-detection-dataset-location")

  val df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

  def readFile(): Future[List[OccupancyData]] = {
    FileIO
      .fromPath(Paths.get(dataFile))
      .via(Framing.delimiter(ByteString("\n"), 256, true).map(_.utf8String))
      .map(line => line.split(",").map(_.replaceAll("\"", "").trim))
      .prefixAndTail(1)
      .flatMapConcat { case (_, rows) => rows }
      .map { cols =>
        val id = cols(0).toLong
        val date = LocalDateTime.parse(cols(1), df)
        val temperature = cols(2).toDouble
        val humidity = cols(3).toDouble
        val light = cols(4).toDouble
        val co2 = cols(5).toDouble
        val humidityRatio = cols(6).toDouble
        OccupancyData(
          id,
          date,
          temperature,
          humidity,
          light,
          co2,
          humidityRatio
        )
      }
      .runWith(Sink.collection)
      .map(_.toList)
  }
}
