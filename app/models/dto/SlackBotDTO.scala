package models.dto

final case class SlackBotDTO(
    id: String,
    scopes: Seq[String],
    token: String,
    userId: String
)

final object SlackBotDTO {
  import play.api.libs.json.{Json, OFormat}
  implicit val format: OFormat[SlackBotDTO] = Json.format[SlackBotDTO]
}
