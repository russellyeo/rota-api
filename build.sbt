import com.typesafe.sbt.packager.docker._

name := """rota"""
organization := "com.russellyeo"
version := "0.1"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala, DockerPlugin)

scalaVersion := "2.13.10"

libraryDependencies ++= Seq(
  guice,
  "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test,
  "com.typesafe.play" %% "play-slick" % "5.0.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "5.0.0",
  "com.h2database" % "h2" % "1.4.200",
  specs2 % Test
)

Test / javaOptions += "-Dconfig.file=conf/test.conf"

dockerBaseImage := "openjdk:11-jre-slim"
dockerUpdateLatest := true
dockerExposedPorts := Seq(9000)
dockerChmodType := DockerChmodType.UserGroupWriteExecute
dockerPermissionStrategy := DockerPermissionStrategy.CopyChown

// Cross-build Docker images (e.g. build on apple-silicon/aarch64 and publish for linux/amd64)
// https://github.com/sbt/sbt-native-packager/issues/1503
dockerBuildCommand := {
  if (sys.props("os.arch") != "amd64") {
    // use buildx with platform to build supported amd64 images on other CPU architectures
    // this may require that you have first run 'docker buildx create' to set docker buildx up
    dockerExecCommand.value ++ Seq(
      "buildx",
      "build",
      "--platform=linux/amd64",
      "--load"
    ) ++ dockerBuildOptions.value :+ "."
  } else dockerBuildCommand.value
}
// Once we have updated sbt-native-packager to 1.9.13, we can use the following instead:
// dockerBuildxPlatforms := Seq("linux/arm64/v8", "linux/amd64")
