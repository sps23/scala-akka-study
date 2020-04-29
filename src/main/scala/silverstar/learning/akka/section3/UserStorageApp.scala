package silverstar.learning.akka.section3

import akka.actor.{Actor, ActorRef, ActorSystem, FSM, Props, Stash}

case class User(username: String, email: String)

trait DBOperation
object DBOperation {
  case object Create extends DBOperation
  case object Update extends DBOperation
  case object Read   extends DBOperation
  case object Delete extends DBOperation
}

case object Connect
case object Disconnect
case class Operation(dBOperation: DBOperation, user: Option[User])

class UserStorage extends Actor with Stash {

  override def receive: Receive = disconnected

  def connected: Receive = {
    case Disconnect =>
      println("User Storage Disconnect from DB")
      context.unbecome()
    case Operation(op, user) =>
      println(s"User Storage receive $op to do in user: $user")

  }

  def disconnected: Receive = {
    case Connect =>
      println(s"User Storage Connect to DB")
      unstashAll()
      context.become(connected)
    case _ =>
      stash()
  }
}

object UserStorageFSM {
  sealed trait State
  case object Connected    extends State
  case object Disconnected extends State

  sealed trait Data
  case object EmptyData extends Data
}

class UserStorageFSM extends FSM[UserStorageFSM.State, UserStorageFSM.Data] with Stash {
  import UserStorageFSM._

  startWith(Disconnected, EmptyData)

  when(Disconnected) {
    case Event(Connect, _) =>
      println("UserStorage Connected to DB")
      unstashAll()
      goto(Connected) using EmptyData
    case Event(_, _) =>
      stash()
      stay using EmptyData
  }

  when(Connected) {
    case Event(Disconnect, _) =>
      println("UserStorage disconnected from DB")
      goto(Disconnected) using EmptyData

    case Event(Operation(op, user), _) =>
      println(s"UserStorage receive $op operation to do in user: $user")
      stay using EmptyData
  }

  initialize()
}

object UserStorageApp extends App {

  private val john: User  = User("John", "john@mail.com")
  private val james: User = User("James", "james@mail.com")

  def performActions(aRef: ActorRef): Unit = {
    println(s"\nPerforming actions for `$aRef`")

    // sending operations before connecting to DB
    aRef ! Operation(DBOperation.Create, Some(john))
    aRef ! Operation(DBOperation.Update, Some(john))

    aRef ! Connect
    aRef ! Operation(DBOperation.Read, Some(john))
    aRef ! Operation(DBOperation.Delete, Some(john))
    aRef ! Disconnect

    // sending operations after disconnecting from DB
    aRef ! Operation(DBOperation.Read, Some(james))
    aRef ! Operation(DBOperation.Update, Some(james))
    aRef ! Connect

    Thread.sleep(1000)
    println("Finished")
  }

  val actorSystem: ActorSystem = ActorSystem("user-storage-system")
  val userStorage: ActorRef    = actorSystem.actorOf(Props[UserStorage], "user-storage")
  val userStorageFSM: ActorRef = actorSystem.actorOf(Props[UserStorageFSM], "user-storage-fsm")

  performActions(userStorage)
  performActions(userStorageFSM)

  actorSystem.terminate()
}
