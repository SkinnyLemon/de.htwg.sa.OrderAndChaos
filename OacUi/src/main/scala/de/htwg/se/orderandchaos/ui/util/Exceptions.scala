package de.htwg.se.orderandchaos.ui.util

class OacException(message: String) extends IllegalArgumentException(message)

class CommandParsingException(message: String) extends OacException(message)

class InvalidCellTypeException extends OacException("Invalid cell type")