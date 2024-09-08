package repositories

import javax.inject.{Inject, Singleton}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.JdbcProfile

import models.dao.SlackTeamDAO

@Singleton()
class SlackTeamRepository @Inject() (
    protected val dbConfigProvider: DatabaseConfigProvider
)(implicit executionContext: ExecutionContext)
    extends SlackTeamsSchema
    with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  /** Insert or update a Slack team
    *
    * @param team
    *   the team to insert or update
    */
  def upsert(team: SlackTeamDAO): Future[String] = {
    val query = teams.insertOrUpdate(team)
    db.run(query).map(_ => team.id)
  }

  /** Get a Slack team
    *
    * @param id
    *   the ID of the team to retrieve
    */
  def get(id: String): Future[Option[SlackTeamDAO]] = {
    db.run(teams.filter(_.id === id).result.headOption)
  }
}

trait SlackTeamsSchema { self: HasDatabaseConfigProvider[JdbcProfile] =>
  import profile.api._

  /** Query for the slack_teams table */
  lazy protected val teams = TableQuery[SlackTeamsTable]

  /** Definition of the slack_teams table */
  class SlackTeamsTable(tag: Tag) extends Table[SlackTeamDAO](tag, "slack_teams") {
    def id = column[String]("id", O.PrimaryKey)
    def team = column[String]("team")
    def is_enterprise = column[Boolean]("is_enterprise")

    def * = (id, team, is_enterprise) <> ((SlackTeamDAO.apply _).tupled, SlackTeamDAO.unapply)
  }
}
