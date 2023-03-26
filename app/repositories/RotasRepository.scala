package repositories

import javax.inject.{Inject, Singleton}

import models.{Rota, RotaUser}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class RotasRepository @Inject() (
    protected val dbConfigProvider: DatabaseConfigProvider
)(implicit executionContext: ExecutionContext)
    extends RotasComponent
    with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  /** Count the number of rotas
    *
    * @return
    *   the number of rotas in the database
    */
  def count(): Future[Int] =
    db.run(rotas.map(_.name).length.result)

  /** List all rotas
    *
    * @return
    *   a list of all rotas in the database
    */
  def list(): Future[Seq[Rota]] =
    db.run(rotas.result)

  /** Insert a new rota
    *
    * @param rota
    *   the rota to be inserted
    */
  def insert(rota: Rota): Future[Rota] =
    db.run(rotasReturningRow += rota)

  /** Retrieve rota
    *
    * @param name
    *   the name of the rota to retreive
    * @return
    *   the requested rota if it exists
    */
  def retrieve(name: String): Future[Option[Rota]] =
    db.run(rotas.filter(_.name === name).result.headOption)

  /** Update a rota's details
    *
    * @param name
    *   the name of the rota to update
    * @param description
    *   the new description for the rota, if given
    * @param assigned
    *   the new assigned user ID for the rota, if given
    * @return
    *   the updated rota, if it was found
    */
  def update(
      name: String,
      description: Option[String] = None,
      assigned: Option[Int] = None
  ): Future[Option[Rota]] =
    db.run(rotas.filter(_.name === name).result.headOption).flatMap {
      case Some(rota) =>
        val updatedRota = rota.copy(
          description = description match {
            case Some(value) => Some(value)
            case None        => rota.description
          },
          assigned = assigned match {
            case Some(value) => Some(value)
            case None        => rota.assigned
          }
        )
        db.run(rotas.filter(_.name === name).update(updatedRota)).map {
          case 1 => Some(updatedRota)
          case _ => None
        }
      case None => Future.successful(None)
    }

  /** Delete a rota
    *
    * @param name
    *   the name of the rota to delete
    * @return
    *   the number of rows deleted
    */
  def delete(name: String): Future[Int] =
    db.run(rotas.filter(_.name === name).delete)

}

trait RotasComponent { self: HasDatabaseConfigProvider[JdbcProfile] =>
  import profile.api._

  /** Query for the ROTAS table */
  lazy protected val rotas = TableQuery[Rotas]

  /** Query for the ROTAS table, returning a copy of the row after a database operation
    */
  lazy protected val rotasReturningRow = rotas returning rotas.map(_.name) into { (rota, name) =>
    rota.copy(name = name)
  }

  /** Definition of the ROTAS table */
  class Rotas(tag: Tag) extends Table[Rota](tag, "ROTAS") {
    def name = column[String]("NAME", O.PrimaryKey)
    def description = column[Option[String]]("DESCRIPTION")
    def assigned = column[Option[Int]]("ASSIGNED_USER_ID")
    def * = (name, description, assigned) <> ((Rota.apply _).tupled, Rota.unapply _)
  }
}
