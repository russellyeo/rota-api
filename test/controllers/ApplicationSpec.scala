package controllers

import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import org.scalatestplus.mockito._

import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._

import repositories.{ RotasRepository, UsersRepository, RotaUsersRepository }
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 *
 * For more information, see https://www.playframework.com/documentation/latest/ScalaTestingWithScalaTest
 */
class ApplicationSpec extends PlaySpec with GuiceOneAppPerTest with Injecting with MockitoSugar {

  "Application GET" should {

    "render the index page from a new instance of controller" in {
      val mockRotasRepository = mock[RotasRepository]
      val mockUsersRepository = mock[UsersRepository]
      val mockRotaUsersRepository = mock[RotaUsersRepository]
      val controllerComponents = stubControllerComponents()
      val controller = new Application(mockRotasRepository, mockUsersRepository, mockRotaUsersRepository, controllerComponents)
      val home = controller.index().apply(FakeRequest(GET, "/"))

      status(home) mustBe OK
      contentType(home) mustBe Some("text/html")
      contentAsString(home) must include ("Welcome to Play")
    }

    "render the index page from the application" in {
      val controller = inject[Application]
      val home = controller.index().apply(FakeRequest(GET, "/"))

      status(home) mustBe OK
      contentType(home) mustBe Some("text/html")
      contentAsString(home) must include ("Welcome to Play")
    }

    "render the index page from the router" in {
      val request = FakeRequest(GET, "/")
      val home = route(app, request).get

      status(home) mustBe OK
      contentType(home) mustBe Some("text/html")
      contentAsString(home) must include ("Welcome to Play")
    }
  }

}
