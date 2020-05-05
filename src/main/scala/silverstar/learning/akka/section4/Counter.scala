package silverstar.learning.akka.section4

import akka.actor.ActorLogging
import akka.persistence.{PersistentActor, RecoveryCompleted, SaveSnapshotFailure, SaveSnapshotSuccess, SnapshotOffer}

object Counter {
  sealed trait Operation {
    val count: Int
  }

  case class Increment(override val count: Int) extends Operation
  case class Decrement(override val count: Int) extends Operation

  case class Command(op: Operation)
  case class Event(op: Operation)

  case class State(count: Int)
}

class Counter extends PersistentActor with ActorLogging {
  import Counter._

  println("Starting ........................")

  // Persistent Identifier
  override def persistenceId = "counter-example"

  var state: State = State(count = 0)

  def updateState(event: Event): Unit = {
    val newState: State = event match {
      case Event(Increment(count)) => State(count = state.count + count)
      case Event(Decrement(count)) => State(count = state.count - count)
    }
    state = newState
    takeSnapshot()
  }

  // Persistent receive on recovery mode
  val receiveRecover: Receive = {
    case event: Event =>
      println(s"Counter receive $event on recovering mode")
      updateState(event)
    case SnapshotOffer(_, snapshot: State) =>
      println(s"Counter receive snapshot with data: $snapshot on recovering mode")
      state = snapshot
    case RecoveryCompleted =>
      println(s"Recovery Complete and Now I'll switch to receiving mode :)")
  }

  // Persistent receive on normal mode
  val receiveCommand: Receive = {
    case cmd @ Command(op) =>
      println(s"Counter receive $cmd")
      persist(Event(op))(updateState)
    case "print" =>
      println(s"The Current state of counter is $state")
    case SaveSnapshotSuccess(metadata) =>
      println(s"save snapshot succeed - $metadata")
    case SaveSnapshotFailure(metadata, reason) =>
      println(s"save snapshot failed and failure is $reason - $metadata")
  }

  def takeSnapshot(): Unit = {
    if (state.count % 5 == 0) {
      saveSnapshot(state)
    }
  }

  //  override def recovery = Recovery.none

}
