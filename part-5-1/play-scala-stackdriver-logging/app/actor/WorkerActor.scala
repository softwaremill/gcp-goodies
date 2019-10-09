package actor

import akka.actor.{Actor, ActorLogging}
import javax.inject.Inject

sealed trait Command
case object DoNothingCommand extends Command
case object ThrowNPECommand extends Command
case object ThrowIllegalArgExceptionCommand extends Command
case object LogSomeErrorLevelMessagesCommand extends Command

class WorkerActor @Inject()() extends Actor with ActorLogging {

  override def receive: Receive = {
    case DoNothingCommand => log.info("Received DoNothingCommand - doing nothing")
    case ThrowNPECommand =>
      log.info("Received ThrowNPECommand")
      throw new NullPointerException("Shame on you! ;)")
    case ThrowIllegalArgExceptionCommand =>
      log.info("Received ThrowIllegalArgExceptionCommand")
      throw new IllegalArgumentException("Illegal stuff is happening here!")
    case LogSomeErrorLevelMessagesCommand =>
      log.info("Received LogSomeErrorLevelMessagesCommand")
      log.error("logging message error 1")
      log.error("logging message error 2")
      log.error("logging message error 3")
  }

}
