package de.htwg.se.orderandchaos.data.control

import java.util.UUID

import akka.actor.ActorSystem
import de.htwg.se.orderandchaos.data.db.slick.SlickDb
import de.htwg.se.orderandchaos.data.{GridUpdater, WinChecker}

import scala.collection.mutable
import scala.concurrent.ExecutionContextExecutor
import scala.swing.{Publisher, Reactor}
import scala.util.{Failure, Success}

class SessionHandler(winChecker: WinChecker, updater: GridUpdater) extends Reactor {
  implicit val system: ActorSystem = ActorSystem("my-system")
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  private val factory = Control.getControlFactory(SlickDb.getInstance)
  private val sessions = mutable.Map.empty[String, Control]

  def apply(id: String): Control = sessions(id)

  def startSession(): String = {
    val id = UUID.randomUUID().toString
    sessions(id) = factory.getNewControl(id)
    listenTo(sessions(id))
    updater.updateBoard(id, sessions(id).controller)
    id
  }

  def endSession(id: String): Unit = {
    deafTo(sessions(id))
    sessions.remove(id)
  }

  reactions += {
    case e: CellSet =>
      val control = sessions(e.sessionId)
      val controller = control.controller
      winChecker.determineWinner(controller.grid)
        .onComplete{
          case Success(WinChecker.NONE) =>
            updater.updateBoard(e.sessionId, controller)
          case Success(WinChecker.ORDER) =>
            control.finish("Order")
            updater.announceWin(e.sessionId, controller, "Order")
          case Success(WinChecker.CHAOS) =>
            control.finish("Chaos")
            updater.announceWin(e.sessionId, controller, "Chaos")
          case Success(i) => new IllegalArgumentException("WinChecker returned unknown number: " + i).printStackTrace()
          case Failure(e) => throw e
        }
    case w: Win => updater.announceWin(w.sessionId, sessions(w.sessionId).controller, w.player)
  }
}
