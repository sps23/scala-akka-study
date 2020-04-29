package silverstar.learning.akka.section3.routing

import akka.actor.Actor
import silverstar.learning.akka.section3.routing.Worker.Work

class RouterGroup(routees: List[String]) extends Actor {

  override def receive: Receive = {
    case msg: Work =>
      println(s"I'm a Router Group and I received Work Message....")
      context.actorSelection(routees(util.Random.nextInt(routees.size))) forward msg
    case other => println(s"Unsupported message `$other`")
  }
}
