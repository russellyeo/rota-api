package repositories

import java.util.Date
import javax.inject.{ Inject, Singleton }

import models.RotaUser
import play.api.db.slick.{ DatabaseConfigProvider, HasDatabaseConfigProvider }
import slick.jdbc.JdbcProfile
import scala.concurrent.{ ExecutionContext, Future }

@Singleton()
class RotaUsersRepository @Inject()
    (protected val dbConfigProvider: DatabaseConfigProvider)
    (implicit executionContext: ExecutionContext) extends RotaUsersComponent with HasDatabaseConfigProvider[JdbcProfile] {

    import profile.api._

    /** Count the number of rota users */
    def count(): Future[Int] = 
        db.run(rotaUsers.map(_.rotaID).length.result)

    /** Get all rota users with rotaID */
    def getRotaUsersWithRotaID(rotaID: Int): Future[Seq[RotaUser]] =
        db.run(rotaUsers.filter(_.rotaID === rotaID).result)

    /** Get all rota users with userID */
    def getRotaUsersWithUserID(userID: Int): Future[Seq[RotaUser]] =
        db.run(rotaUsers.filter(_.userID === userID).result)

}

trait RotaUsersComponent { self: HasDatabaseConfigProvider[JdbcProfile] =>
    import profile.api._

    /** Query for the ROTA_USERS table  */
    lazy protected val rotaUsers = TableQuery[RotaUsers]

    /** Definition of the ROTA_USERS table */
    class RotaUsers(tag: Tag) extends Table[RotaUser](tag, "ROTA_USERS") {
        def rotaID = column[Int]("ROTA_ID")
        def userID = column[Int]("USER_ID")

        def * = (rotaID, userID) <> ((RotaUser.apply _).tupled, RotaUser.unapply _)
    }
}