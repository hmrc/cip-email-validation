import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  val hmrcBootstrapVersion = "7.2.0"

  val compile = Seq(
    "uk.gov.hmrc" %% "bootstrap-backend-play-28"    % hmrcBootstrapVersion,
    "uk.gov.hmrc" %% "internal-auth-client-play-28" % "1.2.0"
  )

  val test = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-28" % hmrcBootstrapVersion % "test, it",
    "org.mockito" %% "mockito-scala"          % "1.17.7"             % Test
  )
}
