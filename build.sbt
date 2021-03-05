import PlayCrossCompilation._

lazy val root = Project("reactivemongo-test", file("."))
  .enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning)
  .settings(
    makePublicallyAvailableOnBintray := true,
    majorVersion                     := 4
  )
  .settings(
    scalaVersion        := "2.12.13",
    libraryDependencies ++= compileDependencies ++ testDependencies,
    resolvers           += Resolver.typesafeRepo("releases"),
    playCrossCompilationSettings
  )

val compileDependencies: Seq[ModuleID] = dependencies(
  shared = Seq(
    "org.scalatest"        %% "scalatest"            % "3.1.4",
    "com.vladsch.flexmark" %  "flexmark-all"         % "0.36.8"
  ),
  play26 = Seq(
    "uk.gov.hmrc"          %% "simple-reactivemongo" % "8.0.0-play-26"
  ),
  play27 = Seq(
    "uk.gov.hmrc"          %% "simple-reactivemongo" % "8.0.0-play-27"
  ),
  play28 = Seq(
    "uk.gov.hmrc"          %% "simple-reactivemongo" % "8.0.0-play-28"
  )
)

val testDependencies: Seq[ModuleID] = List(
  "ch.qos.logback" % "logback-classic" % "1.2.3" % Test
)
