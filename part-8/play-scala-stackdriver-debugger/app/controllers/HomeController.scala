package controllers

import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
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
  apiSampleService: ApiSampleService
)(implicit assetsFinder: AssetsFinder, ec: ExecutionContext, mat: Materializer)
    extends AbstractController(cc) {

  val logger = Logger(this.getClass)

  import scala.language.postfixOps

  implicit val timeout: Timeout = Timeout(10 seconds)

  def index = Action.async { implicit req: Request[_] =>
    Future.successful(Ok(Json.obj("status" -> "ok")))
  }

  def smthToDebug(items: Int) = Action.async { implicit req =>
    Logger.info(s"smthToDebug called with items: $items")
    Source(0 to items)
      .map { _ =>
        apiSampleService.sample()
      }
      .runWith(Sink.collection)
      .map(_.toList)
      .map { res =>
        Ok(Json.toJson(res))
      }
  }

}
