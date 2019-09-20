package actor

import akka.actor.ActorRef
import javax.inject.{Inject, Named}
import jp.co.bizreach.trace.ZipkinTraceServiceLike
import jp.co.bizreach.trace.akka.actor.ActorTraceSupport.{ActorTraceData, TraceMessage, TraceableActor, TraceableActorRef}

case class HelloActorMessage(message: String)(implicit val traceData: ActorTraceData) extends TraceMessage

class HelloActor @Inject()(@Named("child-hello-actor") child: ActorRef)
                          (implicit val tracer: ZipkinTraceServiceLike) extends TraceableActor {
  def receive = {
    case m: HelloActorMessage => {
      Thread.sleep(1000)
      println(m.message)
      TraceableActorRef(child) ! HelloActorMessage("This is a child actor call!")
      sender() ! "result"
    }
  }
}

class ChildHelloActor @Inject()(val tracer: ZipkinTraceServiceLike) extends TraceableActor {
  def receive = {
    case m: HelloActorMessage => {
      Thread.sleep(1000)
      println(m.message)
    }
  }
}
