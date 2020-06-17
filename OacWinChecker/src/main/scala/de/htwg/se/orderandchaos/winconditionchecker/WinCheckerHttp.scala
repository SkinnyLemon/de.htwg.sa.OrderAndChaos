package de.htwg.se.orderandchaos.winconditionchecker

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.{get, _}
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}

import scala.concurrent.{ExecutionContextExecutor, Future}

class WinCheckerHttp {
  implicit val system: ActorSystem = ActorSystem("my-system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val route: Route = concat(
    get {
      path("wincheck") {
        entity(as[String]) { request =>
          val data: JsValue = Json.parse(request)
          data.validate[Array[Array[String]]] match {
            case JsSuccess(rows, _) =>
              val rowsVector = rows.map(_.toVector).toVector
              val winChecker = WinConditionChecker.get(rowsVector)
              if (winChecker.orderWon) {
                complete("1")
              } else if (winChecker.chaosWon) {
                complete("2")
              } else {
                complete("0")
              }
            case JsError(errors) =>
              System.err.print(errors)
              complete("")
          }

        }
      }
    }
  )

  val bindingFuture: Future[Http.ServerBinding] = Http().bindAndHandle(route, "localhost", 8086)
  println("http://localhost:8086/wincheck online")

  def shutdownWebServer(): Unit = {
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }

}
