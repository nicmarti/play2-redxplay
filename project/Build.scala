import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "play2-redxplay"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      // Redis driver
      "org.sedis" % "sedis" % "1.0.1"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
      // Add your own project settings here      
    )

}
