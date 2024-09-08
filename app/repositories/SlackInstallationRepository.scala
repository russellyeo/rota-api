package repositories

import javax.inject.{Inject, Singleton}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.JdbcProfile

import models.dao.SlackInstallationDAO

@Singleton()
class SlackInstallationRepository @Inject() (
    protected val dbConfigProvider: DatabaseConfigProvider
)(implicit executionContext: ExecutionContext)
    extends SlackInstallationsSchema
    with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  /** Insert or update an installation
    *
    * @param installation
    *   the installation to insert or update
    */
  def upsert(installation: SlackInstallationDAO): Future[String] = {
    val query = installations.insertOrUpdate(installation)
    db.run(query).map(_ => installation.teamId)
  }

  /** Get installation
    *
    * @param teamId
    *   the team ID for the installation to retreive
    * @return
    *   the requested installation if it exists
    */
  def get(teamId: String): Future[Option[SlackInstallationDAO]] =
    db.run(installations.filter(_.teamId === teamId).result.headOption)

  /** Delete installation
    *
    * @param teamId
    *   the team ID of the installation to delete
    */
  def delete(teamId: String): Future[Unit] =
    db.run(installations.filter(_.teamId === teamId).delete).map(_ => ())
}

trait SlackInstallationsSchema { self: HasDatabaseConfigProvider[JdbcProfile] =>
  import profile.api._

  /** Query for the slack_installations table */
  lazy protected val installations = TableQuery[SlackInstallationsTable]

  /** Definition of the slack_installations table */
  class SlackInstallationsTable(tag: Tag)
      extends Table[SlackInstallationDAO](tag, "slack_installations") {

    def teamId = column[String]("team_id", O.PrimaryKey)
    def userId = column[String]("user_id")
    def tokenType = column[String]("token_type")
    def isEnterpriseInstall = column[Boolean]("is_enterprise_install")
    def appId = column[String]("app_id")
    def authVersion = column[String]("auth_version")
    def botId = column[String]("bot_id")

    def * = (
      teamId,
      userId,
      tokenType,
      isEnterpriseInstall,
      appId,
      authVersion,
      botId
    ) <> ((SlackInstallationDAO.apply _).tupled, SlackInstallationDAO.unapply)
  }
}
