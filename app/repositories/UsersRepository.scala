package repositories

import javax.inject.{Inject, Singleton}

import models.User
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class UsersRepository @Inject() (
    protected val dbConfigProvider: DatabaseConfigProvider
)(implicit executionContext: ExecutionContext)
    extends UsersComponent
    with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  /** Count the number of users
    *
    * @return
    *   the number of users in the database
    */
  def count(): Future[Int] =
    db.run(users.map(_.name).length.result)

  /** Retrieve user by ID
    *
    * @param id
    *   the ID of the user to retreive
    * @return
    *   the requested user if it exists
    */
  def retrieve(id: Int): Future[Option[User]] =
    db.run(users.filter(_.id === id).result.headOption)

  /** Retrieve a list of users from a list of IDs
    *
    * @param ids
    *   the IDs of the users to retrieve
    * @return
    *   a list of requested users
    */
  def retrieveList(ids: Seq[Int]): Future[Seq[User]] =
    db.run(users.filter(_.id inSet ids).result)

  /** Insert a new rota
    *
    * @param user
    *   the user to be inserted
    */
  def insert(user: User): Future[User] =
    db.run(usersReturningRow += user)

}

trait UsersComponent { self: HasDatabaseConfigProvider[JdbcProfile] =>
  import profile.api._

  /** Query for the USERS table */
  lazy protected val users = TableQuery[Users]

  /** Query for the USERS table, returning a copy of the row after a database
    * operation
    */
  lazy protected val usersReturningRow = users returning users.map(_.id) into {
    (rota, id) =>
      rota.copy(id = id)
  }

  /** Definition of the USERS table */
  class Users(tag: Tag) extends Table[User](tag, "USERS") {
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    def name = column[String]("NAME")
    def * = (name, id) <> ((User.apply _).tupled, User.unapply _)
  }
}
