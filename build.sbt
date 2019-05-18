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

///////////////////////////////////////////////////////////////////////////////////////////////////
// Dependencies
///////////////////////////////////////////////////////////////////////////////////////////////////

lazy val D = new {

  val Versions = new {
    val cats = "1.6.0"
    val catsEffect = "1.3.0"
    val catsPar = "0.2.1"
    val circe = "0.11.1"
    val enumeratum = "1.5.13"
    val enumeratumCirce = "1.5.21"
    val fs2 = "1.0.4"
    val http4s = "0.20.1"
    val logback = "1.2.3"
    val pureConfig = "0.11.0"

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
  val circeLiteral = "io.circe"            %% "circe-literal"        % Versions.circe
  val circeParser = "io.circe"             %% "circe-parser"         % Versions.circe
  val enumeratum = "com.beachape"          %% "enumeratum"           % Versions.enumeratum
  val enumeratumCirce = "com.beachape"     %% "enumeratum-circe"     % Versions.enumeratumCirce
  val fs2 = "co.fs2"                       %% "fs2-core"             % Versions.fs2
  val http4sServer = "org.http4s"          %% "http4s-blaze-server"  % Versions.http4s
  val http4sCirce = "org.http4s"           %% "http4s-circe"         % Versions.http4s
  val http4sClient = "org.http4s"          %% "http4s-blaze-client"  % Versions.http4s
  val http4sDsl = "org.http4s"             %% "http4s-dsl"           % Versions.http4s
  val logback = "ch.qos.logback"           % "logback-classic"       % Versions.logback
  val pureConfig = "com.github.pureconfig" %% "pureconfig"           % Versions.pureConfig

  // Test
  val scalaTest = "org.scalatest" %% "scalatest" % Versions.scalaTest

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
  .aggregate(core)
  .dependsOn(core)

lazy val core = Project(
  id = "core",
  base = file("core")
).settings(moduleName := "core")
  .settings(commonSettings)
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
      D.circeLiteral,
      D.circeParser,
      D.enumeratum,
      D.enumeratumCirce,
      D.fs2,
      D.http4sServer,
      D.http4sCirce,
      D.http4sClient,
      D.http4sDsl,
      D.logback,
      D.pureConfig,
      D.scalaTest % "test"
    )
  )

///////////////////////////////////////////////////////////////////////////////////////////////////
// Commands
///////////////////////////////////////////////////////////////////////////////////////////////////

addCommandAlias("update", ";dependencyUpdates")
addCommandAlias("fcompile", ";scalafmtSbt;compile;test:compile")

///////////////////////////////////////////////////////////////////////////////////////////////////
// Plugins
///////////////////////////////////////////////////////////////////////////////////////////////////

enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)
