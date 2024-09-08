package models.dto

final case class AddUsersToRotaDTO(users: Seq[String])

final object AddUsersToRotaDTO {
  // Convert to and from JSON
  import play.api.libs.json.{Json, OFormat}
  implicit val format: OFormat[AddUsersToRotaDTO] = Json.format[AddUsersToRotaDTO]
}
