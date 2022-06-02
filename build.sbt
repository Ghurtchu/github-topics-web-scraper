ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.1.2"

libraryDependencies += "dev.zio" %% "zio" % "1.0.12"
libraryDependencies += "com.softwaremill.sttp.client3" %% "core" % "3.6.2"
libraryDependencies += "org.jsoup" % "jsoup" % "1.15.1"
libraryDependencies += "com.github.tototoshi" %% "scala-csv" % "1.3.10"

lazy val root = (project in file("."))
  .settings(
    name := "ScalaJobsScraper"
  )
