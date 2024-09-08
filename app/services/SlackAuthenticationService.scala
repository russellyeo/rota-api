package services

import javax.inject.{Inject}
import play.api.Logging
import scala.concurrent.{ExecutionContext, Future}

import models.dao._
import models.dto._
import repositories._

class SlackAuthenticationService @Inject() (
    slackBotRepository: SlackBotRepository,
    slackInstallationRepository: SlackInstallationRepository,
    slackTeamRepository: SlackTeamRepository,
    slackUserRepository: SlackUserRepository
)(implicit executionContext: ExecutionContext)
    extends Logging {

  /** Create or update an installation
    *
    * @param installation
    *   the installation to be upserted
    * @return
    *   the upserted installation ID
    */
  def create(installation: SlackInstallationDTO): Future[String] = {
    val installationDAO = SlackInstallationDAO.fromDTO(installation)
    val userDAO = SlackUserDAO.fromDTO(installation.user)
    val botDAO = SlackBotDAO.fromDTO(installation.bot)

    val teamOrEnterpriseDAOFuture: Future[SlackTeamDAO] =
      if (installation.isTeam) {
        installation.team
          .map(team => SlackTeamDAO.fromDTO(team, isEnterprise = false))
          .fold(Future.failed[SlackTeamDAO](new Exception("Team data is missing")))(
            Future.successful
          )
      } else if (installation.isEnterprise) {
        installation.enterprise
          .map(enterprise => SlackTeamDAO.fromDTO(enterprise, isEnterprise = true))
          .fold(Future.failed[SlackTeamDAO](new Exception("Enterprise data is missing")))(
            Future.successful
          )
      } else {
        Future.failed(new Exception("Must have either team or enterprise data"))
      }

    for {
      teamOrEnterpriseDAO <- teamOrEnterpriseDAOFuture
      _ <- slackTeamRepository.upsert(teamOrEnterpriseDAO)
      _ <- slackUserRepository.upsert(userDAO)
      _ <- slackBotRepository.upsert(botDAO)
      _ <- slackInstallationRepository.upsert(installationDAO)
    } yield installationDAO.teamId
  }

  /** Retrieve an installation
    *
    * @param id
    *   the id of the installation to retrieve
    * @return
    *   the requested installation if it exists
    */
  def get(id: String): Future[Option[SlackInstallationDTO]] = {
    for {
      maybeInstallationDAO <- slackInstallationRepository.get(id)
      maybeTeamDAO <- maybeInstallationDAO.fold(Future.successful[Option[SlackTeamDAO]](None))(
        installation => slackTeamRepository.get(installation.teamId)
      )
      maybeUserDAO <- maybeInstallationDAO.fold(Future.successful[Option[SlackUserDAO]](None))(
        installation => slackUserRepository.get(installation.userId)
      )
      maybeBotDAO <- maybeInstallationDAO.fold(Future.successful[Option[SlackBotDAO]](None))(
        installation => slackBotRepository.get(installation.botId)
      ),
    } yield {
      (maybeInstallationDAO, maybeTeamDAO, maybeUserDAO, maybeBotDAO) match {
        case (Some(installationDAO), Some(teamDAO), Some(userDAO), Some(botDAO)) =>
          Some(
            SlackInstallationDTO.fromDAOs(installationDAO, teamDAO, userDAO, botDAO)
          )
        case _ => None
      }
    }

  }

  /** Delete an installation
    *
    * @param installation
    *   the installation to delete
    * @return
    *   the ID of the deleted installation
    */
  def delete(id: String): Future[String] = {
    slackInstallationRepository.delete(id).map(_ => id)
  }
}
