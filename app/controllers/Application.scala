package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.libs.json._

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
    cc: ControllerComponents
)(implicit ec: ExecutionContext)
    extends AbstractController(cc) {

  /** Create an Action to render an HTML page.
    *
    * The configuration in the `routes` file means that this method will be
    * called when the application receives a `GET` request with a path of `/`.
    */
  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

  /** Handles request for getting all rotas from the database
    */
  def list: Action[AnyContent] =
    Action.async {
      rotasRepository.list().map { result =>
        Ok(Json.toJson(result))
      }
    }

  /** Handles request for getting a rota and related users from the database
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

}
