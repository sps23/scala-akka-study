package silverstar.learning.akka.section3.routing

import akka.actor.{Actor, ActorRef, Props}
import silverstar.learning.akka.section3.routing.Worker.Work

class RouterPool extends Actor {

  var routeesPool: List[ActorRef] = _

  override def preStart(): Unit = {
    routeesPool = List.fill(5)(context.actorOf(Props[Worker]))
  }

  override def receive(): Receive = {
    case msg: Work =>
      println("I'm A Router and I received Work Message.....")
      routeesPool(util.Random.nextInt(routeesPool.size)) forward msg
    case other => println(s"Unsupported message `$other`")
  }
}
