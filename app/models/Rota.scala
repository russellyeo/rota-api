package models

case class Rota(
    name: String,
    description: Option[String] = None,
    assigned: Option[Int] = None
)

object Rota {
  import play.api.libs.json._
  import play.api.libs.functional.syntax._
  import play.api.data.validation._

  implicit def rotaReads: Reads[Rota] = {
    val name = (JsPath \ "name")
      .read[String]
      .filterNot(JsonValidationError("error.empty", "name"))(_.isEmpty)
      .filter(JsonValidationError("error.minLength", "name", 3))(_.size > 3)

    val description = (JsPath \ "description")
      .readNullable[String]
      .filterNot(JsonValidationError("error.empty", "description"))(string =>
        string.isDefined && string.get.isEmpty
      )

    val assigned = (JsPath \ "assigned").readNullable[Int]

    (name and description and assigned)(Rota.apply _)
  }

  implicit val rotaWrites = new Writes[Rota] {
    def writes(rota: Rota) = Json.obj(
      "name" -> rota.name,
      "description" -> rota.description
    )
  }

}
