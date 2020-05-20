package de.htwg.se.orderandchaos.session

import java.util.UUID

import de.htwg.se.orderandchaos.game.control.Control
import de.htwg.se.orderandchaos.view.tui.CommandTranslator

import scala.collection.mutable
import scala.swing.{Publisher, Reactor}
import scala.swing.event.Event

class SessionHandler extends Reactor {
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
    case e: Event => SessionEventForwarder.publish(e)
  }
}

private object SessionEventForwarder extends Publisher