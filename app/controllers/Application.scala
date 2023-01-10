package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.i18n._

import scala.concurrent.{ExecutionContext, Future}
import models.dto._
import models._
import services._

import scala.collection.Seq

/** The main application controller */
@Singleton
class Application @Inject() (
    rotasService: RotasService,
    usersService: UsersService,
    messagesApi: MessagesApi,
    cc: ControllerComponents
)(implicit ec: ExecutionContext)
    extends AbstractController(cc) {

  implicit val lang: Lang = Lang("en")

  /** Handles request for retrieving all rotas
    */
  def listRotas: Action[AnyContent] =
    Action.async {
      rotasService.list().map { result =>
        Ok(Json.toJson(result))
      }
    }

  /** Handles request for creating a new rota
    */
  def createRota(): Action[JsValue] =
    Action.async(parse.json) { request =>
      request.body
        .validate[Rota]
        .fold(
          errors => {
            val errorMessage = validationErrorMessage(errors)
            Future.successful(BadRequest(errorMessage))
          },
          rota => {
            rotasService.create(rota).map { result =>
              Created(Json.toJson(result))
            }
          }
        )
    }

  /** Handles request to retrieve a rota with its assigned user and all unassigned users
    */
  def retrieveRota(id: Int): Action[AnyContent] =
    Action.async {
      rotasService.retrieve(id).map { result =>
        result match {
          case Some(rotaWithUsers) => Ok(Json.toJson(rotaWithUsers))
          case None                => NotFound(notFoundErrorMessage(id, "rota"))
        }
      }
    }

  /** Handles request for updating a rota's details
    */
  def updateRota(id: Int): Action[JsValue] =
    Action.async(parse.json) { request =>
      request.body
        .validate[UpdateRotaDTO]
        .fold(
          errors => {
            val errorMessage = validationErrorMessage(errors)
            Future.successful(BadRequest(errorMessage))
          },
          rota => {
            rotasService
              .update(
                id = id,
                name = rota.name,
                description = rota.description,
                assigned = rota.assigned
              )
              .map { updated =>
                updated match {
                  case Some(rota) => Ok(request.body)
                  case None       => NotFound(notFoundErrorMessage(id, "rota"))
                }
              }
          }
        )
    }

  /** Handles request for deleting a rota
    */
  def deleteRota(id: Int): Action[AnyContent] =
    Action.async {
      rotasService.delete(id).map { result =>
        result match {
          case 0 => NotFound(notFoundErrorMessage(id, "rota"))
          case _ => Ok
        }
      }
    }

  def addUsersToRota(id: Int): Action[JsValue] =
    Action.async(parse.json) { request =>
      request.body
        .validate[AddUsersToRotaDTO]
        .fold(
          errors => {
            val errorMessage = validationErrorMessage(errors)
            Future.successful(BadRequest(errorMessage))
          },
          usersToAdd => {
            for {
              users <- usersService.createUsersIfNeeded(usersToAdd.users)
              _ <- rotasService.addUsersToRota(id, users)
              rota <- rotasService.retrieve(id)
            } yield {
              Ok(Json.toJson(rota))
            }
          }
        )
    }

  private def validationErrorMessage(
      errors: Seq[(JsPath, scala.collection.Seq[JsonValidationError])]
  ): JsObject = {
    // Handle only the first validation error of the first field with validation errors
    val error = errors.head._2.head
    Json.obj(
      "message" -> messagesApi(error.message, error.args: _*)
    )
  }

  private def notFoundErrorMessage(id: Int, resource: String): JsObject =
    Json.obj(
      "message" -> messagesApi("error.resourceNotFound", resource, id)
    )

}
