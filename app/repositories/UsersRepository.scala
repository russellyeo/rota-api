package repositories

import java.util.Date
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

  /** Count the number of users */
  def count(): Future[Int] =
    db.run(users.map(_.name).length.result)

  /** Get user with ID */
  def get(id: Int): Future[Option[User]] =
    db.run(users.filter(_.id === id).result.headOption)

  /** Get a list of users from a list of IDs */
  def getList(ids: Seq[Int]): Future[Seq[User]] =
    db.run(users.filter(_.id inSet ids).result)

  /** Insert a new user */
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
