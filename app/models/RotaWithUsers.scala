package models

case class RotaWithUsers(rota: Rota, assigned: Option[User], users: Seq[User])

object RotaWithUsers {
    import play.api.libs.json.{Json, Writes}

    implicit val writes = new Writes[RotaWithUsers] {
        def writes(rotaWithUsers: RotaWithUsers) = Json.obj(
            "rota" -> rotaWithUsers.rota,
            "assigned" -> rotaWithUsers.assigned.map(_.name),
            "users" -> rotaWithUsers.users.map(_.name)
        )
    }
}