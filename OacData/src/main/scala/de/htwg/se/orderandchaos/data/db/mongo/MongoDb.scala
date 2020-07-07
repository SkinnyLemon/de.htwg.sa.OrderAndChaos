package de.htwg.se.orderandchaos.data.db.mongo

import de.htwg.se.orderandchaos.data.db.ControllerDao

class MongoDb extends ControllerDao {

}

object SlickDb {
  def getInstance: ControllerDao = new MongoDb
}