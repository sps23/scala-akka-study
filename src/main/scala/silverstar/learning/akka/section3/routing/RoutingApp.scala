package silverstar.learning.akka.section3.routing

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.routing.{BroadcastPool, FromConfig, RoundRobinGroup}
import silverstar.learning.akka.section3.routing.Worker.Work

object RoutingApp extends App {

  def sendWork(aRef: ActorRef): Unit = {
    aRef ! Work()
    aRef ! Work()
    aRef ! Work()
    Thread.sleep(500)
  }

  val actorSystem: ActorSystem = ActorSystem("routing")
  val routerPool: ActorRef     = actorSystem.actorOf(Props[RouterPool], "router-pool")
  sendWork(routerPool)

  actorSystem.actorOf(Props[Worker], "w1")
  actorSystem.actorOf(Props[Worker], "w2")
  actorSystem.actorOf(Props[Worker], "w3")
  actorSystem.actorOf(Props[Worker], "w4")
  actorSystem.actorOf(Props[Worker], "w5")

  val workers: List[String] = List("/user/w1", "/user/w2", "/user/w3", "/user/w4", "/user/w5")
  val routerGroup: ActorRef = actorSystem.actorOf(Props(classOf[RouterGroup], workers))
  sendWork(routerGroup)

  val routerRandomPool: ActorRef = actorSystem.actorOf(FromConfig.props(Props[Worker]), "random-router-pool")
  sendWork(routerRandomPool)

  actorSystem.actorOf(Props[Worker], "w6")
  actorSystem.actorOf(Props[Worker], "w7")
  actorSystem.actorOf(Props[Worker], "w8")
  actorSystem.actorOf(Props[Worker], "w9")
  actorSystem.actorOf(Props[Worker], "w10")

  val workers2: List[String] = List("/user/w6", "/user/w7", "/user/w8", "/user/w9", "/user/w10")

  val roundRobinGroupRouter: ActorRef =
    actorSystem.actorOf(RoundRobinGroup(workers2).props(), "round-robin-router-group")
  sendWork(roundRobinGroupRouter)

  val broadcastPoolRouter: ActorRef =
    actorSystem.actorOf(BroadcastPool(5).props(Props[Worker]), "broadcast-router-pool")
  sendWork(broadcastPoolRouter)

  actorSystem.terminate()
}
