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

import models.{Rota, User, RotaUser}
import repositories.{RotasRepository, UsersRepository, RotaUsersRepository}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ApplicationSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {

  "GET /rota/{id}" should {

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

    "not get a rota with id that does not exist" in new WithSUT() {
      // GIVEN
      when(mockRotasRepository.get(1)).thenReturn(Future.successful(None))

      // WHEN
      val result = application.rota(1).apply(FakeRequest(GET, "/rotas/1"))

      // THEN
      status(result) mustBe NOT_FOUND
      contentAsJson(result) mustBe Json.obj(
        "message" -> "error.resourceNotFound"
      )
    }

  }

  "POST /rotas" should {

    "create a rota with a description" in new WithSUT() {
      // GIVEN a rota with a description
      val rota = Rota("Sprint Planning", Some("Assign tickets"))
      val inserted = rota.copy(id = Some(1))

      when(mockRotasRepository.insert(rota))
        .thenReturn(Future.successful(inserted))

      // WHEN the request is received
      val request = FakeRequest(POST, "/rotas").withBody(Json.toJson(rota))
      val result = application.createRota().apply(request)

      // THEN the rota is created
      status(result) mustBe CREATED
      contentAsJson(result) mustBe Json.toJson(inserted)
      verify(mockRotasRepository).insert(rota)
    }

    "create a rota without a description" in new WithSUT() {
      // GIVEN a rota without a description
      val rota = Rota("Retrospective")
      val inserted = rota.copy(id = Some(1))

      when(mockRotasRepository.insert(rota))
        .thenReturn(Future.successful(inserted))

      // WHEN the request is received
      val request = FakeRequest(POST, "/rotas").withBody(Json.toJson(rota))
      val result = application.createRota().apply(request)

      // THEN the rota is created
      status(result) mustBe CREATED
      contentAsJson(result) mustBe Json.toJson(inserted)
      verify(mockRotasRepository).insert(rota)
    }

    "not create a rota with a missing name parameter" in new WithSUT() {
      // GIVEN a rota without a description
      val rota = Json.obj(
        "title" -> "Retrospective",
        "desc" -> "Review the sprint"
      )

      // WHEN the request is received
      val request = FakeRequest(POST, "/rotas").withBody(rota)
      val result = application.createRota().apply(request)

      // THEN an "error.path.missing" error is returned
      status(result) mustBe BAD_REQUEST
      contentAsJson(result) mustBe Json.obj(
        "message" -> "error.path.missing"
      )
      // AND the rota is not created
      verify(mockRotasRepository, times(0)).insert(any[Rota])
    }

    "not create a rota with a name that is less than three characters" in new WithSUT() {
      // GIVEN a rota with a name less than three characters
      val rota = Json.obj("name" -> "wa")

      // WHEN the request is received
      val request = FakeRequest(POST, "/rotas").withBody(rota)
      val result = application.createRota().apply(request)

      // THEN an "error.minLength" error is returned
      status(result) mustBe BAD_REQUEST
      contentAsJson(result) mustBe Json.obj(
        "message" -> "error.minLength"
      )
      // AND the rota is not created
      verify(mockRotasRepository, times(0)).insert(any[Rota])
    }

    "not create a rota with a name that is an empty string" in new WithSUT() {
      // GIVEN a rota with a name that is an empty string
      val rota = Json.obj("name" -> "")

      // WHEN the request is received
      val request = FakeRequest(POST, "/rotas").withBody(rota)
      val result = application.createRota().apply(request)

      // THEN an "error.empty" error is returned
      status(result) mustBe BAD_REQUEST
      contentAsJson(result) mustBe Json.obj(
        "message" -> "error.empty"
      )
      // AND the rota is not created
      verify(mockRotasRepository, times(0)).insert(any[Rota])
    }

    "not create a rota with a description that is an empty string" in new WithSUT() {
      // GIVEN a rota with a description that is an empty string
      val rota = Json.obj(
        "name" -> "Standup",
        "description" -> ""
      )

      // WHEN the request is received
      val request = FakeRequest(POST, "/rotas").withBody(rota)
      val result = application.createRota().apply(request)

      // THEN an "error.empty" error is returned
      status(result) mustBe BAD_REQUEST
      contentAsJson(result) mustBe Json.obj(
        "message" -> "error.empty"
      )
      // AND the rota is not created
      verify(mockRotasRepository, times(0)).insert(any[Rota])
    }

  }

}

trait WithSUT extends WithApplication with MockitoSugar {
  val mockRotasRepository = mock[RotasRepository]
  val mockUsersRepository = mock[UsersRepository]
  val mockRotaUsersRepository = mock[RotaUsersRepository]
  val messagesApi = stubMessagesApi()
  val controllerComponents = stubControllerComponents()

  val application = new Application(
    mockRotasRepository,
    mockUsersRepository,
    mockRotaUsersRepository,
    messagesApi,
    controllerComponents
  )
}
