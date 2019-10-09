package controllers

import javax.inject._
import org.slf4j.LoggerFactory
import play.api.mvc._
import services.Counter

/**
 * This controller demonstrates how to use dependency injection to
 * bind a component into a controller class. The class creates an
 * `Action` that shows an incrementing count to users. The [[Counter]]
 * object is injected by the Guice dependency injection system.
 */
@Singleton
class CountController @Inject() (cc: ControllerComponents,
                                 counter: Counter) extends AbstractController(cc) {

  private val logger = LoggerFactory.getLogger(classOf[CountController])
  /**
   * Create an action that responds with the [[Counter]]'s current
   * count. The result is plain text. This `Action` is mapped to
   * `GET /count` requests by an entry in the `routes` config file.
   */
  def count = Action {
    logger.info("Some INFO log")
    logger.error("Some ERROR log")
    logger.warn("Some WARN log")
    Ok(counter.nextCount().toString) }

}
