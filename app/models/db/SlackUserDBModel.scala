package models.db

import models.dao.SlackUserDAO

final case class SlackUserDBModel(
    id: String,
    token: Option[String]
)

final object SlackUserDBModel {
  // Convert DAO to db model.
  def fromDAO(dao: SlackUserDAO): SlackUserDBModel = {
    SlackUserDBModel(
      id = dao.id,
      token = dao.token
    )
  }
}
