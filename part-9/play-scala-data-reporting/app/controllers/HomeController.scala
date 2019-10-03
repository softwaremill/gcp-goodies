package controllers

import java.util.concurrent.TimeUnit

import actor._
import akka.actor.{ActorRef, ActorSystem}
import akka.pattern._
import akka.util.Timeout
import javax.inject._
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc._
import services._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class HomeController @Inject()(
  cc: ControllerComponents,
  @Named("worker-actor") workerActor: ActorRef,
  actorSystem: ActorSystem
)(implicit assetsFinder: AssetsFinder, ec: ExecutionContext)
    extends AbstractController(cc) {

  val logger = Logger(this.getClass)

  import scala.language.postfixOps
  implicit val timeout: Timeout = Timeout(10 seconds)

  actorSystem.scheduler.schedule(5 seconds, 1 seconds) {
    (workerActor ? actor.PushToBigTableCommand)
  }

  /**
    * Create an Action to render an HTML page with a welcome message.
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/`.
    */
  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

}
