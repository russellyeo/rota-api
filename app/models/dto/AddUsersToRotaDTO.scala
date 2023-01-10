package models.dto

case class AddUsersToRotaDTO(users: Seq[String])

object AddUsersToRotaDTO {
  import play.api.libs.json.{Json, OFormat}
  implicit val format: OFormat[AddUsersToRotaDTO] = Json.format[AddUsersToRotaDTO]
}
