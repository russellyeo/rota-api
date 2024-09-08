package models.dto

import models.dao.{SlackBotDAO, SlackInstallationDAO, SlackTeamDAO, SlackUserDAO}

final case class SlackInstallationResponseDTO(
    installation: SlackInstallationDTO
)

final object SlackInstallationResponseDTO {
  // Convert to and from JSON
  import play.api.libs.json.{Json, OFormat}
  implicit val format: OFormat[SlackInstallationResponseDTO] =
    Json.format[SlackInstallationResponseDTO]
}

final case class SlackInstallationDTO(
    team: Option[SlackTeamDTO],
    enterprise: Option[SlackTeamDTO],
    user: SlackUserDTO,
    tokenType: String,
    isEnterpriseInstall: Boolean,
    appId: String,
    authVersion: String,
    bot: SlackBotDTO
) {
  // Method to get the team or enterprise ID
  def id: String = team
    .map(_.id)
    .orElse(enterprise.map(_.id))
    .getOrElse(
      throw new IllegalStateException("Both team and enterprise are None")
    )
  // Method to get the team or enterprise name
  def name: String = team
    .map(_.name)
    .orElse(enterprise.map(_.name))
    .getOrElse(
      throw new IllegalStateException("Both team and enterprise are None")
    )
  // Determine if this is a team or enterprise installation
  def isTeam: Boolean = team.isDefined
  def isEnterprise: Boolean = enterprise.isDefined
}

final object SlackInstallationDTO {
  // Convert to and from JSON
  import play.api.libs.json.{Json, OFormat}
  implicit val format: OFormat[SlackInstallationDTO] = Json.format[SlackInstallationDTO]

  // Convert from DAOs
  def fromDAOs(
      installationDAO: SlackInstallationDAO,
      teamDAO: SlackTeamDAO,
      userDAO: SlackUserDAO,
      botDAO: SlackBotDAO
  ): SlackInstallationDTO = {
    val team = SlackTeamDTO.fromDAO(teamDAO)
    SlackInstallationDTO(
      team = if (teamDAO.isEnterprise) None else Some(team),
      enterprise = if (teamDAO.isEnterprise) Some(team) else None,
      user = SlackUserDTO.fromDAO(userDAO),
      tokenType = installationDAO.tokenType,
      isEnterpriseInstall = installationDAO.isEnterpriseInstall,
      appId = installationDAO.appId,
      authVersion = installationDAO.authVersion,
      bot = SlackBotDAO.fromDAO(botDAO)
    )
  }
}
