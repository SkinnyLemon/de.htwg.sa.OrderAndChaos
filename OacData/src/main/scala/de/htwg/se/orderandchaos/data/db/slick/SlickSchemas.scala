package de.htwg.se.orderandchaos.data.db.slick

import slick.jdbc.H2Profile.api._

class ControllerSlick(tag: Tag) extends Table[(String, String, Boolean)](tag, "CONTROLLERS") {
  def id = column[String]("ID", O.PrimaryKey)

  def turn = column[String]("TURN")

  def ongoing = column[Boolean]("ONGOING")

  def * = (id, turn, ongoing)
}

class CellSlick(tag: Tag) extends Table[(Int, String, String, Int, Int)](tag, "CELLS") {
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)

  def controllerId = column[String]("CONTROLLER_ID")

  def cellType = column[String]("CELL_TYPE")

  def x = column[Int]("X")

  def y = column[Int]("Y")

  def * = (id, controllerId, cellType, x, y)

  //def controller = foreignKey("CONTROLLER_FK", controllerId, SlickSchemas.controllers) _
}

object SlickSchemas {
  val controllers = TableQuery[ControllerSlick]
  val cells = TableQuery[CellSlick]
}