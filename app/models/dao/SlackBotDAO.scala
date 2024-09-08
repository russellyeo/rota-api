package models.dao

import models.dto.SlackBotDTO
import models.db.SlackBotDBModel

final case class SlackBotDAO(
    id: String,
    scopes: Seq[String],
    token: String,
    userId: String
)

final object SlackBotDAO {
  // Convert DTO to DAO.
  def fromDTO(dto: SlackBotDTO): SlackBotDAO = {
    SlackBotDAO(
      id = dto.id,
      scopes = dto.scopes,
      token = dto.token,
      userId = dto.userId
    )
  }
  // Convert DAO to DTO
  def fromDAO(dao: SlackBotDAO): SlackBotDTO = {
    SlackBotDTO(
      id = dao.id,
      scopes = dao.scopes,
      token = dao.token,
      userId = dao.userId
    )
  }
  // Convert from DB model, hydrated with scopes.
  def fromDBModel(model: SlackBotDBModel, scopes: Seq[String]): SlackBotDAO = {
    SlackBotDAO(
      id = model.id,
      scopes = scopes,
      token = model.token,
      userId = model.userId
    )
  }
}
