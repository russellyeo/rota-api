package repositories

import javax.inject.{Inject, Singleton}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.JdbcProfile

import models.dao.SlackBotDAO
import models.db.SlackBotDBModel

@Singleton()
class SlackBotRepository @Inject() (
    protected val dbConfigProvider: DatabaseConfigProvider
)(implicit executionContext: ExecutionContext)
    extends SlackBotsSchema
    with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  /** Insert or update a Slack bot
    *
    * @param bot
    *   the bot to insert or update
    */
  def upsert(bot: SlackBotDAO): Future[String] = {
    val botModel = SlackBotDBModel.fromDAO(bot)
    val upsertBot = bots.insertOrUpdate(botModel)

    val updateScopes = for {
      _ <- botScopes.filter(_.botId === bot.id).delete
      _ <- botScopes ++= bot.scopes.map(scope => (bot.id, scope))
    } yield ()

    val actions = for {
      _ <- upsertBot
      _ <- updateScopes
    } yield bot.id

    db.run(actions.transactionally)
  }

  /** Get a Slack bot
    *
    * @param id
    *   the ID of the bot to retreive
    */
  def get(id: String): Future[Option[SlackBotDAO]] = {
    for {
      maybeBotModel <- db.run(bots.filter(_.id === id).result.headOption)
      botScopes <- db.run(botScopes.filter(_.botId === id).result)
    } yield maybeBotModel.map(botModel => {
      val scopes = botScopes.map(_._2)
      SlackBotDAO.fromDBModel(botModel, scopes)
    })
  }
}

trait SlackBotsSchema { self: HasDatabaseConfigProvider[JdbcProfile] =>
  import profile.api._

  /** Query for the slack_bots table */
  lazy protected val bots = TableQuery[SlackBotsTable]

  /** Query for the slack_bot_scopes table */
  lazy protected val botScopes = TableQuery[SlackBotScopesTable]

  /** Definition of the slack_bots table */
  class SlackBotsTable(tag: Tag) extends Table[SlackBotDBModel](tag, "slack_bots") {
    def id = column[String]("id", O.PrimaryKey)
    def token = column[String]("token")
    def userId = column[String]("user_id")

    def * = (id, token, userId) <> ((SlackBotDBModel.apply _).tupled, SlackBotDBModel.unapply)
  }

  /** Definition of the slack_bot_scopes table */
  class SlackBotScopesTable(tag: Tag) extends Table[(String, String)](tag, "slack_bot_scopes") {
    def botId = column[String]("bot_id")
    def scope = column[String]("scope")

    def * = (botId, scope)

    // Foreign keys
    def botFK = foreignKey("bot_fk", botId, bots)(_.id)
  }
}
