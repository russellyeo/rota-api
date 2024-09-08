package models.dto

import models.dao.SlackUserDAO

final case class SlackUserDTO(
    id: String,
    token: Option[String],
    scopes: Option[Seq[String]]
)

final object SlackUserDTO {
  // Convert to and from JSON
  import play.api.libs.json.{Json, OFormat}
  implicit val format: OFormat[SlackUserDTO] = Json.format[SlackUserDTO]
  // Convert from DAO
  def fromDAO(dao: SlackUserDAO): SlackUserDTO = {
    SlackUserDTO(
      id = dao.id,
      token = dao.token,
      scopes = dao.scopes
    )
  }
}
