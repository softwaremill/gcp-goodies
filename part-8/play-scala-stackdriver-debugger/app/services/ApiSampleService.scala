package services

import javax.inject.Inject
import play.api.Logger

class ApiSampleService @Inject()(dataGenerator: DataGenerator) {

  val VeryBadSampleDataFragement = "666"

  val logger = Logger(this.getClass)

  def sample(): String = {
    val data = dataGenerator.generateData()
    data
  }

  def filterSamples(sample: String): Boolean = {
    val isBad = sample.contains(VeryBadSampleDataFragement)
    if(isBad){
      notifyTheWorld(sample)
    }
    isBad
  }

  def notifyTheWorld(sample: String): Unit = {
    logger.warn(s"Very bad sample was generated ($sample), be careful!")
  }
}
