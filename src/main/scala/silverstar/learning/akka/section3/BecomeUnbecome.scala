package silverstar.learning.akka.section3

import akka.actor.{Actor, ActorRef, ActorSystem, Props, Stash}
import silverstar.learning.akka.section3.UserStorage.{Connect, Disconnect, Operation}

case class User(username: String, email: String)

object UserStorage {

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

}

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

object BecomeUnbecome extends App {
  import UserStorage._

  private val john: User  = User("John", "john@mail.com")
  private val james: User = User("James", "james@mail.com")

  val actorSystem: ActorSystem = ActorSystem("Hotswap-Become")
  val userStorage: ActorRef    = actorSystem.actorOf(Props[UserStorage], "user-storage")

  // sending operations before connecting to DB
  userStorage ! Operation(DBOperation.Create, Some(john))
  userStorage ! Operation(DBOperation.Update, Some(john))

  userStorage ! Connect
  userStorage ! Operation(DBOperation.Read, Some(john))
  userStorage ! Operation(DBOperation.Delete, Some(john))
  userStorage ! Disconnect

  // sending operations after disconnecting from DB
  userStorage ! Operation(DBOperation.Read, Some(james))
  userStorage ! Operation(DBOperation.Update, Some(james))
  userStorage ! Connect

  Thread.sleep(1000)
  actorSystem.terminate()
}
