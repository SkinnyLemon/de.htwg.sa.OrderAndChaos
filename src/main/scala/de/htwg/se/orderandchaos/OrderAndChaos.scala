package de.htwg.se.orderandchaos

import com.google.inject.{Guice, Injector}
import de.htwg.se.orderandchaos.control.session.SessionHandler
import de.htwg.se.orderandchaos.view.TUI
import de.htwg.se.orderandchaos.view.gui.SwingGui

object OrderAndChaos {
  val injector: Injector = Guice.createInjector(new OacModule)
  val sessions: SessionHandler = injector.getInstance(classOf[SessionHandler])
  val tui: TUI = new TUI(sessions)
  // val gui: SwingGui = new SwingGui(sessions)

  def main(args: Array[String]): Unit = {
    tui.interpretLines()
  }
}
