package de.htwg.se.orderandchaos.data.db.mongo

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.mongodb.BasicDBObject
import de.htwg.se.orderandchaos.data.control.controller.Controller
import de.htwg.se.orderandchaos.data.db.ControllerDao
import de.htwg.se.orderandchaos.data.model.cell.Cell
import de.htwg.se.orderandchaos.data.model.grid.Grid

import scala.concurrent.duration._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.{Document, MongoClient, MongoCollection, MongoDatabase}

import scala.concurrent.{Await, ExecutionContextExecutor, Future}

class MongoDb extends ControllerDao {
  implicit val system: ActorSystem = ActorSystem("my-system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val client: MongoClient = MongoClient("mongodb://mongo:27017")
  val database: MongoDatabase = client.getDatabase("oac")
  val collection: MongoCollection[Document] = database.getCollection("controller")


  override def create(id: String, controller: Controller): Unit = {
    println(s"Writing $id to MongoDb")
    val turn = controller.turn
    val ongoing = controller.isOngoing
    val grid = controller.grid
      .getRows.get
      .map(_.zipWithIndex)
      .zipWithIndex
      .map {
        case (row, x) =>
          row.map {
            case (cell, y) =>
              s"$x$y${cell.cellType}"
          }
      }.map(_.toList)
      .toList

    val toInsert = Document(
      "id" -> id,
      "turn" -> turn,
      "ongoing" -> ongoing,
      "grid" -> grid
    )
    Await.result(collection.insertOne(toInsert).toFuture(), 5 seconds)
  }

  override def read(id: String): Future[Controller] = {
    println(s"Reading $id from MongoDb")
    collection.find(equal("id", id)).first().toFuture()
      .map(controllerObj => {
        val turn = controllerObj.get("turn").get.asString().getValue
        val ongoing = controllerObj.get("ongoing").get.asBoolean().getValue
        val gridArray = Array.ofDim[Cell](Grid.WIDTH, Grid.WIDTH)
        controllerObj.get("grid").get
          .asArray()
          .forEach(_.asArray()
            .forEach(cell => {
              val cellValue = cell.asString().getValue
              val x = cellValue.substring(0, 1).toInt
              val y = cellValue.substring(1, 2).toInt
              val cellType = cellValue.charAt(2).toString
              gridArray(x)(y) = Cell.ofType(cellType).get
            })
          )
        val gridSeq = gridArray.map(_.toSeq).toSeq
        val grid = Grid.fromSeq(gridSeq)
        if (ongoing)
          Controller.getOngoing(grid, turn)
        else
          Controller.getFinished(grid, turn)
      })
  }
}

object MongoDb {
  def getInstance: ControllerDao = new MongoDb
}

private case class MongoController(id: String, grid: List[List[String]])