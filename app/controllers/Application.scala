package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.i18n._

import scala.concurrent.{ExecutionContext, Future}
import models._
import services._

/** The main Application controller */
@Singleton
class Application @Inject() (
    rotasService: RotasService,
    messagesApi: MessagesApi,
    cc: ControllerComponents
)(implicit ec: ExecutionContext)
    extends AbstractController(cc) {

  implicit val lang: Lang = Lang("en")

  /** Handles request for retrieving all Rotas
    */
  def list: Action[AnyContent] =
    Action.async {
      rotasService.list().map { result =>
        Ok(Json.toJson(result))
      }
    }

  /** Handles request to retrieve a Rota with its assigned user and all
    * unassigned users
    */
  def rota(id: Int): Action[AnyContent] =
    Action.async {
      rotasService.retrieve(id).map { result =>
        result match {
          case Some(rotaWithUsers) =>
            Ok(Json.toJson(rotaWithUsers))
          case None =>
            val error = Json.obj(
              "message" -> messagesApi("error.resourceNotFound", "Rota", id)
            )
            NotFound(error)
        }
      }
    }

  /** Handles request for creating a new Rota
    */
  def createRota(): Action[JsValue] =
    Action.async(parse.json) { request =>
      request.body
        .validate[Rota]
        .fold(
          errors => {
            // Handle only the first validation error of the first field with validation errors
            val error = errors.head._2.head
            val response = Json.obj(
              "message" -> messagesApi(error.message, error.args: _*)
            )
            Future.successful(BadRequest(response))
          },
          rota => {
            rotasService.create(rota).map { result =>
              Created(Json.toJson(result))
            }
          }
        )
    }

}
