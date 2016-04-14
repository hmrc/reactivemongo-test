import sbt.Keys._
import sbt._
import uk.gov.hmrc.SbtAutoBuildPlugin
import uk.gov.hmrc.versioning.SbtGitVersioning

object HmrcBuild extends Build {

  import SbtAutoBuildPlugin._

  val nameApp = "reactivemongo-test"

  val appDependencies = {
    import Dependencies._

    Seq(
      Compile.scalaTest,
      Compile.pegdown,
      Compile.simpleReactiveMongo,
      Compile.playJson
    )
  }

  lazy val simpleReactiveMongo = Project(nameApp, file("."))
    .enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning)
    .settings(
      autoSourceHeader := false,
      scalaVersion := "2.11.7",
      libraryDependencies ++= appDependencies,
      resolvers := Seq (
        Resolver.bintrayRepo("hmrc", "releases"),
        Resolver.typesafeRepo("releases")
        ),
      crossScalaVersions := Seq("2.11.7")
    )
}

object Dependencies {

  object Compile {
    val scalaTest = "org.scalatest" %% "scalatest" % "2.2.4" % "provided"
    val pegdown = "org.pegdown" % "pegdown" % "1.5.0" % "provided"
    val playJson = "com.typesafe.play" %% "play-json" % "2.3.10" % "provided"
    val simpleReactiveMongo = "uk.gov.hmrc" %% "simple-reactivemongo" % "4.6.0"
  }

  sealed abstract class Test(scope: String) {
  }

  object Test extends Test("test")

  object IntegrationTest extends Test("it")

}


object BuildDescriptionSettings {

  def apply() = Seq(
      pomExtra := (<url>https://www.gov.uk/government/organisations/hm-revenue-customs</url>
        <licenses>
          <license>
            <name>Apache 2</name>
            <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
          </license>
        </licenses>
        <scm>
          <connection>scm:git@github.com:hmrc/simple-reactivemongo.git</connection>
          <developerConnection>scm:git@github.com:hmrc/simple-reactivemongo.git</developerConnection>
          <url>git@github.com:hmrc/simple-reactivemongo.git</url>
        </scm>
        <developers>
          <developer>
            <id>xnejp03</id>
            <name>Petr Nejedly</name>
            <url>http://www.equalexperts.com</url>
          </developer>
          <developer>
            <id>charleskubicek</id>
            <name>Charles Kubicek</name>
            <url>http://www.equalexperts.com</url>
          </developer>
          <developer>
            <id>duncancrawford</id>
            <name>Duncan Crawford</name>
            <url>http://www.equalexperts.com</url>
          </developer>
        </developers>)
    )
}

