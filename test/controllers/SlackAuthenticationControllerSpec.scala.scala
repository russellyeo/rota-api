package controllers

import org.scalatest._
import org.scalatestplus.mockito._
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers._
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import models._
import models.dto._
import services.SlackAuthenticationService

class SlackAuthenticationControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {

  "POST /api/v1/slack-installation" should {
    "create a new installation" in new WithSlackAuthenticationController() {
      // GIVEN a valid Slack installation JSON
      val jsonString = """
      {
        "installation": {
          "team": {
            "id": "T012345678",
            "name": "example-team-name"
          },
          "user": {
            "id": "U012345678"
          },
          "tokenType": "bot",
          "isEnterpriseInstall": false,
          "appId": "1",
          "authVersion": "v2",
          "bot":{
              "scopes": [
                "app_mentions:read",
                "chat:write"
              ],
              "token": "xoxb-12345678",
              "userId": "U012345678",
              "id": "B012345678"
          }
        }
      }
      """
      val json = Json.parse(jsonString)
      val slackInstallationDTO = json.as[SlackInstallationResponseDTO]
      // AND the SlackAuthenticationService returns a successful future
      when(mockSlackAuthenticationService.create(any[SlackInstallationDTO]))
        .thenReturn(Future.successful("T012345678"))

      // WHEN we make a request to create a new installation
      val request = FakeRequest(POST, "/api/v1/slack-installation").withBody(json)
      val result = controller.createInstallation().apply(request)

      // THEN the response is OK and returns the expected JSON
      status(result) mustBe CREATED
      contentType(result) mustBe Some("application/json")
    }
  }

}

trait WithSlackAuthenticationController extends WithApplication with MockitoSugar {
  val mockSlackAuthenticationService = mock[SlackAuthenticationService]
  val controllerComponents = stubControllerComponents()

  val controller = new SlackAuthenticationController(
    mockSlackAuthenticationService,
    controllerComponents
  )
}
