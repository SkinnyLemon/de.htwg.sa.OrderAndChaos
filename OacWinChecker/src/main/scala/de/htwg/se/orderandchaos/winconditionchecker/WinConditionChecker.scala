package de.htwg.se.orderandchaos.winconditionchecker

import de.htwg.se.orderandchaos.data.model.cell.Cell
import de.htwg.se.orderandchaos.data.model.grid.Grid

import scala.annotation.tailrec
import scala.util.{Failure, Success, Try}

trait WinConditionChecker {
  def orderWon: Boolean

  def chaosWon: Boolean
}

private class WinConditionCheckerImpl(values: Vector[Vector[String]]) extends WinConditionChecker {
  private val blueLine = Cell.TYPE_BLUE * WinConditionChecker.WINNINGSTREAK
  private val redLine = Cell.TYPE_RED * WinConditionChecker.WINNINGSTREAK

  def orderWon: Boolean = winningLineExists(values)

  def chaosWon: Boolean =
    winningLineExists(convertEmptyFields(values, "B")) ||
      winningLineExists(convertEmptyFields(values, "R"))

  private def winningLineExists(toCheck: Vector[Vector[String]]): Boolean =
    checkLines(values) ||
      checkLines(columns(values)) ||
      checkLines(upDiagonals(values)) ||
      checkLines(downDiagonals(values))

  private def checkLines(in: Vector[Vector[String]]): Boolean = in.exists(checkForWinningLine)

  private def checkForWinningLine(input: Vector[String]): Boolean = {
    val line = input.mkString("")
    line.contains(blueLine) || line.contains(redLine)
  }

  private def convertEmptyFields(rows: Vector[Vector[String]], toSet: String): Vector[Vector[String]] = rows.map(_.map {
    case "E" => toSet
    case other: String => other
  })


  private def columns(rows: Vector[Vector[String]]): Vector[Vector[String]] = getLinesOverVal(0, x => getColumn(rows, x))

  private def upDiagonals(rows: Vector[Vector[String]]): Vector[Vector[String]] = getDiagonals(
    x => getUpDiagonal(rows, x, 0),
    y => getUpDiagonal(rows, 0, y))

  private def downDiagonals(rows: Vector[Vector[String]]): Vector[Vector[String]] = getDiagonals(
    x => getDownDiagonal(rows, x, Grid.WIDTH - 1),
    y => getDownDiagonal(rows, 0, y)
  )

  private def getDiagonals(buildDiagonalX: Int => Vector[String], buildDiagonalY: Int => Vector[String]): Vector[Vector[String]] = {
    getLinesOverVal(0, buildDiagonalX) ++
      getLinesOverVal(1, buildDiagonalY)
  }

  private def getLinesOverVal(start: Int, method: Int => Vector[String]): Vector[Vector[String]] = {
    @tailrec
    def getLinesOverValRec(value: Int, sum: Vector[Vector[String]] = Vector.empty): Vector[Vector[String]] =
      if (value < Grid.WIDTH) {
        val diagonal = method(value)
        getLinesOverValRec(value + 1, sum :+ diagonal)
      } else {
        sum
      }
    getLinesOverValRec(start)
  }


  private def getColumn(rows: Vector[Vector[String]], x: Int): Vector[String] = rows.map(row => row(x))

  private def getUpDiagonal(rows: Vector[Vector[String]], xStart: Int, yStart: Int): Vector[String] = getDiagonal(rows, xStart, yStart, 1)

  private def getDownDiagonal(rows: Vector[Vector[String]], xStart: Int, yStart: Int): Vector[String] = getDiagonal(rows, xStart, yStart, -1)

  private def getDiagonal(rows: Vector[Vector[String]], xStart: Int, yStart: Int, deltaY: Int): Vector[String] = {
    @tailrec
    def getDiagonalPart(x: Int, y: Int, fields: Vector[String]): Vector[String] = {
      if (x >= Grid.WIDTH || y >= Grid.WIDTH || x < 0 || y < 0) {
        fields
      } else {
        getDiagonalPart(x + 1, y + deltaY, fields :+ rows(x)(y))
      }
    }
    getDiagonalPart(xStart, yStart, Vector.empty)
  }
}

object WinConditionChecker {
  val WINNINGSTREAK = 5

  def get(rows: Vector[Vector[String]]): WinConditionChecker = new WinConditionCheckerImpl(rows)
}