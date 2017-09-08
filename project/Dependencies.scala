import sbt._
import bintray.BintrayKeys._

object Dependencies {
  // Versions


  // Libraries
  val brezee = "org.scalanlp" %% "breeze" % "0.13.1"
  val scopt = "com.github.scopt" %% "scopt" % "3.5.0"
  val logging =  "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2"
  val logback = "ch.qos.logback" % "logback-classic" % "1.2.3"
  val toml4j =  "com.moandjiezana.toml" % "toml4j" % "0.7.2"


  //Common Libraries
  val commonLibs = Seq(
    logging,
    logback
  )

  // Projects
  val coreDeps = Seq(brezee) ++ commonLibs
  val consDeps = Seq(scopt, toml4j) ++ commonLibs
}