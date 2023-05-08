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
  def retrieveRota(name: String): Action[AnyContent] =
    Action.async {
      rotasService.retrieve(name).map { result =>
        result match {
          case Some(rotaWithUsers) => Ok(Json.toJson(rotaWithUsers))
          case None                => NotFound(notFoundErrorMessage(name, "rota"))
        }
      }
    }

  /** Handles request for updating a rota's details
    */
  def updateRota(name: String): Action[JsValue] =
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
                name = name,
                description = rota.description,
                assigned = rota.assigned
              )
              .map { updated =>
                updated match {
                  case Some(rota) => Ok(request.body)
                  case None       => NotFound(notFoundErrorMessage(name, "rota"))
                }
              }
          }
        )
    }

  /** Handles request for deleting a rota
    */
  def deleteRota(name: String): Action[AnyContent] =
    Action.async {
      rotasService.delete(name).map { result =>
        result match {
          case 0 => NotFound(notFoundErrorMessage(name, "rota"))
          case _ => Ok
        }
      }
    }

  /** Handles request for updating the users in a rota
    */
  def addUsersToRota(name: String): Action[JsValue] =
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
              _ <- rotasService.addUsersToRota(name, users)
              rota <- rotasService.retrieve(name)
            } yield {
              Ok(Json.toJson(rota))
            }
          }
        )
    }

  def rotateRota(name: String): Action[AnyContent] =
    Action.async {
      for {
        _ <- rotasService.rotate(name)
        updated <- rotasService.retrieve(name)
      } yield {
        Ok(Json.toJson(updated))
      }
    }

  def getUserByName(name: String): Action[AnyContent] =
    Action.async {
      usersService.getUserByName(name).flatMap { result =>
        val response = result match {
          case Some(user) => Ok(Json.toJson(user))
          case _ =>
            NotFound(
              Json.obj(
                "message" -> s"User $name was not found"
              )
            )
        }
        Future.successful(response)
      }
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

  private def notFoundErrorMessage(name: String, resource: String): JsObject =
    Json.obj(
      "message" -> messagesApi("error.resourceNotFound", resource, name)
    )

}
