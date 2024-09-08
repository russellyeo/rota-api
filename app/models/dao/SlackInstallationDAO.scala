package models.dao

import models.dto.SlackInstallationDTO

final case class SlackInstallationDAO(
    teamId: String,
    userId: String,
    tokenType: String,
    isEnterpriseInstall: Boolean,
    appId: String,
    authVersion: String,
    botId: String
)

final object SlackInstallationDAO {
  // Convert DTO to DAO.
  def fromDTO(dto: SlackInstallationDTO): SlackInstallationDAO = {
    val teamId = dto.enterprise.map(_.id).orElse(dto.team.map(_.id)) match {
      case Some(value) => value
      case None =>
        throw new IllegalArgumentException("Both enterprise id and team id cannot be None")
    }
    SlackInstallationDAO(
      teamId = teamId,
      userId = dto.user.id,
      tokenType = dto.tokenType,
      isEnterpriseInstall = dto.isEnterpriseInstall,
      appId = dto.appId,
      authVersion = dto.authVersion,
      botId = dto.bot.id
    )
  }
}
