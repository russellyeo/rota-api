package models.dao

import models.dto.SlackTeamDTO

final case class SlackTeamDAO(
    id: String,
    name: String,
    isEnterprise: Boolean
)

object SlackTeamDAO {
  // Convert DTO to DAO.
  def fromDTO(dto: SlackTeamDTO, isEnterprise: Boolean): SlackTeamDAO = {
    SlackTeamDAO(
      id = dto.id,
      name = dto.name,
      isEnterprise = isEnterprise
    )
  }
}
