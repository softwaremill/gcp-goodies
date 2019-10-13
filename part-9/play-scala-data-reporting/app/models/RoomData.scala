package models

import play.api.libs.json._


case class RoomData(id: Long, temperature: Double, humidity: Double, light: Double, co2: Double, humidityRatio: Double)

object RoomData {
  implicit val romDataFormat = Json.format[RoomData]
}
