package de.htwg.se.orderandchaos.session

import java.util.UUID

import de.htwg.se.orderandchaos.data.control.{CellSet, Control, Win}
import de.htwg.se.orderandchaos.winconditionchecker.WinConditionChecker

import scala.collection.mutable
import scala.swing.{Publisher, Reactor}

class SessionHandler(winConditionChecker: WinConditionChecker = WinConditionChecker.get) extends Reactor {
  private val factory = Control.getControlFactory
  private val sessions = mutable.Map.empty[String, Control]

  def apply(id: String): Control = sessions(id)

  def startSession(): String = {
    val id = UUID.randomUUID().toString
    sessions(id) = factory.getNewControl(id)
    listenTo(sessions(id))
    id
  }

  def endSession(id: String): Unit = {
    deafTo(sessions(id))
    sessions.remove(id)
  }

  def eventProvider: Publisher = SessionEventForwarder

  reactions += {
    case e: CellSet =>
      val control = sessions(e.sessionId)
      val grid = control.controller.grid
      if (winConditionChecker.winningLineExists(grid)) {
        control.finish("Order")
        SessionEventForwarder.publish(new Win(e.sessionId, "Order"))
      } else if (winConditionChecker.noWinningLinePossible(grid)) {
        control.finish("Chaos")
        SessionEventForwarder.publish(new Win(e.sessionId, "Chaos"))
      } else {
        SessionEventForwarder.publish(new CellSet(e.sessionId))
      }
    case _ => throw new IllegalArgumentException("Wins should be determined by the SessionHandler")
  }
}

private object SessionEventForwarder extends Publisher