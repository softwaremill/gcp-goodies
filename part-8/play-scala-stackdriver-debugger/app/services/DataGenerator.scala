package services

import java.util.UUID

import javax.inject.Inject

trait DataGenerator {
  def generateData(): String
}

class DataGeneratorImpl @Inject()() extends DataGenerator {

  def generateData():String = {
    UUID.randomUUID().toString
  }
}