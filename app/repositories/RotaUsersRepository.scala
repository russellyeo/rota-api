package repositories

import javax.inject.{Inject, Singleton}

import models.RotaUser
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class RotaUsersRepository @Inject() (
    protected val dbConfigProvider: DatabaseConfigProvider
)(implicit executionContext: ExecutionContext)
    extends RotaUsersComponent
    with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  /** Count the number of rota users
    *
    * @return
    *   the number of rota users in the database
    */
  def count(): Future[Int] =
    db.run(rotaUsers.map(_.rotaID).length.result)

  /** Retrieve all rota users with a given rotaID
    *
    * @param rotaID
    *   the ID of the rota to retreive
    * @return
    *   the requested rota users
    */
  def retrieveRotaUsersWithRotaID(rotaID: Int): Future[Seq[RotaUser]] =
    db.run(rotaUsers.filter(_.rotaID === rotaID).result)

  /** Retrieve all rota users with a given userID
    *
    * @param userID
    *   the ID of the user to retreive
    * @return
    *   the requested rota users
    */
  def retrieveRotaUsersWithUserID(userID: Int): Future[Seq[RotaUser]] =
    db.run(rotaUsers.filter(_.userID === userID).result)

  /** Delete all rota users with a given rotaID
    *
    * @param rotaID
    *   the ID of the user to retreive
    * @return
    *   the requested rota users
    */
  def deleteRotaUsersWithRotaID(rotaID: Int): Future[Int] =
    db.run(rotaUsers.filter(_.rotaID === rotaID).delete)

}

trait RotaUsersComponent { self: HasDatabaseConfigProvider[JdbcProfile] =>
  import profile.api._

  /** Query for the ROTA_USERS table */
  lazy protected val rotaUsers = TableQuery[RotaUsers]

  /** Definition of the ROTA_USERS table */
  class RotaUsers(tag: Tag) extends Table[RotaUser](tag, "ROTA_USERS") {
    def rotaID = column[Int]("ROTA_ID")
    def userID = column[Int]("USER_ID")

    def * = (rotaID, userID) <> ((RotaUser.apply _).tupled, RotaUser.unapply _)
  }
}
