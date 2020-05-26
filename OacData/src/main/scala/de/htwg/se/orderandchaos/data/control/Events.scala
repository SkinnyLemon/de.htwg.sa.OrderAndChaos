package de.htwg.se.orderandchaos.data.control

import scala.swing.event.Event

class CellSet(val sessionId: String) extends Event

class Win(val sessionId: String, val player: String) extends Event
