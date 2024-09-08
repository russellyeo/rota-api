package services

import org.scalatest._
import org.scalatestplus.mockito._
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers._
import play.api.test.Helpers._
import play.api.test.WithApplication
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import models.dao._
import models.dto._
import repositories._

class SlackAuthenticationServiceSpec extends PlaySpec with GuiceOneAppPerTest {

  "createInstallation" should {
    "store a new installation" in new WithSlackAuthenticationService() {
      // GIVEN a valid Slack team installation
      val installation = SlackInstallationDTO(
        team = Some(SlackTeamDTO("T012345678", "example-team-name")),
        enterprise = None,
        user = SlackUserDTO("U012345678", None, None),
        tokenType = "bot",
        isEnterpriseInstall = false,
        appId = "1",
        authVersion = "v2",
        bot = SlackBotDTO(
          id = "B012345678",
          scopes = Seq("app_mentions:read", "chat:write"),
          token = "xoxb-12345678",
          userId = "U012345678"
        )
      )
      // AND the repositories will return successful futures
      when(mockSlackTeamRepository.upsert(any[SlackTeamDAO]))
        .thenReturn(Future.successful("T012345678"))
      when(mockSlackUserRepository.upsert(any[SlackUserDAO]))
        .thenReturn(Future.successful("U012345678"))
      when(mockSlackBotRepository.upsert(any[SlackBotDAO]))
        .thenReturn(Future.successful("B012345678"))
      when(mockSlackInstallationRepository.upsert(any[SlackInstallationDAO]))
        .thenReturn(Future.successful("T012345678"))

      // WHEN we create a new installation
      val result = service.create(installation)

      // THEN the installation is stored and the ID is returned
      await(result) mustBe "T012345678"
    }
  }

}

trait WithSlackAuthenticationService extends MockitoSugar {
  val mockSlackBotRepository = mock[SlackBotRepository]
  val mockSlackInstallationRepository = mock[SlackInstallationRepository]
  val mockSlackTeamRepository = mock[SlackTeamRepository]
  val mockSlackUserRepository = mock[SlackUserRepository]

  val service = new SlackAuthenticationService(
    mockSlackBotRepository,
    mockSlackInstallationRepository,
    mockSlackTeamRepository,
    mockSlackUserRepository
  )
}
