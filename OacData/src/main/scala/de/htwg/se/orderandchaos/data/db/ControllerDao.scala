package de.htwg.se.orderandchaos.data.db

import de.htwg.se.orderandchaos.data.control.controller.Controller
import de.htwg.se.orderandchaos.data.db.mongo.MongoDb

import scala.concurrent.Future

trait ControllerDao {
  def create(id: String, controller: Controller): Unit

  def read(id: String): Future[Controller]
}

object ControllerDao {
  def getInstance: ControllerDao = MongoDb.getInstance
}