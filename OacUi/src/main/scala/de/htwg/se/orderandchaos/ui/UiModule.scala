package de.htwg.se.orderandchaos.ui

import de.htwg.se.orderandchaos.ui.tui.TUI

object UiModule {
  def main(args: Array[String]): Unit = {
    val http = new UiHttp
    val tui: TUI = new TUI(http)
    // val gui: SwingGui = new SwingGui(http)
    tui.interpretLines()
  }
}
