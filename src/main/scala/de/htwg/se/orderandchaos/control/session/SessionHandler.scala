package de.htwg.se.orderandchaos.control.session

import java.util.UUID

import de.htwg.se.orderandchaos.control.game.{CommandTranslator, Control}

import scala.collection.mutable
import scala.swing.{Publisher, Reactor}
import scala.swing.event.Event

class SessionHandler extends Reactor {
  private val factory = Control.getControlFactory
  private val sessions = mutable.Map.empty[String, Session]

  def apply(id: String): Session = sessions(id)

  def startSession(): String = {
    val id = UUID.randomUUID().toString
    sessions(id) = new Session(id, factory.getNewControl(id))
    listenTo(sessions(id).control)
    id
  }

  def endSession(id: String): Unit = {
    deafTo(sessions(id).control)
    sessions.remove(id)
  }

  def eventProvider: Publisher = SessionEventForwarder

  reactions += {
    case e: Event => SessionEventForwarder.publish(e)
  }
}

class Session(val id: String, val control: Control) {
  val commandTranslator: CommandTranslator = new CommandTranslator(control)
}

private object SessionEventForwarder extends Publisher