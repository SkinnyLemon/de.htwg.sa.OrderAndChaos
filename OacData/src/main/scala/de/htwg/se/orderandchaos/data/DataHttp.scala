package de.htwg.se.orderandchaos.data

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpEntity, HttpRequest, HttpResponse}
import akka.http.scaladsl.server.Directives.{get, _}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import de.htwg.se.orderandchaos.data.control.SessionHandler
import de.htwg.se.orderandchaos.data.control.controller.Controller
import de.htwg.se.orderandchaos.data.model.grid.Grid
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

object WinChecker {
  val NONE: Int = 0
  val ORDER: Int = 1
  val CHAOS: Int = 2
}

trait WinChecker {
  def determineWinner(grid: Grid): Future[Int]
}

trait GridUpdater {
  def updateBoard(id: String, controller: Controller): Unit

  def announceWin(id: String, controller: Controller, winner: String): Unit
}

//noinspection ScalaStyle
class DataHttp extends WinChecker with GridUpdater {
  implicit val system: ActorSystem = ActorSystem("my-system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  private val sessionHandler = new SessionHandler(this, this)

  val route: Route = concat(
    get {
      path("data" / "new") {
        complete(sessionHandler.startSession())
      }
    },
    get {
      path("data" / "end") {
        entity(as[String]) { id =>
          sessionHandler.endSession(id)
          complete("")
        }
      }
    },
    get {
      path("data" / "save") {
        entity(as[String]) { id =>
          sessionHandler(id).save()
          complete("")
        }
      }
    },
    get {
      path("data" / "load") {
        entity(as[String]) { id =>
          sessionHandler(id).load()
          complete("")
        }
      }
    },
    get {
      path("data" / "reset") {
        entity(as[String]) { id =>
          sessionHandler(id).reset()
          complete("")
        }
      }
    },
    put {
      path("data" / "play") {
        entity(as[String]) { request =>
          val jsValue: JsValue = Json.parse(request)
          handlePlay(jsValue)
          complete("")
        }
      }
    }
  )

  private def handlePlay(jsValue: JsValue): Unit = {
    val id = (jsValue \ "id").validate[String] match {
      case JsSuccess(id, _) => id
      case JsError(errors) =>
        System.err.print(errors)
        return
    }
    val x = (jsValue \ "x").validate[Int] match {
      case JsSuccess(id, _) => id
      case JsError(errors) =>
        System.err.print(errors)
        return
    }
    val y = (jsValue \ "y").validate[Int] match {
      case JsSuccess(id, _) => id
      case JsError(errors) =>
        System.err.print(errors)
        return
    }
    val color = (jsValue \ "color").validate[String] match {
      case JsSuccess(id, _) => id
      case JsError(errors) =>
        System.err.print(errors)
        return
    }
    sessionHandler(id).play(x, y, color) match {
      case Failure(e) => e.printStackTrace()
      case _ =>
    }
  }

  val bindingFuture: Future[Http.ServerBinding] = Http().bindAndHandle(route, "localhost", 8085)
  println("http://localhost:8085/data online")

  def shutdownWebServer(): Unit = {
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }

  override def determineWinner(grid: Grid): Future[Int] = {
    send(convertGridToJsonArray(grid), "http://localhost:8086/wincheck")
      .map(_.toInt)
  }

  override def updateBoard(id: String, controller: Controller): Unit = {
    val jsValue = convertGridToJsonArray(controller.grid)
    val toSend = Json.obj("id" -> id, "data" -> jsValue)
    send(toSend, "http://localhost:8084/ui/update")
  }

  override def announceWin(id: String, controller: Controller, winner: String): Unit = {
    val jsValue = convertGridToJsonArray(controller.grid)
    val toSend = Json.obj("id" -> id, "data" -> jsValue, "winner" -> winner)
    send(toSend, "http://localhost:8084/ui/win")
  }

  private def send(json: JsValue, uri: String): Future[String] = {
    val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = uri, entity = HttpEntity(Json.stringify(json))))
    responseFuture.onComplete {
      case Success(res) => res
      case Failure(e) => e.printStackTrace()
    }
    responseFuture.flatMap(Unmarshal(_).to[String])
  }

  private def convertGridToJsonArray(grid: Grid): JsValue = {
    val array: Array[Array[String]] = grid.getRows.get.map(_.map(_.cellType).toArray).toArray
    Json.toJson(array)
  }
}
