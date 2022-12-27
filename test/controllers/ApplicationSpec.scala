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
import services.RotasService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ApplicationSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {

  "GET /rota/:id" should {

    "retrieve a rota with an assigned user and some unassigned users" in new WithSUT() {
      // GIVEN a rota with an assigned user and some unassigned users
      val rota = Rota(
        name = "Retrospective",
        description = None,
        assigned = Some(1)
      )
      val user1 = User("Sofia", 1)
      val user2 = User("Emma", 2)
      val user3 = User("Aiden", 3)
      val users = Seq(user1, user2, user3)

      when(mockRotasService.retrieve(1))
        .thenReturn(
          Future.successful(Some(RotaWithUsers(rota, Some(user1), users)))
        )

      // WHEN we make a request to retrieve the Rota
      val result = application.rota(1).apply(FakeRequest(GET, "/rotas/1"))

      // THEN the response is OK and returns the expected JSON
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

    "retrieve a rota no assigned user and some unassigned users" in new WithSUT() {
      // GIVEN a rota with no assigned user and some unassigned users
      val rota = Rota(
        name = "Sprint Planning",
        description = Some("Assign tickets to the next Sprint"),
        assigned = None
      )
      val user1 = User("Yara", 1)
      val user2 = User("Ravi", 2)
      val user3 = User("Isabella", 3)
      val users = Seq(user1, user2, user3)

      when(mockRotasService.retrieve(1))
        .thenReturn(
          Future.successful(Some(RotaWithUsers(rota, None, users)))
        )

      // WHEN we make a request to retrieve the Rota
      val result = application.rota(1).apply(FakeRequest(GET, "/rotas/1"))

      // THEN the response is OK and returns the expected JSON
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

    "not retrieve a rota with id that does not exist" in new WithSUT() {
      // GIVEN there is no rota with id 1
      when(mockRotasService.retrieve(1)).thenReturn(Future.successful(None))

      // WHEN we make a request to retrieve a Rota with id 1
      val result = application.rota(1).apply(FakeRequest(GET, "/rotas/1"))

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
      val rota = Rota("Sprint Planning", Some("Assign tickets"))
      val created = rota.copy(id = Some(1))

      when(mockRotasService.create(rota))
        .thenReturn(Future.successful(created))

      // WHEN we make a request to create the Rota
      val request = FakeRequest(POST, "/rotas").withBody(Json.toJson(rota))
      val result = application.createRota().apply(request)

      // THEN the rota is created
      status(result) mustBe CREATED
      contentAsJson(result) mustBe Json.toJson(created)
      verify(mockRotasService).create(rota)
    }

    "create a rota without a description" in new WithSUT() {
      // GIVEN a rota without a description
      val rota = Rota("Retrospective")
      val created = rota.copy(id = Some(1))

      when(mockRotasService.create(rota))
        .thenReturn(Future.successful(created))

      // WHEN we make a request to create the Rota
      val request = FakeRequest(POST, "/rotas").withBody(Json.toJson(rota))
      val result = application.createRota().apply(request)

      // THEN the rota is created
      status(result) mustBe CREATED
      contentAsJson(result) mustBe Json.toJson(created)
      verify(mockRotasService).create(rota)
    }

    "not create a rota with a missing name parameter" in new WithSUT() {
      // GIVEN a rota without a description
      val rota = Json.obj(
        "title" -> "Retrospective",
        "desc" -> "Review the sprint"
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
        "name" -> "Standup",
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

  "DELETE /rotas/:id" should {
    "delete an existing rota" in new WithSUT() {
      // GIVEN the rota will be deleted successfully
      when(mockRotasService.delete(1))
        .thenReturn(Future.successful(1))

      // WHEN we make a request to delete the rota
      val result =
        application.deleteRota(1).apply(FakeRequest(DELETE, "/rotas/1"))

      // THEN the rota is deleted
      status(result) mustBe OK
      verify(mockRotasService).delete(1)
    }

    "fail to delete a non-existing rota" in new WithSUT() {
      // GIVEN the rota will be not be deleted successfully
      when(mockRotasService.delete(1))
        .thenReturn(Future.successful(0))

      // WHEN we make a request to delete the rota
      val result =
        application.deleteRota(1).apply(FakeRequest(DELETE, "/rotas/1"))

      // THEN the rota is not deleted
      status(result) mustBe NOT_FOUND
      contentAsJson(result) mustBe Json.obj(
        "message" -> "error.resourceNotFound"
      )
      verify(mockRotasService).delete(1)
    }
  }

}

trait WithSUT extends WithApplication with MockitoSugar {
  val mockRotasService = mock[RotasService]
  val messagesApi = stubMessagesApi()
  val controllerComponents = stubControllerComponents()

  val application = new Application(
    mockRotasService,
    messagesApi,
    controllerComponents
  )
}
