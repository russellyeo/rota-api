package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.i18n._

import scala.concurrent.{ExecutionContext, Future}
import models.{Rota, User, RotaWithUsers}
import repositories.{RotasRepository, UsersRepository, RotaUsersRepository}

/** This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class Application @Inject() (
    rotasRepository: RotasRepository,
    usersRepository: UsersRepository,
    rotaUsersRepository: RotaUsersRepository,
    messagesApi: MessagesApi,
    cc: ControllerComponents
)(implicit ec: ExecutionContext)
    extends AbstractController(cc) {

  implicit val lang: Lang = Lang("en")

  /** Handles request for getting all rotas
    */
  def list: Action[AnyContent] =
    Action.async {
      rotasRepository.list().map { result =>
        Ok(Json.toJson(result))
      }
    }

  /** Handles request for getting a rota and related users
    */
  def rota(id: Int): Action[AnyContent] =
    Action.async {
      rotasRepository.get(id).flatMap { rota =>
        rota match {
          case Some(rota) =>
            for {
              rotaUsers <- rotaUsersRepository.getRotaUsersWithRotaID(id)
              users <- usersRepository.getList(rotaUsers.map(_.userID))
              assigned <- rota.assigned
                .map(usersRepository.get)
                .getOrElse(Future.successful(None))
            } yield {
              Ok(Json.toJson(RotaWithUsers(rota, assigned, users)))
            }
          case None =>
            val error = Json.obj("message" -> s"Rota with id $id not found")
            Future.successful(NotFound(error))
        }
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
            // Handle only the first validation error of the first field with validation errors
            val error = errors.head._2.head
            val response = Json.obj(
              "message" -> messagesApi(error.message, error.args: _*)
            )
            Future.successful(BadRequest(response))
          },
          rota => {
            rotasRepository.insert(rota).map { result =>
              Created(Json.toJson(result))
            }
          }
        )
    }

}
