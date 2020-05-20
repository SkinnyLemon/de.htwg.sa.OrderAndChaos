package de.htwg.se.orderandchaos.view.gui

import de.htwg.se.orderandchaos.game.control.{CellSet, Win}
import de.htwg.se.orderandchaos.session.SessionHandler
import de.htwg.se.orderandchaos.game.model.grid.Grid

import scala.swing._
import scala.util.{Failure, Success, Try}

class SwingGui(sessions: SessionHandler) extends Frame {
  listenTo(sessions.eventProvider)
  private var id = ""

  title = "Order and Chaos"
  menuBar = new MenuBar {
    contents += new Menu("Edit") {
      contents += new MenuItem(Action("Undo") {
        sessions(id).undo()
      })
      contents += new MenuItem(Action("Redo") {
        sessions(id).redo()
      })
      contents += new MenuItem(Action("Reset") {
        sessions(id).reset()
      })
    }
    contents += new Menu("File") {
      contents += new MenuItem(Action("Save") {
        sessions(id).save()
      })
      contents += new MenuItem(Action("Load") {
        sessions(id).load()
      })
    }
  }
  contents = update()
  visible = true
  repaint()

  def update(): BoxPanel = Try({
    val control = sessions(id)
    val locked = !control.controller.isOngoing
    val cells: Vector[CellPanel] =
      (for (y <- Grid.WIDTH - 1 to 0 by -1; x <- 0 until Grid.WIDTH)
        yield new CellPanel(x, y, control, locked))
        .toVector
    val gridPanel = new GridPanel(Grid.WIDTH, Grid.WIDTH) {
      contents ++= cells
    }
    new BoxPanel(Orientation.Vertical) {
      contents += menuBar
      contents += new Label(control.controller.header)
      contents += gridPanel
    }
  }) match {
    case Success(value) => value
    case Failure(_) => new BoxPanel(Orientation.Vertical)
  }

  reactions += {
    case ev: CellSet =>
      if (ev.sessionId != id)
        this.id = ev.sessionId
      contents = update()
      repaint()
    case ev: Win =>
      if (ev.sessionId != id)
        this.id = ev.sessionId
      contents = update()
      repaint()
  }
}
