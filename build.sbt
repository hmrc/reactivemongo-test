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
    crossScalaVersions  := Seq("2.11.12", "2.12.6"),
    libraryDependencies ++= compileDependencies ++ testDependencies,
    resolvers           := Seq(
      Resolver.bintrayRepo("hmrc", "releases"),
      Resolver.typesafeRepo("releases")
    ),
    playCrossCompilationSettings
  )

val simpleReactiveMongoVersion = "7.0.0"

val compileDependencies: Seq[ModuleID] = dependencies(
  shared = Seq(
    "org.pegdown"       % "pegdown"               % "1.6.0",
    "org.scalatest"     %% "scalatest"            % "3.0.5"
  ),
  play25 = Seq(
    "uk.gov.hmrc"       %% "simple-reactivemongo" % s"$simpleReactiveMongoVersion-play-25-SNAPSHOT"
  ),
  play26 = Seq(
    "uk.gov.hmrc"       %% "simple-reactivemongo" % s"$simpleReactiveMongoVersion-play-26-SNAPSHOT"
  )
)

val testDependencies: Seq[ModuleID] = dependencies(
  shared = Seq(
    "log4j"             % "log4j"                 % "1.2.17" % Test
  )
)
