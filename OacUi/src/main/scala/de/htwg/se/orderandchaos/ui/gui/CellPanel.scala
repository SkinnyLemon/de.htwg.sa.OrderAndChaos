package de.htwg.se.orderandchaos.ui.gui

import scala.swing.event.{MouseClicked, MouseEntered, MouseExited}
import scala.swing.{BoxPanel, Color, Dimension, Orientation, Swing}

class CellPanel(entry: String, isLocked: Boolean, play: String => Unit) extends BoxPanel(Orientation.Horizontal) {

  if (!isLocked && entry != "E") contents += redChoice += blueChoice
  preferredSize = new Dimension(50, 50)
  background =
    if (entry == "B") CellPanel.BLUE
    else if (entry == "R") CellPanel.RED
    else if (isLocked) CellPanel.LOCKED
    else CellPanel.EMPTY
  border = Swing.BeveledBorder(Swing.Raised)

  def redChoice: BoxPanel = choice(() => play("R"), CellPanel.RED)

  def blueChoice: BoxPanel = choice(() => play("R"), CellPanel.BLUE)

  def choice(choose: () => Unit, choiceColor: Color): BoxPanel = new BoxPanel(Orientation.Horizontal) {
    preferredSize = new Dimension(20, 40)
    background = CellPanel.EMPTY
    border = Swing.BeveledBorder(Swing.Raised)
    listenTo(mouse.clicks)
    listenTo(mouse.moves)
    reactions += {
      case MouseClicked(_, _, _, _, _) =>
        choose()
      case MouseEntered(_, _, _) =>
        background = choiceColor
        repaint()
      case MouseExited(_, _, _) =>
        background = CellPanel.EMPTY
        repaint()
    }
  }
}

object CellPanel {
  val RED = new Color(255, 0, 0)
  val BLUE = new Color(0, 0, 255)
  val EMPTY = new Color(255, 255, 255)
  val LOCKED = new Color(150, 150, 150)
}
