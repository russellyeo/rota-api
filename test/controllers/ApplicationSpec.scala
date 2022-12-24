package controllers

import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import org.scalatestplus.mockito._

import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._

import models.{Rota, User, RotaUser}
import repositories.{RotasRepository, UsersRepository, RotaUsersRepository}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import org.mockito.Mockito.when

class ApplicationSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {

  "Application GET" should {

    "get a rota with users and an assigned user" in new WithSUT() {
      // GIVEN
      val rota = Rota("Retrospective", None, Some(1))

      val user1 = User("Sofia", 1)
      val user2 = User("Emma", 2)
      val user3 = User("Aiden", 3)
      val users = Seq(user1, user2, user3)

      val rotaUsers = Seq(
        RotaUser(1, 1),
        RotaUser(1, 2),
        RotaUser(1, 3)
      )

      when(mockRotasRepository.get(1)).thenReturn(Future.successful(Some(rota)))
      when(mockRotaUsersRepository.getRotaUsersWithRotaID(1))
        .thenReturn(Future.successful(rotaUsers))
      when(mockUsersRepository.get(1))
        .thenReturn(Future.successful(Some(user1)))
      when(mockUsersRepository.getList(Seq(1, 2, 3)))
        .thenReturn(Future.successful(users))

      // WHEN
      val result = application.rota(1).apply(FakeRequest(GET, "/rotas/1"))

      // THEN
      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) mustBe Json.parse("""
      {
        "rota": {
          "name": "Retrospective",
          "description": null
        },
        "assigned": "Sofia",
        "users": ["Sofia", "Emma", "Aiden"]
      }
      """)
    }

    "get a rota with users and no assigned user" in new WithSUT() {
      // GIVEN
      val rota =
        Rota("Sprint Planning", Some("Assign tickets to the next Sprint"), None)

      val user1 = User("Yara", 1)
      val user2 = User("Ravi", 2)
      val user3 = User("Isabella", 3)
      val users = Seq(user1, user2, user3)

      val rotaUsers = Seq(
        RotaUser(1, 1),
        RotaUser(1, 2),
        RotaUser(1, 3)
      )

      when(mockRotasRepository.get(1)).thenReturn(Future.successful(Some(rota)))
      when(mockRotaUsersRepository.getRotaUsersWithRotaID(1))
        .thenReturn(Future.successful(rotaUsers))
      when(mockUsersRepository.getList(Seq(1, 2, 3)))
        .thenReturn(Future.successful(users))

      // WHEN
      val result = application.rota(1).apply(FakeRequest(GET, "/rotas/1"))

      // THEN
      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) mustBe Json.parse("""
      {
        "rota": {
          "name": "Sprint Planning",
          "description": "Assign tickets to the next Sprint"
        },
        "assigned": null,
        "users": ["Yara", "Ravi", "Isabella"]
      }
      """)
    }

    "get a rota with id that does not exist" in new WithSUT() {
      // GIVEN
      when(mockRotasRepository.get(1)).thenReturn(Future.successful(None))

      // WHEN
      val result = application.rota(1).apply(FakeRequest(GET, "/rotas/1"))

      // THEN
      status(result) mustBe NOT_FOUND
      contentAsJson(result) mustBe Json.parse("""
      {
        "message": "Rota with id 1 not found"
      }
      """)
    }

  }

}

trait WithSUT extends WithApplication with MockitoSugar {
  val mockRotasRepository = mock[RotasRepository]
  val mockUsersRepository = mock[UsersRepository]
  val mockRotaUsersRepository = mock[RotaUsersRepository]
  val controllerComponents = stubControllerComponents()
  val application = new Application(
    mockRotasRepository,
    mockUsersRepository,
    mockRotaUsersRepository,
    controllerComponents
  )
}
