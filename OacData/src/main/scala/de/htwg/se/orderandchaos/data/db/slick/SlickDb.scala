package de.htwg.se.orderandchaos.data.db.slick

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import de.htwg.se.orderandchaos.data.control.controller.Controller
import de.htwg.se.orderandchaos.data.db.ControllerDao
import de.htwg.se.orderandchaos.data.model.cell.Cell
import de.htwg.se.orderandchaos.data.model.grid.Grid
import slick.jdbc.H2Profile.api._

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContextExecutor, Future}

class SlickDb extends ControllerDao {
  implicit val system: ActorSystem = ActorSystem("my-system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  private val db = Database.forConfig("h2mem1")
  private val setup = DBIO.seq(
    (SlickSchemas.controllers.schema ++ SlickSchemas.cells.schema).create
  )
  private val setupFuture = db.run(setup)

  def read(id: String): Future[Controller] = {
    val cellFutures = Array.ofDim[Future[Cell]](Grid.WIDTH, Grid.WIDTH)
    for (x <- 0 until Grid.WIDTH; y <- 0 until Grid.WIDTH) {
      cellFutures(x)(y) = readCell(id, x, y)
    }
    val cells = cellFutures.map(_.map(Await.result(_, 1000 millis)).toSeq).toSeq
    val grid: Grid = Grid.fromSeq(cells)

    val query = for (
      controllerObj <- SlickSchemas.controllers if controllerObj.id === id
    ) yield (controllerObj.ongoing, controllerObj.turn)
    db.run(query.result)
      .map(tuple => {
        val ongoing = tuple.head._1
        val turn = tuple.head._2
        if (ongoing) Controller.getOngoing(grid, turn)
        else Controller.getFinished(grid, turn)
      })
  }

  def readCell(id: String, x: Int, y: Int): Future[Cell] = {
    val query = for (
      cellObj <- SlickSchemas.cells if cellObj.controllerId === id && cellObj.x === x && cellObj.y === y
    ) yield cellObj.cellType
    db.run(query.result)
      .map(cellTypes => {
        Cell.ofType(cellTypes.headOption.getOrElse("R")).get
      })
  }

  override def create(id: String, controller: Controller): Unit = {
    controller.grid
      .getRows.get
      .map(_.zipWithIndex)
      .zipWithIndex
      .foreach {
        case (row, x) =>
          row.foreach {
            case (cell, y) =>
              create(cell, id, x, y)
          }
      }
    db.run(SlickSchemas.controllers += (id, controller.turn, controller.isOngoing))
  }

  private def create(cell: Cell, controllerId: String, x: Int, y: Int): Unit = {
    db.run(SlickSchemas.cells += (0, controllerId, cell.cellType, x, y))
  }

  private def remove(id: String): Unit = {
    val query = DBIO.seq(
      SlickSchemas.cells.filter(cellObj => cellObj.controllerId === id).delete,
      SlickSchemas.controllers.filter(controllerObj => controllerObj.id === id).delete
    )
    Await.result(
      db.run(query),
      1000 millis
    )
  }
}

object SlickDb {
  def getInstance: ControllerDao = new SlickDb
}
