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
    db.run(rotas.map(_.id).length.result)

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

  /** Retrieve rota by ID
    *
    * @param id
    *   the ID of the rota to retreive
    * @return
    *   the requested rota if it exists
    */
  def retrieve(id: Int): Future[Option[Rota]] =
    db.run(rotas.filter(_.id === id).result.headOption)

  /** Update rota
    *
    * @param id
    *   the ID of the rota to update
    * @param name
    *   the new name for the rota, if given
    * @param description
    *   the new description for the rota, if given
    * @param assigned
    *   the new assigned user ID for the rota, if given
    * @return
    *   the updated rota, if it was found
    */
  def update(
      id: Int,
      name: Option[String],
      description: Option[String],
      assigned: Option[Int]
  ): Future[Option[Rota]] =
    db.run(rotas.filter(_.id === id).result.headOption).flatMap {
      case Some(rota) =>
        val updatedRota = rota.copy(
          name = name match {
            case Some(value) => value
            case None        => rota.name
          },
          description = description match {
            case Some(value) => Some(value)
            case None        => rota.description
          },
          assigned = assigned match {
            case Some(value) => Some(value)
            case None        => rota.assigned
          }
        )
        db.run(rotas.filter(_.id === id).update(updatedRota)).map {
          case 1 => Some(updatedRota)
          case _ => None
        }
      case None => Future.successful(None)
    }

  /** Delete a rota
    *
    * @param id
    *   the ID of the rota to delete
    * @return
    *   the number of rows deleted
    */
  def delete(id: Int): Future[Int] =
    db.run(rotas.filter(_.id === id).delete)

}

trait RotasComponent { self: HasDatabaseConfigProvider[JdbcProfile] =>
  import profile.api._

  /** Query for the ROTAS table */
  lazy protected val rotas = TableQuery[Rotas]

  /** Query for the ROTAS table, returning a copy of the row after a database
    * operation
    */
  lazy protected val rotasReturningRow = rotas returning rotas.map(_.id) into {
    (rota, id) => rota.copy(id = Some(id))
  }

  /** Definition of the ROTAS table */
  class Rotas(tag: Tag) extends Table[Rota](tag, "ROTAS") {
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    def name = column[String]("NAME")
    def description = column[Option[String]]("DESCRIPTION")
    def assigned = column[Option[Int]]("ASSIGNED_USER_ID")
    def * = (
      name,
      description,
      assigned,
      id.?
    ) <> ((Rota.apply _).tupled, Rota.unapply _)
  }
}
