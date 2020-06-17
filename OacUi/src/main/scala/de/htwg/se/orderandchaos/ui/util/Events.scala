package de.htwg.se.orderandchaos.ui.util

import scala.swing.event.Event

class CellSet(val sessionId: String, val rows: Array[Array[String]]) extends Event

class Win(val sessionId: String, val rows: Array[Array[String]], val winner: String) extends Event
