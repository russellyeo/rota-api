package repositories

import javax.inject.{Inject, Singleton}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.JdbcProfile

import models.dao.SlackUserDAO
import models.db.SlackUserDBModel

@Singleton()
class SlackUserRepository @Inject() (
    protected val dbConfigProvider: DatabaseConfigProvider
)(implicit executionContext: ExecutionContext)
    extends SlackUsersSchema
    with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  /** Insert or update a Slack user
    *
    * @param user
    *   the user to insert or update
    */
  def upsert(user: SlackUserDAO): Future[String] = {
    val userModel = SlackUserDBModel.fromDAO(user)
    val upsertUser = users.insertOrUpdate(userModel)

    val updateScopes = for {
      _ <- userScopes.filter(_.userId === user.id).delete
      _ <- userScopes ++= user.scopes.getOrElse(Seq.empty).map(scope => (user.id, scope))
    } yield ()

    val actions = for {
      _ <- upsertUser
      _ <- updateScopes
    } yield user.id

    db.run(actions.transactionally)
  }

  /** Get a Slack user
    *
    * @param id
    *   the ID of the user
    */
  def get(id: String): Future[Option[SlackUserDAO]] = {
    for {
      maybeUserModel <- db.run(users.filter(_.id === id).result.headOption)
      userScopes <- db.run(userScopes.filter(_.userId === id).result)
    } yield maybeUserModel.map(userModel => {
      val scopes = userScopes.map(_._2)
      SlackUserDAO.fromDBModel(userModel, if (scopes.isEmpty) None else Some(scopes))
    })
  }
}

trait SlackUsersSchema { self: HasDatabaseConfigProvider[JdbcProfile] =>
  import profile.api._

  /** Query for the slack_users table */
  lazy protected val users = TableQuery[SlackUsersTable]

  /** Query for the slack_user_scopes table */
  lazy protected val userScopes = TableQuery[SlackUserScopesTable]

  /** Definition of the slack_users table */
  class SlackUsersTable(tag: Tag) extends Table[SlackUserDBModel](tag, "slack_users") {
    def id = column[String]("id", O.PrimaryKey)
    def token = column[Option[String]]("token")

    def * = (id, token) <> ((SlackUserDBModel.apply _).tupled, SlackUserDBModel.unapply)
  }

  /** Definition of the slack_user_scopes table */
  class SlackUserScopesTable(tag: Tag) extends Table[(String, String)](tag, "slack_user_scopes") {
    def userId = column[String]("user_id")
    def scope = column[String]("scope")

    def * = (userId, scope)

    // Foreign keys
    def userFK = foreignKey("user_fk", userId, users)(_.id)
  }
}
