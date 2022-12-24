import mill._
import $ivy.`com.lihaoyi::mill-contrib-playlib:`, mill.playlib._

object rota extends PlayModule with SingleModule {

  def scalaVersion = "2.13.10"
  def playVersion = "2.8.18"

  object test extends PlayTests
}
