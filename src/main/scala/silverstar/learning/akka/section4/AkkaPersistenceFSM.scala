package silverstar.learning.akka.section4

import akka.actor.{ActorSystem, Props}

object AkkaPersistenceFSM extends App {
  import Account._

  val system  = ActorSystem("persistent-fsm-actors")
  val account = system.actorOf(Props[Account])

  account ! Operation(100, Credit)
  account ! Operation(200, Debit)
  account ! Operation(20, Debit)
  account ! Operation(1000, Credit)
  account ! Operation(500, Debit)

  Thread.sleep(1000)
  system.terminate()
}
