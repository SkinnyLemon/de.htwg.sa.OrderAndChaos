package de.htwg.se.orderandchaos.data.control

import de.htwg.se.orderandchaos.data.control.controller.TestController
import de.htwg.se.orderandchaos.data.model.NoMoreMovesException
import de.htwg.se.orderandchaos.data.model.cell.Cell
import de.htwg.se.orderandchaos.util.ExceptionChecker
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class ControlSpec extends WordSpec with Matchers {
  "A Control" should {
    val startController = new TestController
    "play a red cell" in {
      val control: Control = new ControlImpl("", startController)
      control.playRed(0, 1)
      val newController = control.controller.asInstanceOf[TestController]
      newController.lastX should be(0)
      newController.lastY should be(1)
      newController.playCalls should be(1)
      newController.redCalls should be(1)
    }
    "play a blue cell" in {
      val control: Control = new ControlImpl("", startController)
      control.playBlue(2, 3)
      val newController = control.controller.asInstanceOf[TestController]
      newController.lastX should be(2)
      newController.lastY should be(3)
      newController.playCalls should be(1)
      newController.blueCalls should be(1)
    }
    "play a cell" in {
      val control: Control = new ControlImpl("", startController)
      control.play(2, 3, Cell.TYPE_EMPTY)
      val newController = control.controller.asInstanceOf[TestController]
      newController.lastX should be(2)
      newController.lastY should be(3)
      newController.playCalls should be(1)
      newController.redCalls should be(0)
      newController.blueCalls should be(0)
    }
    "undo a turn" in {
      val control: Control = new ControlImpl("", startController)
      control.play(2, 3, Cell.TYPE_EMPTY)
      control.play(4, 5, Cell.TYPE_BLUE)
      control.undo()
      val newController = control.controller.asInstanceOf[TestController]
      newController.lastX should be(2)
      newController.lastY should be(3)
      newController.playCalls should be(1)
      newController.redCalls should be(0)
      newController.blueCalls should be(0)
    }
    "redo a turn" in {
      val control: Control = new ControlImpl("", startController)
      control.play(2, 3, Cell.TYPE_EMPTY)
      control.play(4, 5, Cell.TYPE_BLUE)
      control.undo()
      control.redo()
      val newController = control.controller.asInstanceOf[TestController]
      newController.lastX should be(4)
      newController.lastY should be(5)
      newController.playCalls should be(2)
      newController.redCalls should be(0)
      newController.blueCalls should be(1)
    }
    "limit undos" in {
      val control: Control = new ControlImpl("", startController)
      ExceptionChecker.checkTry[NoMoreMovesException](control.undo(), "Did too many undos")
      control.play(4, 5, Cell.TYPE_BLUE)
      control.undo()
      ExceptionChecker.checkTry[NoMoreMovesException](control.undo(), "Did too many undos")
    }
    "limit redos" in {
      val control: Control = new ControlImpl("", startController)
      ExceptionChecker.checkTry[NoMoreMovesException](control.redo(), "Did too many redos")
      control.play(4, 5, Cell.TYPE_BLUE)
      control.undo()
      control.redo()
      ExceptionChecker.checkTry[NoMoreMovesException](control.redo(), "Did too many redos")
    }
    "reset" in {
      val control: Control = new ControlImpl("", startController)
      control.play(2, 3, Cell.TYPE_EMPTY)
      control.play(4, 5, Cell.TYPE_BLUE)
      control.reset()
      control.controller should be(startController)
    }
  }
}
