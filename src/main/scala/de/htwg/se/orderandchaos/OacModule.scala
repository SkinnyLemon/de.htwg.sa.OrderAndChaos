package de.htwg.se.orderandchaos

import com.google.inject.AbstractModule
import de.htwg.se.orderandchaos.control.game.file.FileManager
import de.htwg.se.orderandchaos.control.game.file.xml.XmlFileManager
import de.htwg.se.orderandchaos.control.session.SessionHandler
import net.codingwell.scalaguice.ScalaModule

class OacModule extends AbstractModule with ScalaModule {
  override def configure(): Unit = {
    val fileManager = new XmlFileManager
    val sessions = new SessionHandler
    bind[FileManager].toInstance(fileManager)
    bind[SessionHandler].toInstance(sessions)
  }
}
