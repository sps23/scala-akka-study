package silverstar.learning.akka.section3

import akka.actor.{Actor, ActorIdentity, ActorRef, ActorSelection, ActorSystem, Identify, PoisonPill, Props}

class Counter extends Actor {
  import Counter._

  var count = 0

  override def receive: Receive = {
    case Inc(x) => count += x
    case Dec(x) => count -= x
  }
}

object Counter {

  final case class Inc(num: Int)
  final case class Dec(num: Int)
}

class Watcher extends Actor {

  var counterRef: ActorRef = _

  val selection: ActorSelection = context.actorSelection("/user/counter")

  selection ! Identify(None)

  override def receive: Receive = {
    case ActorIdentity(_, Some(ref)) => println(s"Watcher: Actor Reference for counter is $ref")
    case ActorIdentity(_, None)      => println("Watcher: Actor selection for actor doesn't live :( ")

  }
}

object ActorPath extends App {

  def print(aRef: ActorRef, aName: String): Unit = {
    println(s"Actor reference: $aRef")
    println(s"Actor selection: ${actorSystem.actorSelection(s"/user/$aName")}")
  }

  def kill(aRef: ActorRef): Unit = {
    aRef ! PoisonPill
    Thread.sleep(100)
  }

  val actorSystem: ActorSystem = ActorSystem("watch-actor-selection")

  val watcher: ActorRef = actorSystem.actorOf(Props[Watcher], "watcher")

  val actorName: String  = "counter"
  val counter1: ActorRef = actorSystem.actorOf(Props[Counter], actorName)
  print(counter1, actorName)
  kill(counter1)

  val counter2: ActorRef = actorSystem.actorOf(Props[Counter], actorName)
  print(counter2, actorName)
  kill(counter2)

  Thread.sleep(1000)
  actorSystem.terminate()
}
