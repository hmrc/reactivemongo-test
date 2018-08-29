import sbt.PlayCrossCompilation._

val nameApp = "reactivemongo-test"

lazy val root = Project(nameApp, file("."))
  .enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning, SbtArtifactory)
  .settings(
    majorVersion                     := 3,
    makePublicallyAvailableOnBintray := true
  )
  .settings(
    scalaVersion        := "2.11.12",
    crossScalaVersions  := Seq("2.11.12", "2.12.6"),
    libraryDependencies ++= dependencies,
    resolvers           :=
      Seq(
        Resolver.bintrayRepo("hmrc", "releases"),
        Resolver.typesafeRepo("releases")
      ),
    PlayCrossCompilation()
  )

val dependencies = {

  val play25Dependencies = Seq(
    "uk.gov.hmrc"       %% "simple-reactivemongo" % "6.1.0",
    "com.typesafe.play" %% "play-json"            % "2.5.12"
  )

  val play26Dependencies = Seq(
    "uk.gov.hmrc"   %% "simple-reactivemongo-26" % "0.9.0"
  )

  Seq(
    "org.scalatest" %% "scalatest" % "3.0.5",
    "org.pegdown"   % "pegdown"    % "1.6.0"
  ) ++ (
    if (playVersion == Play25) play25Dependencies else play26Dependencies
  )

}
