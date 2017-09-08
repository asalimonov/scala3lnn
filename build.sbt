import sbt.Keys.{libraryDependencies, organization, _}
import sbt._
import Dependencies._

scalaVersion := "2.12.2"
name := "scala3lnn"

lazy val commonSettings = Seq(
  version := "0.0.1",
  scalaVersion := "2.12.2",
  organization := "org.scala3lnn",
  test in assembly := {}
)

lazy val core = project.in(file("core"))
  .settings(commonSettings: _*)
  .settings(
    commonSettings,
    name := "scala3lnn.core",
    libraryDependencies ++=  coreDeps,
    assemblyJarName in assembly := "core.jar"
)


lazy val cons = project.in(file("cons")).dependsOn(core % "compile->compile")
  .settings(commonSettings: _*)
  .settings(
    name := "scala3lnn.cons",
    libraryDependencies ++= consDeps,
    mainClass in assembly := Some("org.scala3lnn.cons.Main")
)

/*** lazy val root = (project in file(".")).aggregate(core, cons).settings(
  commonSettings,
  mainClass in assembly := Some("org.scala3lnn.cons.Main"),
  assemblyMergeStrategy in assembly := {
  case PathList("org", "scala3lnn", xs @ _*) => MergeStrategy.last
  case PathList("META-INF", xs @ _*) =>
      (xs map {_.toLowerCase}) match {
        case ("manifest.mf" :: Nil) | ("index.list" :: Nil) | ("dependencies" :: Nil) =>
          MergeStrategy.discard
        case ps @ (x :: xs) if ps.last.endsWith(".sf") || ps.last.endsWith(".dsa") =>
          MergeStrategy.discard
        case "plexus" :: xs =>
          MergeStrategy.discard
        case "services" :: xs =>
          MergeStrategy.filterDistinctLines
        case ("spring.schemas" :: Nil) | ("spring.handlers" :: Nil) =>
          MergeStrategy.filterDistinctLines
        case _ => MergeStrategy.deduplicate
      }
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}
)
*/

resolvers += "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/"
resolvers += Resolver.bintrayRepo("jvican", "releases")