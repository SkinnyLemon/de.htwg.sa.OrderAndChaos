package de.htwg.se.orderandchaos.winconditionchecker

import de.htwg.se.orderandchaos.data.model.grid.Grid

class TestWinConditionChecker(returnValue: Int = 0) extends WinConditionChecker {
  override def winningLineExists(grid: Grid): Boolean = returnValue == 1

  override def noWinningLinePossible(grid: Grid): Boolean = returnValue == 2
}
