package de.htwg.se.orderandchaos.ui

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpEntity, HttpMethod, HttpMethods, HttpRequest, HttpResponse}
import akka.http.scaladsl.server.Directives.{get, _}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import de.htwg.se.orderandchaos.ui.util.{CellSet, Win}
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.swing.Publisher
import scala.util.{Failure, Success}

trait GameController extends Publisher {
  def startInstance(): Future[String]

  def stopInstance(id: String): Future[Unit]

  def saveGame(id: String): Future[Unit]

  def loadGame(id: String): Future[Unit]

  def resetGame(id: String): Future[Unit]

  def play(id: String, x: Int, y: Int, color: String): Future[Unit]
}

class UiHttp extends GameController {
  implicit val system: ActorSystem = ActorSystem("my-system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val route: Route = concat(
    get {
      path("ui" / "update") {
        entity(as[String]) { request =>
          val jsValue: JsValue = Json.parse(request)
          (jsValue \ "id").validate[String] match {
            case JsSuccess(id, _) =>
              (jsValue \ "data").validate[Array[Array[String]]] match {
                case JsSuccess(rows, _) =>
                  publish(new CellSet(id, rows))
                case JsError(errors) =>
                  System.err.print(errors)
              }
            case JsError(errors) =>
              System.err.print(errors)
          }
          complete("")
        }
      }
    },
    get {
      path("ui" / "win") {
        entity(as[String]) { request =>
          val jsValue: JsValue = Json.parse(request)
          (jsValue \ "id").validate[String] match {
            case JsSuccess(id, _) =>
              (jsValue \ "data").validate[Array[Array[String]]] match {
                case JsSuccess(rows, _) =>
                  (jsValue \ "winner").validate[String] match {
                    case JsSuccess(winner, _) =>
                  publish(new Win(id, rows, winner))
                    case JsError(errors) =>
                      System.err.print(errors)
                  }
                case JsError(errors) =>
                  System.err.print(errors)
              }
            case JsError(errors) =>
              System.err.print(errors)
          }
          complete("")
        }
      }
    }
  )

  val bindingFuture: Future[Http.ServerBinding] = Http().bindAndHandle(route, "ui", 8084)
  println("http://ui:8084/ui online")

  def shutdownWebServer(): Unit = {
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }

  override def startInstance(): Future[String] = {
    send("http://data:8085/data/new")
  }

  override def stopInstance(id: String): Future[Unit] = {
    send("http://data:8085/data/end", id)
      .map(_ => Unit)
  }

  override def saveGame(id: String): Future[Unit] = {
    send("http://data:8085/data/save", id)
      .map(_ => Unit)
  }

  override def loadGame(id: String): Future[Unit] = {
    send("http://data:8085/data/load", id)
      .map(_ => Unit)
  }

  override def resetGame(id: String): Future[Unit] = {
    send("http://data:8085/data/reset", id)
      .map(_ => Unit)
  }

  override def play(id: String, x: Int, y: Int, color: String): Future[Unit] = {
    val jsValue = Json.obj(
      "id" -> id,
      "x" -> x,
      "y" -> y,
      "color" -> color
    )
    val toSend = Json.stringify(jsValue)
    send("http://data:8085/data/play", toSend, HttpMethods.PUT)
      .map(_ => Unit)
  }

  private def send(uri: String, id: String = "", method: HttpMethod = HttpMethods.GET): Future[String] = {
    val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = uri, entity = HttpEntity(id), method = method))
    responseFuture.onComplete {
      case Success(res) => res
      case Failure(e) => e.printStackTrace()
    }
    responseFuture.flatMap(Unmarshal(_).to[String])
  }
}
