package de.htwg.se.orderandchaos.ui.tui

import com.google.inject.Inject
import de.htwg.se.orderandchaos.data.control.{CellSet, Win}
import de.htwg.se.orderandchaos.data.model.{CommandParsingException, OacException}
import de.htwg.se.orderandchaos.session.SessionHandler

import scala.annotation.tailrec
import scala.io.StdIn.readLine
import scala.swing.Reactor
import scala.util.{Failure, Success, Try}

//noinspection ScalaStyle
final class TUI(@Inject sessions: SessionHandler) extends Reactor {
  listenTo(sessions.eventProvider)
  var stop = false
  private var id = sessions.startSession()
  private var control = sessions(id)
  private var interpreter = new CommandTranslator(control)
  println("Welcome to Order and Chaos! Type \"help\" to display the available commands.")
  println(control.toString)

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
          case set if set startsWith "set " => interpreter.interpretSet(set.substring(4))
          case "undo" => control.undo()
          case "redo" => control.redo()
          case "reset" => control.reset()
          case "save" => control.save()
          case "load" => control.load()
          case "print" => println(interpreter.makeColorString)
          case "new" =>
            changeSession(sessions.startSession())
            println(this.id + "\n" + interpreter.makeColorString)
          case switch if switch startsWith "switch " => Try(sessions(switch.substring(7))) match {
            case Success(_) =>
              changeSession(switch.substring(7))
              println(this.id + "\n" + interpreter.makeColorString)
            case Failure(_) => println("Can't find game with that id")
          }
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
      println(id + "\n" + interpreter.makeColorString)
    case win: Win =>
      println(s"Player ${win.player} won the game!")
      println(id + "\n" + interpreter.makeColorString)
  }

  private def changeSession(newId: String): Unit = {
    id = newId
    control = sessions(newId)
    interpreter = new CommandTranslator(control)
  }
}
