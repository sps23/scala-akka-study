package silverstar.learning.akka.section4

import akka.actor.{ActorSystem, Props}
import silverstar.learning.akka.section4.Counter._

object AkkaPersistenceApp extends App {

  val system  = ActorSystem("persistent-actors")
  val counter = system.actorOf(Props[Counter])

  counter ! Command(Increment(3))
  counter ! Command(Increment(12))
  counter ! Command(Decrement(5))
  counter ! "print"

  Thread.sleep(1000)
  system.terminate()
}
