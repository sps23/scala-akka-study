package silverstar.learning.akka.section2

import akka.actor.{Actor, ActorRef, ActorSystem, Props, Terminated}

class Ares(athena: ActorRef) extends Actor {

  override def preStart(): Unit = {
    context.watch(athena)
  }

  override def postStop(): Unit = {
    println("Ares postStop")
  }

  override def receive: Receive = {
    case Terminated(_) => context.stop(self)
    case other         => println(s"Unknown message `$other`")
  }
}

object Ares {

  def props(athena: ActorRef) = Props(new Ares(athena))
}

class Athena extends Actor {

  override def postStop(): Unit = {
    println("Athena postStop")
  }

  override def receive: Receive = {
    case msg =>
      println(s"Athena received message `$msg`")
      context.stop(self)
  }
}

object Monitoring extends App {

  val actorSystem: ActorSystem = ActorSystem("monitoring")
  val athena: ActorRef         = actorSystem.actorOf(Props[Athena], "athena")
  val ares: ActorRef           = actorSystem.actorOf(Props(classOf[Ares], athena), "ares")
//  val ares: ActorRef           = actorSystem.actorOf(Ares.props(athena), "ares")

  athena ! "Hi"

  Thread.sleep(100)

  actorSystem.terminate()
}
