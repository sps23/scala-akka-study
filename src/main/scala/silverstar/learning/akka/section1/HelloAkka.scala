package silverstar.learning.akka.section1

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

case class WhoToGreet(who: String)

class Greeter extends Actor {
  override def receive: Receive = {
    case WhoToGreet(who) => println(s"Hello $who")
  }
}

object HelloAkka extends App {

  val system: ActorSystem = ActorSystem("Hello-Akka")
  val greeter: ActorRef = system.actorOf(Props[Greeter], "greeter")
  greeter ! WhoToGreet("Akka")
}
