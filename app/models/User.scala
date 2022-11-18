package models

case class User(
    name: String,
    id: Int=0
)

object User { 
    import play.api.libs.json.{Json, OFormat}
    implicit val format: OFormat[User] = Json.format[User]
}