package silverstar.learning.akka.section2

import akka.actor.SupervisorStrategy.{Escalate, Restart, Resume, Stop}
import akka.actor.{Actor, ActorRef, ActorSystem, OneForOneStrategy, Props}

class Aphrodite extends Actor {
  import Aphrodite._

  override def preStart(): Unit = {
    println("Aphrodite preStart hook....")
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    println("Aphrodite preRestart hook...")
    super.preRestart(reason, message)
  }

  override def postRestart(reason: Throwable): Unit = {
    println("Aphrodite postRestart hook...")
    super.postRestart(reason)
  }

  override def postStop(): Unit = {
    println("Aphrodite postStop...")
  }

  override def receive: Receive = {
    case "Resume"  => throw ResumeException
    case "Stop"    => throw StopException
    case "Restart" => throw RestartException
    case other =>
      println(s"Aphrodite received message `$other`")
      throw new Exception
  }
}

object Aphrodite {
  case object ResumeException  extends Exception
  case object StopException    extends Exception
  case object RestartException extends Exception
}

class Hera(aphrodite: ActorRef) extends Actor {
  import scala.concurrent.duration._
  import Aphrodite._

  override val supervisorStrategy: OneForOneStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1.second) {
      case ResumeException  => Resume
      case RestartException => Restart
      case StopException    => Stop
      case _: Exception     => Escalate
    }

//  override def preStart(): Unit = {
//    // Create Aphrodite Actor
//    childRef = context.actorOf(Props[Aphrodite], "Aphrodite")
//    Thread.sleep(100)
//  }

  override def receive: Receive = {
    case msg =>
      println(s"Hera received $msg")
      aphrodite ! msg
      Thread.sleep(100)
  }
}

object Supervision extends App {

  val actorSystem: ActorSystem = ActorSystem("supervision")
  val aphrodite: ActorRef      = actorSystem.actorOf(Props[Aphrodite], "Aphrodite")
  val hera: ActorRef           = actorSystem.actorOf(Props(classOf[Hera], aphrodite), "Hera")

  hera ! "Resume"
//  hera ! "Restart"
//  hera ! "Stop"

  Thread.sleep(1000)
  println()
  actorSystem.terminate()
}
