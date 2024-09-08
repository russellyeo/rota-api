package models.dto

import models.dao.SlackTeamDAO

final case class SlackTeamDTO(id: String, name: String)

final object SlackTeamDTO {
  // Convert to and from JSON
  import play.api.libs.json.{Json, OFormat}
  implicit val format: OFormat[SlackTeamDTO] = Json.format[SlackTeamDTO]
  // Convert from DAO
  def fromDAO(dao: SlackTeamDAO): SlackTeamDTO = {
    SlackTeamDTO(
      id = dao.id,
      name = dao.name
    )
  }
}
