name := """rota-api"""
organization := "com.russellyeo"
version := "0.1"
scalaVersion := "2.13.10"

// Application configuration
lazy val root = project
  .in(file("."))
  .enablePlugins(PlayScala)
  .settings(
    libraryDependencies ++= Seq(
      guice,
      "com.typesafe.play" %% "play-slick" % "5.0.0",
      "com.typesafe.play" %% "play-slick-evolutions" % "5.0.0",
      "org.postgresql" % "postgresql" % "42.2.12",
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test,
      "com.h2database" % "h2" % "1.4.200" % Test,
      specs2 % Test
    ),
    // https://www.playframework.com/documentation/latest/Deploying#Using-the-sbt-assembly-plugin
    assembly / assemblyJarName := "app.jar",
    assembly / mainClass := Some("play.core.server.ProdServerStart"),
    assembly / fullClasspath += Attributed.blank(PlayKeys.playPackageAssets.value),
    assemblyMergeStrategy in assembly := {
      case PathList("META-INF", xs @ _*) => 
        MergeStrategy.discard
      case manifest if manifest.contains("MANIFEST.MF") =>
        MergeStrategy.discard
      case "module-info.class" => 
        MergeStrategy.discard
      case referenceOverrides if referenceOverrides.contains("reference-overrides.conf") =>
        MergeStrategy.concat
      case x =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
    }
  )

// Test options
Test / javaOptions += "-Dconfig.file=conf/test.conf"

// Packaging into a fat jar
val packageApplication = taskKey[File]("Package the whole application into a fat jar")
packageApplication := {
  val fatJar = (root / assembly).value
  val target = baseDirectory.value / "dist" / "app.jar"
  IO.copyFile(fatJar, target)
  target
}