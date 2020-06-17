package de.htwg.se.orderandchaos.ui.tui

import de.htwg.se.orderandchaos.ui.GameController
import de.htwg.se.orderandchaos.ui.util.{CellSet, CommandParsingException, OacException, Win}

import scala.annotation.tailrec
import scala.io.StdIn.readLine
import scala.swing.Reactor
import scala.util.Failure

//noinspection ScalaStyle
final class TUI(controller: GameController) extends Reactor {
  listenTo(controller)
  controller.startInstance()
  var stop = false
  private var id: String = ""
  private val interpreter = new CommandTranslator(controller)
  println("Welcome to Order and Chaos! Type \"help\" to display the available commands.")

  @tailrec
  def interpretLines(): Unit = {
    readLine() match {
      case "help" => println("The available commands are: set, print, undo, redo, q. To learn more about them type them with a -h")
      case "set -h" => println(s"This command sets a field to the specified type. Only empty fields can be set. Use like this:\n${CommandTranslator.setInstruction}")
      case "print -h" => println("This command prints out the current board")
      case "undo -h" => println("This undoes the last move made and saves it to redo later")
      case "redo -h" => println("This redoes the last move that was undone")
      case "q -h" => println(s"This command ends the game")
      case "q" =>
        println("Ending game")
        return
      case command =>
        if (stop) {
          println("Game ended!")
          return
        }
        (command match {
          case set if set startsWith "set " => interpreter.interpretSet(set.substring(4), id)
          //          case "undo" => controller.undo()
          //          case "redo" => controller.redo()
          case "reset" => controller.resetGame(id)
          case "save" => controller.saveGame(id)
          case "load" => controller.loadGame(id)
          //          case "print" => println(interpreter.makeColorString)
          case "new" =>
            controller.startInstance()
          //          case switch if switch startsWith "switch " => Try(sessions(switch.substring(7))) match {
          //            case Success(_) =>
          //              changeSession(switch.substring(7))
          //              println(this.id + "\n" + interpreter.makeColorString)
          //            case Failure(_) => println("Can't find game with that id")
          //          }
          case _ => Failure(new CommandParsingException("Unrecognized command!"))
        }) match {
          case Failure(e: CommandParsingException) => println(s"Error parsing command: ${e.getMessage}")
          case Failure(e: OacException) => println(s"Error executing command: ${e.getMessage}")
          case Failure(e) => throw e
          case _ =>
        }
    }
    if (stop) {
      println("Game ended!")
      return
    }
    interpretLines()
  }

  reactions += {
    case ev: CellSet =>
      changeSession(ev.sessionId)
      printGame(id, ev.rows)
    case win: Win =>
      println(s"Player ${win.winner} won the game!")
      printGame(id, win.rows)
  }

  private def printGame(id: String, rows: Array[Array[String]]): Unit = {
    val stringRepresentation = rows.map(_.map(interpreter.colorCell).mkString(" ")).mkString("\n")
    println(s"$id\n$stringRepresentation")
  }

  private def changeSession(newId: String): Unit = {
    id = newId
  }
}
