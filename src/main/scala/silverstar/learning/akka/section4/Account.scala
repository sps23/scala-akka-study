package silverstar.learning.akka.section4

import akka.persistence.fsm.PersistentFSM
import akka.persistence.fsm.PersistentFSM.FSMState
import silverstar.learning.akka.section4.Account._

import scala.reflect.{ClassTag, classTag}

object Account {

  // Account States
  sealed trait State extends FSMState
  case object Empty extends State {
    override def identifier = "Empty"
  }
  case object Active extends State {
    override def identifier = "Active"
  }

  // Account Data
  sealed trait Data {
    val amount: Float
  }
  case object ZeroBalance extends Data {
    override val amount: Float = 0.0f
  }
  case class Balance(override val amount: Float) extends Data

  // Domain Events (Persist events)
  sealed trait Event
  case class AcceptedTransaction(amount: Float, `type`: TransactionType)                 extends Event
  case class RejectedTransaction(amount: Float, `type`: TransactionType, reason: String) extends Event

  // Transaction Types
  sealed trait TransactionType
  case object Credit extends TransactionType
  case object Debit  extends TransactionType

  // Commands
  case class Operation(amount: Float, `type`: TransactionType)
}

class Account extends PersistentFSM[Account.State, Account.Data, Account.Event] {

  override def persistenceId: String = "account"

  override def applyEvent(event: Account.Event, currentData: Account.Data): Account.Data = {
    event match {
      case AcceptedTransaction(amount, Credit) =>
        val newAmount = currentData.amount + amount
        println(s"Your new balance is $newAmount")
        Balance(currentData.amount + amount)
      case AcceptedTransaction(amount, Debit) =>
        val newAmount = currentData.amount - amount
        println(s"Your new balance is $newAmount")
        if (newAmount > 0) Balance(newAmount)
        else ZeroBalance
      case RejectedTransaction(_, _, reason) =>
        println(s"RejectedTransaction with reason: $reason")
        currentData
    }
  }

  override def domainEventClassTag: ClassTag[Account.Event] = classTag[Account.Event]

  startWith(Empty, ZeroBalance)

  when(Empty) {
    case Event(Operation(amount, Credit), _) =>
      println(s"Hi, It's your first Credit Operation.")
      goto(Active) applying AcceptedTransaction(amount, Credit)
    case Event(Operation(amount, Debit), _) =>
      println(s"Sorry, your account has zero balance.")
      stay applying RejectedTransaction(amount, Debit, "Balance is Zero")
  }

  when(Active) {
    case Event(Operation(amount, Credit), _) =>
      stay applying AcceptedTransaction(amount, Credit)
    case Event(Operation(amount, Debit), balance) =>
      val newBalance = balance.amount - amount
      if (newBalance > 0) {
        stay applying AcceptedTransaction(amount, Debit)
      } else if (newBalance == 0) {
        goto(Empty) applying AcceptedTransaction(amount, Debit)
      } else
        stay applying RejectedTransaction(amount, Debit, "balance doesn't cover this operation.")
  }
}
