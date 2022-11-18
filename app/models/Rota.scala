package models

case class Rota(
    name: String,
    description: Option[String]=None,
    assigned: Option[Int]=None,
    id: Int=0
)

object Rota { 
    import play.api.libs.json.{Json, OFormat}
    implicit val format: OFormat[Rota] = Json.format[Rota]
}

case class RotaUser(rotaID: Int, userID: Int)