package de.htwg.se.orderandchaos.ui.gui

import de.htwg.se.orderandchaos.ui.GameController
import de.htwg.se.orderandchaos.ui.util.{CellSet, Win}

import scala.swing._
import scala.util.{Failure, Success, Try}

class SwingGui(controller: GameController) extends Frame {
  private val width: Int = 6
  listenTo(controller)
  private var id = ""

  title = "Order and Chaos"
  menuBar = new MenuBar {
    contents += new Menu("Edit") {
//      contents += new MenuItem(Action("Undo") {
//        sessions(id).undo()
//      })
//      contents += new MenuItem(Action("Redo") {
//        sessions(id).redo()
//      })
      contents += new MenuItem(Action("Reset") {
        controller.resetGame(id)
      })
    }
    contents += new Menu("File") {
      contents += new MenuItem(Action("Save") {
        controller.saveGame(id)
      })
      contents += new MenuItem(Action("Load") {
        controller.loadGame(id)
      })
    }
  }
  visible = true
  repaint()

  def update(rows: Array[Array[String]], locked: Boolean, header: String = ""): BoxPanel = Try({
    val cells: Vector[CellPanel] =
      (for (y <- width - 1 to 0 by -1; x <- 0 until width)
        yield new CellPanel(rows(y)(x), locked, controller.play(id, x, y, _)))
        .toVector
    val gridPanel = new GridPanel(width, width) {
      contents ++= cells
    }
    new BoxPanel(Orientation.Vertical) {
      contents += menuBar
      contents += new Label(header)
      contents += gridPanel
    }
  }) match {
    case Success(value) => value
    case Failure(_) => new BoxPanel(Orientation.Vertical)
  }

  reactions += {
    case ev: CellSet =>
      if (ev.sessionId != id) {
        this.id = ev.sessionId
      }
      contents = update(ev.rows, locked = false)
      repaint()
    case ev: Win =>
      if (ev.sessionId != id) {
        this.id = ev.sessionId
      }
      contents = update(ev.rows, locked = true, "Winner: " + ev.winner)
      repaint()
  }
}
