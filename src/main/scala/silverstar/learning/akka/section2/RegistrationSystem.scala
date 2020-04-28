package silverstar.learning.akka.section2

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.pattern.AskableActorRef
import akka.util.Timeout
import silverstar.learning.akka.section2.Checker.{BlackUser, CheckUser, WhiteUser}
import silverstar.learning.akka.section2.Recorder.NewUser
import silverstar.learning.akka.section2.Storage.AddUser

import scala.collection.mutable

case class User(userName: String, email: String)

object Recorder {
  sealed trait RecorderMsg
  case class NewUser(user: User) extends RecorderMsg

  def props(checker: AskableActorRef, storage: ActorRef): Props = Props(new Recorder(checker, storage))
}

object Checker {
  sealed trait CheckerMsg
  case class CheckUser(user: User) extends CheckerMsg

  sealed trait CheckerResponse
  case class BlackUser(user: User) extends CheckerResponse
  case class WhiteUser(user: User) extends CheckerResponse

  def props(blackList: Set[User]): Props = Props(new Checker(blackList))
}

object Storage {
  sealed trait StorageMsg
  case class AddUser(user: User) extends StorageMsg

  val props: Props = Props[Storage]
}

class Storage extends Actor {

  val users: mutable.Set[User] = mutable.HashSet.empty[User]

  override def receive: Receive = {
    case AddUser(user) =>
      println(s"Storage: $user added")
      users.add(user)
  }
}

class Checker(blackList: Set[User]) extends Actor {
  override def receive: Receive = {
    case CheckUser(user) =>
      if (blackList.contains(user)) {
        println(s"Checker: User $user is blacklisted")
        sender() ! BlackUser(user)
      } else {
        println(s"Checker: User $user NOT blacklisted")
        sender() ! WhiteUser(user)
      }

  }
}

class Recorder(checker: AskableActorRef, storage: ActorRef) extends Actor {
  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.duration._

  implicit val timeout: Timeout = Timeout(5.seconds)

  override def receive: Receive = {
    case NewUser(user) =>
      checker ? CheckUser(user) map {
        case WhiteUser(user) => storage ! AddUser(user)
        case BlackUser(user) => println(s"Recorder: User $user is blacklisted")
      }
    case _ => println("Unknown user")
  }
}

object RegistrationSystem extends App {

  val actorSystem: ActorSystem = ActorSystem("registration-system")

  val blackUser: User      = User("Adam", "adam@mail.com")
  val blackList: Set[User] = Set(blackUser)
  val checker: ActorRef    = actorSystem.actorOf(Checker.props(blackList), "checker")
  val storage: ActorRef    = actorSystem.actorOf(Storage.props, "storage")
  val recorder: ActorRef   = actorSystem.actorOf(Recorder.props(checker, storage), "recorder")

  recorder ! Recorder.NewUser(User("Jon", "jon@mail.com"))
  recorder ! Recorder.NewUser(User("Mike", "mike@mail.com"))
  recorder ! Recorder.NewUser(blackUser)

  Thread.sleep(100)

  actorSystem.terminate()
}
