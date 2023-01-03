package models.dto

case class UpdateRotaDTO(
    name: Option[String] = None,
    description: Option[String] = None,
    assigned: Option[Int] = None
)

object UpdateRotaDTO {
  import play.api.libs.json._
  import play.api.libs.functional.syntax._
  import play.api.data.validation._

  implicit def reads: Reads[UpdateRotaDTO] = {

    // `name` is optional and should have at least 3 characters if it is given
    val name = (JsPath \ "name")
      .readNullable[String]
      .filter(JsonValidationError("error.minLength", "name", 3))(string =>
        string.forall(_.size >= 3)
      )

    // `description` is optional and should have at least 3 characters if it is given
    val description = (JsPath \ "description")
      .readNullable[String]
      .filter(JsonValidationError("error.minLength", "description", 3))(string =>
        string.forall(_.size >= 3)
      )

    // `assigned` is optional
    val assigned = (JsPath \ "assigned").readNullable[Int]

    (name and description and assigned)(UpdateRotaDTO.apply _)
  }

  implicit def writes: Writes[UpdateRotaDTO] = new Writes[UpdateRotaDTO] {
    def writes(updateRotaDTO: UpdateRotaDTO) = Json.obj(
      "name" -> updateRotaDTO.name,
      "description" -> updateRotaDTO.description,
      "assigned" -> updateRotaDTO.assigned
    )
  }

}
