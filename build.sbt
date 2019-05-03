import com.scalapenos.sbt.prompt._

///////////////////////////////////////////////////////////////////////////////////////////////////
// Settings
///////////////////////////////////////////////////////////////////////////////////////////////////

lazy val commonSettings = Seq(
  organization := "me.milanvdm",
  scalaVersion := "2.12.8",
  resolvers ++= Seq(
    "Typesafe Releases" at "http://repo.typesafe.com/typesafe/maven-releases/"
  ),
  scalafmtOnCompile := true,
  incOptions := incOptions.value.withLogRecompileOnMacro(false),
  scalacOptions ++= commonScalacOptions,
  fork in Test := true,
  parallelExecution in Test := false,
  testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oDF"),
  libraryDependencies ++= Seq(
    compilerPlugin(D.kindProjector),
    compilerPlugin(D.macroParadise)
  ),
  scalacOptions in (Compile, doc) := (scalacOptions in (Compile, doc)).value.filter(_ != "-Xfatal-warnings"),
  promptTheme := PromptTheme(
    List(
      text("[SBT] ", fg(136)),
      currentProject(fg(64)).padRight(": ")
    )
  )
)

lazy val commonScalacOptions = Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-language:experimental.macros",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xfuture",
  "-Xlint",
  "-Xlog-reflective-calls",
  "-Ydelambdafy:method",
  "-Yno-adapted-args",
  "-Ypartial-unification",
  "-Ywarn-dead-code",
  "-Ywarn-value-discard",
  "-Ywarn-unused-import",
  "-Ywarn-inaccessible"
)

lazy val dockerSettings = Seq(
  name := "forex-proxy",
  dockerBaseImage := "openjdk:jre-alpine",
  packageName in Docker := name.value,
  version in Docker := version.value
)

lazy val noPublishSettings = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false
)

///////////////////////////////////////////////////////////////////////////////////////////////////
// Dependencies
///////////////////////////////////////////////////////////////////////////////////////////////////

lazy val D = new {

  val Versions = new {
    val cats = "1.6.0"
    val catsEffect = "1.3.0"
    val catsPar = "0.2.1"
    val enumeratum = "1.5.13"
    val fs2 = "1.0.4"
    val http4s = "0.20.0"
    val circe = "0.11.1"
    val pureConfig = "0.10.2"

    // Test
    val scalaCheck = "1.14.0"
    val scalaTest = "3.0.7"
    val catsScalaCheck = "0.1.1"

    // Compiler
    val kindProjector = "0.9.10"
    val macroParadise = "2.1.1"
  }

  val cats = "org.typelevel"               %% "cats-core"            % Versions.cats
  val catsEffect = "org.typelevel"         %% "cats-effect"          % Versions.catsEffect
  val catsPar = "io.chrisdavenport"        %% "cats-par"             % Versions.catsPar
  val circe = "io.circe"                   %% "circe-core"           % Versions.circe
  val circeGeneric = "io.circe"            %% "circe-generic"        % Versions.circe
  val circeGenericExtras = "io.circe"      %% "circe-generic-extras" % Versions.circe
  val circeJava8 = "io.circe"              %% "circe-java8"          % Versions.circe
  val circeParser = "io.circe"             %% "circe-parser"         % Versions.circe
  val enumeratum = "com.beachape"          %% "enumeratum"           % Versions.enumeratum
  val enumeratumCirce = "com.beachape"     %% "enumeratum-circe"     % Versions.enumeratum
  val fs2 = "co.fs2"                       %% "fs2-core"             % Versions.fs2
  val http4sServer = "org.http4s"          %% "http4s-blaze-server"  % Versions.http4s
  val http4sCirce = "org.http4s"           %% "http4s-circe"         % Versions.http4s
  val http4sClient = "org.http4s"          %% "http4s-blaze-client"  % Versions.http4s
  val http4sDsl = "org.http4s"             %% "http4s-dsl"           % Versions.http4s
  val pureConfig = "com.github.pureconfig" %% "pureconfig"           % Versions.pureConfig

  // Test
  val scalaTest = "org.scalatest"          %% "scalatest"       % Versions.scalaTest
  val scalaCheck = "org.scalacheck"        %% "scalacheck"      % Versions.scalaCheck
  val catsScalaCheck = "io.chrisdavenport" %% "cats-scalacheck" % Versions.catsScalaCheck

  // Compiler
  val kindProjector = "org.spire-math"  %% "kind-projector" % Versions.kindProjector
  val macroParadise = "org.scalamacros" %% "paradise"       % Versions.macroParadise cross CrossVersion.full
}

///////////////////////////////////////////////////////////////////////////////////////////////////
// Projects
///////////////////////////////////////////////////////////////////////////////////////////////////

lazy val `forex-proxy` = Project(
  id = "forex-proxy",
  base = file(".")
).settings(moduleName := "forex-proxy")
  .settings(commonSettings)
  .settings(noPublishSettings)
  .aggregate(core)
  .dependsOn(core)

lazy val core = Project(
  id = "core",
  base = file("core")
).settings(moduleName := "core")
  .settings(commonSettings)
  .settings(dockerSettings)
  .settings(Revolver.settings)
  .settings(
    libraryDependencies ++= Seq(
      D.cats,
      D.catsEffect,
      D.catsPar,
      D.circe,
      D.circeGeneric,
      D.circeGenericExtras,
      D.circeJava8,
      D.circeParser,
      D.enumeratum,
      D.enumeratumCirce,
      D.fs2,
      D.http4sServer,
      D.http4sCirce,
      D.http4sClient,
      D.http4sDsl,
      D.pureConfig,
      D.scalaTest % "it,test"
    )
  )
  .configs(IntegrationTest extend Test)
  .settings(Defaults.itSettings)
  .settings(
    fork in IntegrationTest := true,
    parallelExecution in IntegrationTest := false,
    inConfig(IntegrationTest)(ScalafmtPlugin.scalafmtConfigSettings)
  )

///////////////////////////////////////////////////////////////////////////////////////////////////
// Commands
///////////////////////////////////////////////////////////////////////////////////////////////////

addCommandAlias("update", ";dependencyUpdates")
addCommandAlias("fcompile", ";scalafmtSbt;compile;it:compile;test:compile")

///////////////////////////////////////////////////////////////////////////////////////////////////
// Plugins
///////////////////////////////////////////////////////////////////////////////////////////////////

enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)
