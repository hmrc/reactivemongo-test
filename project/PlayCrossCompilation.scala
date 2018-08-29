package sbt

import Keys._

object PlayCrossCompilation {

  sealed trait PlayVersion
  case object Play25 extends PlayVersion
  case object Play26 extends PlayVersion

  val playVersion: PlayVersion =
    sys.env.get("PLAY_VERSION") match {
      case Some("2.5")   => Play25
      case Some("2.6")   => Play26
      case None          => Play25 // default to Play 2.5 for local development
      case Some(sthElse) => throw new Exception(s"Play version '$sthElse' not supported")
    }

  private val releaseSuffix, playDir =
    if (playVersion == Play25) "play-25" else "play-26"

  def apply() = Seq(
    version ~= {
      _ + "-" + releaseSuffix // handle -SNAPSHOT versions + migrate to new build + copy failOnUnindexedQueiresSpec
    },
    unmanagedSourceDirectories in Compile += {
      (sourceDirectory in Compile).value / playDir
    },
    unmanagedSourceDirectories in Test += {
      (sourceDirectory in Test).value / playDir
    },
    unmanagedResourceDirectories in Compile += {
      (sourceDirectory in Compile).value / playDir / "resources"
    },
    unmanagedResourceDirectories in Test += {
      (sourceDirectory in Test).value / playDir / "resources"
    }
  )

}
