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

import models._
import models.dto._
import services.RotasService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import org.checkerframework.checker.initialization.qual.NotOnlyInitialized
import services.UsersService

class ApplicationSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {

  "GET /rota/:name" should {

    "retrieve a rota with an assigned user and some unassigned users" in new WithSUT() {
      // GIVEN a rota with an assigned user and some unassigned users
      val rota = Rota(
        name = "retrospective",
        description = None,
        assigned = Some(1)
      )
      val user1 = User("@Sofia", 1)
      val user2 = User("@Emma", 2)
      val user3 = User("@Aiden", 3)
      val users = Seq(user1, user2, user3)

      when(mockRotasService.retrieve("retrospective"))
        .thenReturn(
          Future.successful(Some(RotaWithUsers(rota, Some(user1), users)))
        )

      // WHEN we make a request to retrieve the Rota
      val result = application
        .retrieveRota("retrospective")
        .apply(FakeRequest(GET, "/rotas/retrospective"))

      // THEN the response is OK and returns the expected JSON
      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) mustBe Json.parse("""
      {
        "rota": {
          "name": "retrospective",
          "description": null
        },
        "assigned": "@Sofia",
        "users": ["@Sofia", "@Emma", "@Aiden"]
      }
      """)
    }

    "retrieve a rota with no assigned user and some unassigned users" in new WithSUT() {
      // GIVEN a rota with no assigned user and some unassigned users
      val rota = Rota(
        name = "sprint-planning",
        description = Some("Assign tickets to the next Sprint"),
        assigned = None
      )
      val user1 = User("@Yara", 1)
      val user2 = User("@Ravi", 2)
      val user3 = User("@Isabella", 3)
      val users = Seq(user1, user2, user3)

      when(mockRotasService.retrieve("sprint-planning"))
        .thenReturn(
          Future.successful(Some(RotaWithUsers(rota, None, users)))
        )

      // WHEN we make a request to retrieve the Rota
      val result = application
        .retrieveRota("sprint-planning")
        .apply(FakeRequest(GET, "/rotas/sprint-planning"))

      // THEN the response is OK and returns the expected JSON
      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) mustBe Json.parse("""
      {
        "rota": {
          "name": "sprint-planning",
          "description": "Assign tickets to the next Sprint"
        },
        "assigned": null,
        "users": ["@Yara", "@Ravi", "@Isabella"]
      }
      """)
    }

    "not retrieve a rota that does not exist" in new WithSUT() {
      // GIVEN there is no rota "unknown"
      when(mockRotasService.retrieve("unknown")).thenReturn(Future.successful(None))

      // WHEN we make a request to retrieve a rota "unknown"
      val result = application
        .retrieveRota("unknown")
        .apply(FakeRequest(GET, "/rotas/unknown"))

      // THEN the repsonse is Not Found with an error message
      status(result) mustBe NOT_FOUND
      contentAsJson(result) mustBe Json.obj(
        "message" -> "error.resourceNotFound"
      )
    }

  }

  "POST /rotas" should {

    "create a rota with a description" in new WithSUT() {
      // GIVEN a rota with a description
      val rota = Rota("sprint-planning", Some("Assign tickets"))

      when(mockRotasService.create(rota))
        .thenReturn(Future.successful(rota))

      // WHEN we make a request to create the Rota
      val request = FakeRequest(POST, "/rotas").withBody(Json.toJson(rota))
      val result = application.createRota().apply(request)

      // THEN the rota is created
      status(result) mustBe CREATED
      contentAsJson(result) mustBe Json.toJson(rota)
      verify(mockRotasService).create(rota)
    }

    "create a rota without a description" in new WithSUT() {
      // GIVEN a rota without a description
      val rota = Rota("retrospective")

      when(mockRotasService.create(rota))
        .thenReturn(Future.successful(rota))

      // WHEN we make a request to create the Rota
      val request = FakeRequest(POST, "/rotas").withBody(Json.toJson(rota))
      val result = application.createRota().apply(request)

      // THEN the rota is created
      status(result) mustBe CREATED
      contentAsJson(result) mustBe Json.toJson(rota)
      verify(mockRotasService).create(rota)
    }

    "not create a rota with a missing name parameter" in new WithSUT() {
      // GIVEN a rota without a name
      val rota = Json.obj(
        "description" -> "Review the sprint"
      )

      // WHEN we make a request to create the Rota
      val request = FakeRequest(POST, "/rotas").withBody(rota)
      val result = application.createRota().apply(request)

      // THEN an "error.path.missing" error is returned
      status(result) mustBe BAD_REQUEST
      contentAsJson(result) mustBe Json.obj(
        "message" -> "error.path.missing"
      )
      // AND the rota is not created
      verify(mockRotasService, times(0)).create(any[Rota])
    }

    "not create a rota with a name that is less than three characters" in new WithSUT() {
      // GIVEN a rota with a name less than three characters
      val rota = Json.obj("name" -> "wa")

      // WHEN we make a request to create the Rota
      val request = FakeRequest(POST, "/rotas").withBody(rota)
      val result = application.createRota().apply(request)

      // THEN an "error.minLength" error is returned
      status(result) mustBe BAD_REQUEST
      contentAsJson(result) mustBe Json.obj(
        "message" -> "error.minLength"
      )
      // AND the rota is not created
      verify(mockRotasService, times(0)).create(any[Rota])
    }

    "not create a rota with a name that is an empty string" in new WithSUT() {
      // GIVEN a rota with a name that is an empty string
      val rota = Json.obj("name" -> "")

      // WHEN we make a request to create the Rota
      val request = FakeRequest(POST, "/rotas").withBody(rota)
      val result = application.createRota().apply(request)

      // THEN an "error.empty" error is returned
      status(result) mustBe BAD_REQUEST
      contentAsJson(result) mustBe Json.obj(
        "message" -> "error.empty"
      )
      // AND the rota is not created
      verify(mockRotasService, times(0)).create(any[Rota])
    }

    "not create a rota with a description that is an empty string" in new WithSUT() {
      // GIVEN a rota with a description that is an empty string
      val rota = Json.obj(
        "name" -> "standup",
        "description" -> ""
      )

      // WHEN we make a request to create the Rota
      val request = FakeRequest(POST, "/rotas").withBody(rota)
      val result = application.createRota().apply(request)

      // THEN an "error.empty" error is returned
      status(result) mustBe BAD_REQUEST
      contentAsJson(result) mustBe Json.obj(
        "message" -> "error.empty"
      )
      // AND the rota is not created
      verify(mockRotasService, times(0)).create(any[Rota])
    }

  }

  "PATCH /rotas/:name" should {
    "update a rota's assigned user ID" in new WithSUT() {
      // GIVEN an updated rota will be returned from RotasService
      val updated = Rota(
        name = "sprint-planning",
        description = Some("Assign tickets"),
        assigned = Some(5)
      )

      when(
        mockRotasService.update(
          name = "sprint-planning",
          description = None,
          assigned = Some(5)
        )
      ).thenReturn(Future.successful(Some(updated)))

      // WHEN we make a request to update the rota's assigned user ID
      val request = FakeRequest(PATCH, "/rotas/sprint-planning").withBody(Json.obj("assigned" -> 5))
      val response = application.updateRota("sprint-planning").apply(request)

      // THEN the rota is updated
      status(response) mustBe OK
      contentAsJson(response) mustBe Json.obj("assigned" -> 5)
    }

    "update a rota's description" in new WithSUT() {
      // GIVEN an updated rota will be returned from RotasService
      val updated = Rota(
        name = "sprint-planning",
        description = Some("Plan, estimate, and assign tickets"),
        assigned = Some(5)
      )
      when(
        mockRotasService.update(
          name = "sprint-planning",
          description = Some("Plan, estimate, and assign tickets"),
          assigned = None
        )
      ).thenReturn(Future.successful(Some(updated)))

      // WHEN we make a request to update the rota's assigned user ID
      val request = FakeRequest(PATCH, "/rotas/sprint-planning").withBody(
        Json.obj("description" -> "Plan, estimate, and assign tickets")
      )
      val response = application.updateRota("sprint-planning").apply(request)

      // THEN the rota is updated
      status(response) mustBe OK
      contentAsJson(response) mustBe Json.obj("description" -> "Plan, estimate, and assign tickets")
    }

    "fail to update a rota's name if it less than 3 characters" in new WithSUT() {
      // WHEN we make a request to update the rota's assigned user ID
      val request = FakeRequest(PATCH, "/rotas/sprint-planning").withBody(
        Json.obj("name" -> "x")
      )
      val response = application.updateRota("sprint-planning").apply(request)

      // THEN the rota is updated
      status(response) mustBe BAD_REQUEST
      contentAsJson(response) mustBe Json.obj("message" -> "error.minLength")
    }

    "fail to update a rota's description if it less than 3 characters" in new WithSUT() {
      // WHEN we make a request to update the rota's assigned user ID
      val request = FakeRequest(PATCH, "/rotas/sprint-planning").withBody(
        Json.obj("description" -> "y")
      )
      val response = application.updateRota("sprint-planning").apply(request)

      // THEN the rota is updated
      status(response) mustBe BAD_REQUEST
      contentAsJson(response) mustBe Json.obj("message" -> "error.minLength")
    }
  }

  "DELETE /rotas/:name" should {
    "delete an existing rota" in new WithSUT() {
      // GIVEN the rota will be deleted successfully
      when(mockRotasService.delete("retrospective"))
        .thenReturn(Future.successful(1))

      // WHEN we make a request to delete the rota
      val result = application
        .deleteRota("retrospective")
        .apply(FakeRequest(DELETE, "/rotas/retrospective"))

      // THEN the rota is deleted
      status(result) mustBe OK
      verify(mockRotasService).delete("retrospective")
    }

    "fail to delete a non-existing rota" in new WithSUT() {
      // GIVEN the rota will be not be deleted successfully
      when(mockRotasService.delete("retro"))
        .thenReturn(Future.successful(0))

      // WHEN we make a request to delete the rota
      val result = application
        .deleteRota("retro")
        .apply(FakeRequest(DELETE, "/rotas/retro"))

      // THEN the rota is not deleted
      status(result) mustBe NOT_FOUND
      contentAsJson(result) mustBe Json.obj(
        "message" -> "error.resourceNotFound"
      )
      verify(mockRotasService).delete("retro")
    }
  }

  "POST /rotas/:retrospective/users" should {
    "add users to a rota" in new WithSUT() {
      // GIVEN
      val rota = Rota("retrospective", None, Some(1))
      val usersToAdd = Seq("@Helena", "@Bruno")
      val createdUsers = Seq(User("@Helena", 1), User("@Bruno", 2))
      val rotaUsers = Seq(RotaUser("retrospective", 1), RotaUser("retrospective", 2))
      val updatedRota = RotaWithUsers(rota, None, createdUsers)

      when(mockUsersService.createUsersIfNeeded(usersToAdd))
        .thenReturn(Future.successful(createdUsers))

      when(mockRotasService.addUsersToRota("retrospective", createdUsers))
        .thenReturn(Future.successful(rotaUsers))

      when(mockRotasService.retrieve("retrospective"))
        .thenReturn(Future.successful(Some(updatedRota)))

      // WHEN the request is made
      val request = FakeRequest(POST, "/rotas/retrospective/users").withBody(
        Json.obj("users" -> usersToAdd)
      )
      val result = application.addUsersToRota("retrospective").apply(request)

      // THEN
      status(result) mustBe OK
      contentAsJson(result) mustBe Json.obj(
        "rota" -> Json.obj(
          "name" -> "retrospective",
          "description" -> JsNull
        ),
        "assigned" -> JsNull,
        "users" -> JsArray(Seq("@Helena", "@Bruno").map(JsString))
      )
    }
  }

  "GET /rotas/:retrospective/rotate" should {
    "rotate the rota's assigned user" in new WithSUT() {
      // GIVEN a rota will be rotated successfully
      val rota = Rota(
        name = "retrospective",
        description = Some("Reflect on the previous sprint"),
        assigned = Some(1)
      )
      val users = Seq(
        User("@Maria", 1),
        User("@Mohammed", 2),
        User("@Beatrice", 3)
      )
      val rotaUsers = Seq(
        RotaUser("retrospective", 1),
        RotaUser("retrospective", 2),
        RotaUser("retrospective", 3)
      )
      val rotaWithUsers = RotaWithUsers(
        rota = rota,
        assigned = Some(User("@Mohammed", 2)),
        users = users
      )

      when(mockRotasService.rotate("retrospective"))
        .thenReturn(Future.successful(Some(rota)))

      when(mockRotasService.retrieve("retrospective"))
        .thenReturn(Future.successful(Some(rotaWithUsers)))

      // WHEN the request is made
      val request = FakeRequest(GET, "/rotas/retrospective/rotate")
      val result = application.rotateRota("retrospective").apply(request)

      // THEN the updated rota with users is returned
      status(result) mustBe OK
      contentAsJson(result) mustBe Json.obj(
        "rota" -> Json.obj(
          "name" -> "retrospective",
          "description" -> "Reflect on the previous sprint"
        ),
        "assigned" -> "@Mohammed",
        "users" -> JsArray(Seq("@Maria", "@Mohammed", "@Beatrice").map(JsString))
      )
    }
  }

  "GET /users/by-name/:name" should {
    "get a user by name" in new WithSUT() {
      // GIVEN the user exists
      val user = User("@Mohammed", 2)
      when(mockUsersService.getUserByName("@Mohammed"))
        .thenReturn(Future.successful(Some(user)))

      // WHEN the request is made
      val request = FakeRequest(GET, "/users/by-name/%40Mohammed")
      val result = application.getUserByName("@Mohammed").apply(request)

      // THEN
      status(result) mustBe OK
      contentAsJson(result) mustBe Json.obj(
        "name" -> "@Mohammed",
        "id" -> 2
      )
    }
  }

  "DELETE  /rotas/:name/users/:name" should {
    "delete a user from a rota" in new WithSUT() {
      // GIVEN a rota with an assigned user
      val rota = Rota(
        name = "retrospective",
        description = Some("Reflect on the previous sprint"),
        assigned = Some(1)
      )
      val maria = User("@Maria", 1)
      val mohammed = User("@Mohammed", 2)
      val beatrice = User("@Beatrice", 3)

      when(mockRotasService.retrieveRotaDescription("retrospective"))
        .thenReturn(Future.successful(Some(rota)))

      when(mockUsersService.getUserByName("@Mohammed"))
        .thenReturn(Future.successful(Some(mohammed)))

      when(mockRotasService.deleteUserFromRota(rota, mohammed))
        .thenReturn(Future.successful(Some(rota)))

      // WHEN the request is made
      val request = FakeRequest(DELETE, "/rotas/retrospective/users/%40Mohammed")
      val result = application.deleteUserFromRota("retrospective", "@Mohammed").apply(request)

      // THEN
      status(result) mustBe NO_CONTENT
    }

    "fail if the rota does not exist" in new WithSUT() {
      // GIVEN
      when(mockRotasService.retrieveRotaDescription("retrospective"))
        .thenReturn(Future.successful(None))

      val user = User("@Mohammed", 2)
      when(mockUsersService.getUserByName("@Mohammed"))
        .thenReturn(Future.successful(Some(user)))

      // WHEN
      val request = FakeRequest(DELETE, "/rotas/retrospective/users/%40Mohammed")
      val result = application.deleteUserFromRota("retrospective", "@Mohammed").apply(request)

      // THEN
      status(result) mustBe NOT_FOUND
      contentAsJson(result) mustBe Json.obj(
        "message" -> "Rota with name `retrospective` not found"
      )
    }

    "fail if the user does not exist" in new WithSUT() {
      // GIVEN
      val rota = Rota(
        name = "retrospective",
        description = Some("Reflect on the previous sprint"),
        assigned = Some(1)
      )
      when(mockRotasService.retrieveRotaDescription("retrospective"))
        .thenReturn(Future.successful(Some(rota)))

      when(mockUsersService.getUserByName("@Mohammed"))
        .thenReturn(Future.successful(None))

      // WHEN
      val request = FakeRequest(DELETE, "/rotas/retrospective/users/%40Mohammed")
      val result = application.deleteUserFromRota("retrospective", "@Mohammed").apply(request)

      // THEN
      status(result) mustBe NOT_FOUND
      contentAsJson(result) mustBe Json.obj(
        "message" -> "User with name `@Mohammed` not found"
      )
    }
  }

}

trait WithSUT extends WithApplication with MockitoSugar {
  val mockRotasService = mock[RotasService]
  val mockUsersService = mock[UsersService]
  val messagesApi = stubMessagesApi()
  val controllerComponents = stubControllerComponents()

  val application = new Application(
    mockRotasService,
    mockUsersService,
    messagesApi,
    controllerComponents
  )
}
