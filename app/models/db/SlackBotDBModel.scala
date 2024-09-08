package models.db

import models.dao.SlackBotDAO

final case class SlackBotDBModel(
    id: String,
    token: String,
    userId: String
)

final object SlackBotDBModel {
  // Convert DAO to db model.
  def fromDAO(dao: SlackBotDAO): SlackBotDBModel = {
    SlackBotDBModel(
      id = dao.id,
      token = dao.token,
      userId = dao.userId
    )
  }
}
