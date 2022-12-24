package models

case class Rota(
    name: String,
    description: Option[String] = None,
    assigned: Option[Int] = None,
    id: Int = 0
)

object Rota {
  import play.api.libs.json.{Json, Writes}

  implicit val writes = new Writes[Rota] {
    def writes(rota: Rota) = Json.obj(
      "name" -> rota.name,
      "description" -> rota.description
    )
  }
}
