package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.libs.json._

import scala.concurrent.{ ExecutionContext, Future }
import models.{ Rota, User }
import repositories.{ RotasRepository, UsersRepository, RotaUsersRepository }

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class Application @Inject() (
  rotasRepository: RotasRepository,
  usersRepository: UsersRepository,
  rotaUsersRepository: RotaUsersRepository,
  cc: ControllerComponents
)(implicit ec: ExecutionContext) extends AbstractController(cc) {
  
  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

}