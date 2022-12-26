package models

case class Rota(
    name: String,
    description: Option[String] = None,
    assigned: Option[Int] = None,
    id: Option[Int] = None
)

object Rota {
  import play.api.libs.json._
  import play.api.libs.functional.syntax._
  import play.api.data.validation._

  implicit val rotaReads: Reads[Rota] = (
    (JsPath \ "name")
      .read[String]
      .filter(JsonValidationError("min_length"))(_.length > 3) and
      (JsPath \ "description").readNullable[String] and
      (JsPath \ "assigned").readNullable[Int] and
      (JsPath \ "id").readNullable[Int]
  )(Rota.apply _)

  implicit val rotaWrites = new Writes[Rota] {
    def writes(rota: Rota) = Json.obj(
      "name" -> rota.name,
      "description" -> rota.description
    )
  }

}
