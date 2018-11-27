import PlayCrossCompilation._

val libName = "reactivemongo-test"

lazy val root = Project(libName, file("."))
  .enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning, SbtArtifactory)
  .settings(
    makePublicallyAvailableOnBintray := true,
    majorVersion                     := 4
  )
  .settings(
    scalaVersion        := "2.11.12",
    crossScalaVersions  := Seq("2.11.12"),
    libraryDependencies ++= compileDependencies ++ testDependencies,
    resolvers           := Seq(
      Resolver.bintrayRepo("hmrc", "releases"),
      Resolver.typesafeRepo("releases")
    ),
    playCrossCompilationSettings
  )


val compileDependencies: Seq[ModuleID] = dependencies(
  shared = Seq(
    "org.pegdown"       % "pegdown"               % "1.6.0",
    "org.scalatest"     %% "scalatest"            % "3.0.5"
  ),
  play25 = Seq(
    "uk.gov.hmrc"       %% "simple-reactivemongo" % "7.6.0-play-25"
  ),
  play26 = Seq(
    "uk.gov.hmrc"       %% "simple-reactivemongo" % "7.6.0-play-26"
  )
)

val testDependencies: Seq[ModuleID] = dependencies(
  play25 = Seq(
    "ch.qos.logback" % "logback-classic" % "1.1.2" % Test
  ),
  play26 = Seq(
    "ch.qos.logback" % "logback-classic" % "1.2.3" % Test
  )
)
