package de.htwg.se.orderandchaos.data.control

import de.htwg.se.orderandchaos.data.control.controller.Controller
import de.htwg.se.orderandchaos.data.db.ControllerDao
import de.htwg.se.orderandchaos.data.db.slick.SlickDb
import de.htwg.se.orderandchaos.data.model.NoMoreMovesException
import de.htwg.se.orderandchaos.data.model.cell.Cell

import scala.concurrent.duration._

import scala.concurrent.Await
import scala.swing.Publisher
import scala.util.{Failure, Success, Try}

trait Control extends Publisher {
  def playRed(x: Int, y: Int): Try[Unit]

  def playBlue(x: Int, y: Int): Try[Unit]

  def play(x: Int, y: Int, fieldType: String): Try[Unit]

  def undo(): Try[Unit]

  def redo(): Try[Unit]

  def reset(): Try[Unit]

  def save(id: String): Unit

  def load(id: String): Unit

  def controller: Controller

  def makeString(cellToString: Cell => String): String

  def finish(winner: String): Controller
}

object Control {
  def getControlFactory(database: ControllerDao): ControlFactory = new ControlFactory(database)
}

class ControlFactory(database: ControllerDao) {
  def getNewControl(sessionId: String): Control = new ControlImpl(sessionId, database = database)
}

class ControlImpl(sessionId: String,
                  startController: Controller = Controller.getNew,
                  database: ControllerDao = SlickDb.getInstance) extends Control {
  private var currentController: Controller = startController
  private var pastMoves: Vector[Controller] = Vector.empty
  private var futureMoves: Vector[Controller] = Vector.empty

  override def controller: Controller = currentController

  override def playRed(x: Int, y: Int): Try[Unit] = play(x, y, Cell.TYPE_RED)

  override def playBlue(x: Int, y: Int): Try[Unit] = play(x, y, Cell.TYPE_BLUE)

  override def play(x: Int, y: Int, fieldType: String): Try[Unit] = currentController.play(x, y, fieldType).map(newController => {
    futureMoves = Vector.empty
    pastMoves = currentController +: pastMoves
    currentController = newController
    publish(new CellSet(sessionId))
  })

  override def finish(winner: String): Controller = {
    val gameOverController = Controller.getFinished(currentController.grid, winner)
    currentController = gameOverController
    gameOverController
  }

  //noinspection DuplicatedCode
  override def undo(): Try[Unit] = {
    if (pastMoves.isEmpty) return Failure(new NoMoreMovesException)
    futureMoves = currentController +: futureMoves
    currentController = pastMoves.head
    pastMoves = pastMoves.tail
    publish(new CellSet(sessionId))
    Success()
  }

  //noinspection DuplicatedCode
  override def redo(): Try[Unit] = {
    if (futureMoves.isEmpty) return Failure(new NoMoreMovesException)
    pastMoves = currentController +: pastMoves
    currentController = futureMoves.head
    futureMoves = futureMoves.tail
    publish(new CellSet(sessionId))
    Success()
  }

  override def reset(): Try[Unit] = {
    pastMoves = currentController +: pastMoves
    currentController = startController
    publish(new CellSet(sessionId))
    Success()
  }

  override def save(id: String): Unit = database.create(id, controller)

  override def load(id: String): Unit = {
    val controller = Await.result(database.read(id), 1000 millis)
    pastMoves = currentController +: pastMoves
    currentController = controller
    if (controller.isOngoing) publish(new CellSet(sessionId))
    else publish(new Win(sessionId, controller.turn))
  }

  override def toString: String = currentController.toString

  override def makeString(cellToString: Cell => String): String = currentController.makeString(cellToString)
}
