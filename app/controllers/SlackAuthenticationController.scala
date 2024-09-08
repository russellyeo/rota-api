package controllers

import javax.inject._
import play.api._
import play.api.libs.json._
import play.api.Logger
import play.api.i18n._
import play.api.mvc._
import scala.concurrent.{ExecutionContext, Future}

import models.dto._
import services.SlackAuthenticationService

/** The Slack authentication controller */
@Singleton
class SlackAuthenticationController @Inject() (
    slackAuthenticationService: SlackAuthenticationService,
    cc: ControllerComponents
)(implicit ec: ExecutionContext)
    extends AbstractController(cc) {

  private val logger = Logger(this.getClass)

  /** Handles request for retrieving an installation */
  def getInstallation(id: String): Action[AnyContent] =
    Action.async {
      slackAuthenticationService.get(id).map { result =>
        result match {
          case Some(installation) => Ok(Json.toJson(installation))
          case None               => NotFound(Json.obj("error" -> "Installation not found"))
        }
      }
    }

  /** Handles request for creating a new installation */
  def createInstallation(): Action[JsValue] = {
    Action.async(parse.json) { request =>
      request.body
        .validate[SlackInstallationResponseDTO]
        .fold(
          errors => {
            logger
              .error(s"Failed to validate SlackInstallationResponseDTO: ${JsError.toJson(errors)}")
            Future.successful(BadRequest(Json.obj("error" -> JsError.toJson(errors))))
          },
          response => {
            logger.info(
              s"Attempting to create ${if (response.installation.isTeam) "team" else "enterprise"} installation: " +
                s"id=${response.installation.id}, " +
                s"name=${response.installation.name}, " +
                s"isEnterpriseInstall=${response.installation.isEnterpriseInstall}"
            )
            slackAuthenticationService
              .create(response.installation)
              .map { result =>
                logger.info(s"Successfully created installation for ${response.installation.id}")
                Created(Json.toJson(result))
              }
              .recover { case e: Exception =>
                logger.error(s"Failed to create installation for ${response.installation.id}", e)
                InternalServerError(
                  Json.obj("error" -> s"Failed to create installation: ${e.getMessage}")
                )
              }
          }
        )
    }
  }

  /** Handles request for deleting an installation */
  def deleteInstallation(id: String): Action[AnyContent] =
    Action.async {
      slackAuthenticationService.delete(id).map { _ =>
        Ok(Json.obj("message" -> "Installation deleted"))
      }
    }
}
