package silverstar.learning.akka.section3.routing

import akka.actor.Actor

class Worker extends Actor {
  import Worker._

  override def receive: Receive = {
    case _: Work => println(s"I received Work Message and My ActorRef: $self")
    case other   => println(s"Unsupported message `$other`")
  }
}

object Worker {
  case class Work()
}
