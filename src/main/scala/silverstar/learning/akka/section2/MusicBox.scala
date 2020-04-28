package silverstar.learning.akka.section2

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import silverstar.learning.akka.section2.MusicController.{Play, Stop}
import silverstar.learning.akka.section2.MusicPlayer.{StartMusic, StopMusic}

class MusicController extends Actor {
  override def receive: Receive = {
    case Play => println("Music started ...")
    case Stop => println("Music stopped ...")
    case _    => println("Unknown message")
  }
}

object MusicController {
  sealed trait MusicControllerMsg
  case object Play extends MusicControllerMsg
  case object Stop extends MusicControllerMsg

  val props: Props = Props[MusicController]
}

class MusicPlayer extends Actor {
  override def receive: Receive = {
    case StartMusic =>
      println("Start playing music")
      val musicController: ActorRef = context.actorOf(MusicController.props, "musicController")
      musicController ! Play
    case StopMusic => println("I don't want to stop music!!!")
    case _         => println("Unknown message")
  }
}

object MusicPlayer {
  sealed trait MusicPlayerMsg
  case object StartMusic extends MusicPlayerMsg
  case object StopMusic  extends MusicPlayerMsg

  val props: Props = Props[MusicPlayer]
}

object MusicBox extends App {

  val actorSystem = ActorSystem("music-box")
  val musicPlayer = actorSystem.actorOf(MusicPlayer.props, "musicPlayer")

  musicPlayer ! StartMusic
  musicPlayer ! StopMusic
  musicPlayer ! StopMusic
  musicPlayer ! StopMusic
}
