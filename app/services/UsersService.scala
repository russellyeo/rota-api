package services

import javax.inject.{Inject}

import scala.concurrent.{ExecutionContext, Future}

import repositories._
import models._

class UsersService @Inject() (
    usersRepository: UsersRepository,
    rotaUsersRepository: RotaUsersRepository
)(implicit executionContext: ExecutionContext) {

  /** Get a user by name
    *
    * @param name
    *   the name of the user
    * @return
    *   the user if it exists
    */
  def getUserByName(name: String): Future[Option[User]] = {
    usersRepository.retrieve(name)
  }

  /** Create a new user if needed
    *
    * @param name
    *   the name of the user
    * @return
    *   the existing or newly created user
    */
  def createUserIfNeeded(name: String): Future[User] = {
    usersRepository.retrieve(name).flatMap { user =>
      user match {
        case Some(user) =>
          Future.successful(user)
        case None =>
          val user = User(name)
          usersRepository.insert(user)
          Future.successful(user)
      }
    }
  }

  /** Create a list of new users if needed
    *
    * @param names
    *   the names of the users to create
    * @return
    *   the existing or newly created users
    */
  def createUsersIfNeeded(names: Seq[String]): Future[Seq[User]] = {
    Future.sequence(names.map(createUserIfNeeded))
  }

}
