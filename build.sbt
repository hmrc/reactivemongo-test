
val nameApp = "reactivemongo-test"

lazy val root = Project(nameApp, file("."))
  .enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning)
  .settings(
    scalaVersion        := "2.11.12",
    crossScalaVersions  := Seq("2.11.12", "2.12.6"),
    libraryDependencies ++= dependencies,
    resolvers           :=
      Seq(
        Resolver.bintrayRepo("hmrc", "releases"),
        Resolver.typesafeRepo("releases")
      )
  )

val dependencies =
  Seq(
    "org.scalatest"     %% "scalatest"            % "2.2.6",
    "org.pegdown"       % "pegdown"               % "1.6.0",
    "com.typesafe.play" %% "play-json"            % "2.5.8",
    "uk.gov.hmrc"       %% "simple-reactivemongo" % "6.1.0"
  )
