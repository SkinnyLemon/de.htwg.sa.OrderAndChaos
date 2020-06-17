package de.htwg.se.orderandchaos.ui.tui

import de.htwg.se.orderandchaos.ui.GameController
import de.htwg.se.orderandchaos.ui.util.{CommandParsingException, InvalidCellTypeException}

import scala.io.AnsiColor.{BLUE, RED, RESET}
import scala.util.{Failure, Success, Try}

class CommandTranslator(controller: GameController) {
  private val width = 6

  def colorCell(color: String): String = color match {
    case "B" => s"${BLUE}B$RESET"
    case "R" => s"${RED}R$RESET"
    case "E" => "E"
    case s => new IllegalArgumentException("Cell did not match known types").printStackTrace(); s
  }

  def interpretSet(input: String, id: String): Try[Unit] = {
    val values = input.split(" ")
    (if (values.length != 2) {
      Failure(new CommandParsingException("Both coordinates and the field type need to be set!"))
    } else {
      buildCoordinates(values(0)).map(coordinates => controller.play(id, coordinates(0), coordinates(1), values(1)))
    }) match {
      case Failure(e: CommandParsingException) => Failure(new CommandParsingException(s"${e.getMessage} - Usage: ${CommandTranslator.setInstruction}"))
      case Failure(_: InvalidCellTypeException) => Failure(new CommandParsingException(
        s"The field type was invalid! types: B, R - Usage: ${CommandTranslator.setInstruction}"))
      case Failure(e) => Failure(e)
      case Success(_) => Success()
    }
  }

  private def buildCoordinates(input: String): Try[Array[Int]] = {
    val values = input.split(",")
    if (values.length != 2) {
      Failure(new CommandParsingException("X and Y value required to set a field!"))
    } else {
      Try(values.map(_.toInt)) match {
        case Success(coordinates) =>
          if (coordinates(0) > width || coordinates(0) < 1
            || coordinates(1) > width || coordinates(0) < 1) {
            Failure(new CommandParsingException(s"The coordinates need to be between 1 and $width"))
          } else {
            Success(coordinates)
          }
        case Failure(_) => Failure(new CommandParsingException("X and Y value need to be numbers"))
      }
    }

  }
}

object CommandTranslator {
  val setInstruction: String = "set X_VALUE,Y_VALUE CELL_TYPE"
}
