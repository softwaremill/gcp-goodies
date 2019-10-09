package controllers

import actor._
import akka.actor.ActorRef
import akka.pattern._
import akka.util.Timeout
import javax.inject._
import org.slf4j.LoggerFactory
import play.api.mvc._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(cc: ControllerComponents, @Named("worker-actor") workerActor: ActorRef)
                              (implicit assetsFinder: AssetsFinder, ec: ExecutionContext)
  extends AbstractController(cc) {

  private val logger = LoggerFactory.getLogger(classOf[HomeController])

  import scala.language.postfixOps
  implicit val timeout: Timeout = Timeout(10 seconds)
  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def sendDoNothingCommand = Action.async { implicit req =>
    logger.info("do nothing command received")
    workerActor ! DoNothingCommand
    Future.successful(Ok("Done"))
  }

  def sendThrowNPECommand = Action.async { implicit req =>
    workerActor ! ThrowNPECommand
    Future.successful(Ok("Done"))
  }

  def sendThrowIllegalArgExceptionCommand = Action.async { implicit req =>
    workerActor ! ThrowIllegalArgExceptionCommand
    Future.successful(Ok("Done"))
  }

  def sendLogSomeErrorLevelMessagesCommand = Action.async { implicit req =>
    workerActor ! LogSomeErrorLevelMessagesCommand
    Future.successful(Ok("Done"))
  }

}
