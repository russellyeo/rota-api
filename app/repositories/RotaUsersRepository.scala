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
    db.run(rotaUsers.map(_.rotaName).length.result)

  /** Create rota users
    *
    * @param rotaUsers
    *   the rota users to be inserted
    */
  def createRotaUsers(rotaUsers: Seq[RotaUser]): Unit =
    db.run(this.rotaUsers ++= rotaUsers)

  /** Retrieve all users in a given rota
    *
    * @param rotaName
    *   the name of the rota to retreive
    * @return
    *   the users in the rota
    */
  def retrieveRotaUsersInRota(rotaName: String): Future[Seq[RotaUser]] =
    db.run(rotaUsers.filter(_.rotaName === rotaName).result)

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
    * @param rotaName
    *   the name of the rota to retreive
    * @return
    *   the number of rota users deleted
    */
  def deleteAllRotaUsersFromRota(rotaName: String): Future[Int] =
    db.run(rotaUsers.filter(_.rotaName === rotaName).delete)

  /** Delete a rota user from a rota
    *
    * @param rotaName
    *   the name of the rota
    * @param userID
    *   the userID to delete
    * @return
    *   the number of rota users deleted
    */
  def deleteRotaUser(rotaName: String, userID: Int): Future[Int] = ???

}

trait RotaUsersComponent { self: HasDatabaseConfigProvider[JdbcProfile] =>
  import profile.api._

  /** Query for the ROTA_USERS table */
  lazy protected val rotaUsers = TableQuery[RotaUsers]

  /** Definition of the ROTA_USERS table */
  class RotaUsers(tag: Tag) extends Table[RotaUser](tag, "ROTA_USERS") {
    def rotaName = column[String]("ROTA_NAME")
    def userID = column[Int]("USER_ID")

    def * = (rotaName, userID) <> ((RotaUser.apply _).tupled, RotaUser.unapply _)
  }
}
