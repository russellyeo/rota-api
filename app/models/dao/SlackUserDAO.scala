package models.dao

import models.dto.SlackUserDTO
import models.db.SlackUserDBModel

final case class SlackUserDAO(
    id: String,
    token: Option[String],
    scopes: Option[Seq[String]]
)

object SlackUserDAO {
  // Convert DTO to DAO.
  def fromDTO(dto: SlackUserDTO): SlackUserDAO = {
    SlackUserDAO(
      id = dto.id,
      token = dto.token,
      scopes = dto.scopes
    )
  }
  // Convert DB model to DAO, hydrated with scopes.
  def fromDBModel(model: SlackUserDBModel, scopes: Option[Seq[String]]): SlackUserDAO = {
    SlackUserDAO(
      id = model.id,
      token = model.token,
      scopes = scopes
    )
  }
}
