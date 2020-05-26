package de.htwg.se.orderandchaos.ui

import com.google.inject.{Guice, Injector}
import de.htwg.se.orderandchaos.session.SessionHandler
import de.htwg.se.orderandchaos.ui.gui.SwingGui
import de.htwg.se.orderandchaos.ui.tui.TUI

object OrderAndChaos {
  val injector: Injector = Guice.createInjector(new OacModule)
  val sessions: SessionHandler = injector.getInstance(classOf[SessionHandler])
  val tui: TUI = new TUI(sessions)
  val gui: SwingGui = new SwingGui(sessions)

  def main(args: Array[String]): Unit = {
    tui.interpretLines()
  }
}
