package models

case class RotaUser(rotaName: String, userID: Int)

object RotaUser {
  import play.api.libs.json.{Json, OFormat}
  implicit val format: OFormat[RotaUser] = Json.format[RotaUser]
}
