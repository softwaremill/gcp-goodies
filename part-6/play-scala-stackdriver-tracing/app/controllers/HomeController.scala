package controllers

import java.util.concurrent.TimeUnit

import actor._
import akka.actor.ActorRef
import akka.pattern._
import akka.util.Timeout
import javax.inject._
import jp.co.bizreach.trace.ZipkinTraceServiceLike
import jp.co.bizreach.trace.akka.actor.ActorTraceSupport.TraceableActorRef
import jp.co.bizreach.trace.play26.implicits.ZipkinTraceImplicits
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc._
import services.ApiSampleService

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class HomeController @Inject()(cc: ControllerComponents, @Named("hello-actor") helloActor: ActorRef,
                               service: ApiSampleService)
                              (implicit assetsFinder: AssetsFinder, ec: ExecutionContext, val tracer: ZipkinTraceServiceLike)
  extends AbstractController(cc) with ZipkinTraceImplicits {

  val logger = Logger(this.getClass)

  import scala.language.postfixOps

  implicit val timeout: Timeout = Timeout(10 seconds)

  def index = Action.async { implicit req: Request[_] =>
    Future.successful(Ok(Json.obj("status" -> "ok")))
  }

  def once = Action.async { implicit req: Request[_] =>
    Logger.debug(req.headers.toSimpleMap.map { case (k, v) => s"${k}:${v}" }.toSeq.mkString("\n"))

    service.sample("http://localhost:9992/api/once").map(_ => Ok(Json.obj("OK" -> "OK")))
  }

  def nested = Action.async { implicit req: Request[_] =>
    Logger.debug(req.headers.toSimpleMap.map { case (k, v) => s"${k}:${v}" }.toSeq.mkString("\n"))

    implicit val timeout = Timeout(5000, TimeUnit.MILLISECONDS)
    val f1 = TraceableActorRef(helloActor) ? HelloActorMessage("This is an actor call!")
    val f2 = service.sample("http://localhost:9992/api/nest")

    for {
      r1 <- f1
      r2 <- f2
    } yield Ok(Json.obj("result" -> (r1 + " " + r2)))
  }

}
