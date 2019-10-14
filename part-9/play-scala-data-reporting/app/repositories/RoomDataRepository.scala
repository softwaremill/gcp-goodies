package repositories


import javax.inject.{Inject, Singleton}
import models.RoomData
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

/**
 * A repository for people.
 *
 * @param dbConfigProvider The Play db config provider. Play will inject this for you.
 */
@Singleton
class RoomDataRepository @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  // We want the JdbcProfile for this provider
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private class RoomDataTable(tag: Tag) extends Table[RoomData](tag, "roomdata") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def temperature = column[Double]("temperature")
    def humidity = column[Double]("humidity")
    def light = column[Double]("light")
    def co2 = column[Double]("co2")
    def humidityRatio = column[Double]("humidity_ratio")

    def * = (id, temperature, humidity, light, co2, humidityRatio) <> ((RoomData.apply _).tupled, RoomData.unapply)
  }

  private val roomData = TableQuery[RoomDataTable]

  def create(temperature: Double, humidity: Double, light: Double, co2: Double, humidityRatio: Double): Future[RoomData] = db.run {
    val query = (roomData.map(rd => (rd.temperature, rd.humidity, rd.light, rd.co2, rd.humidityRatio))
      returning roomData.map(_.id)
      into ((res, id) => RoomData(id, res._1, res._2, res._3, res._4, res._5))
      )
    println("query: " + query)
    query += (temperature, humidity, light, co2, humidityRatio)
  }

  /**
   * List all the people in the database.
   */
  def list(): Future[Seq[RoomData]] = db.run {
    roomData.result
  }
}
