package repositories

import java.util.Date
import javax.inject.{ Inject, Singleton }

import models.Rota
import play.api.db.slick.{ DatabaseConfigProvider, HasDatabaseConfigProvider }
import slick.jdbc.JdbcProfile
import scala.concurrent.{ ExecutionContext, Future }

@Singleton()
class RotasRepository @Inject()
    (protected val dbConfigProvider: DatabaseConfigProvider)
    (implicit executionContext: ExecutionContext) extends RotasComponent with HasDatabaseConfigProvider[JdbcProfile] {
  
    import profile.api._

    /** Count the number of rotas */
    def count(): Future[Int] = 
        db.run(rotas.map(_.id).length.result)

    /** List all rotas */
    def list(): Future[Seq[Rota]] =
        db.run(rotas.result)

    /** Get rota by ID */
    def get(id: Int): Future[Option[Rota]] =
        db.run(rotas.filter(_.id === id).result.headOption)
    
    /** Insert a new rota */
    def insert(rota: Rota): Future[Rota] =
        db.run(rotasReturningRow += rota)

}

trait RotasComponent { self: HasDatabaseConfigProvider[JdbcProfile] =>
    import profile.api._

    /** Query for the ROTAS table  */
    lazy protected val rotas = TableQuery[Rotas]
    
    /** Query for the ROTAS table, returning a copy of the row after a database operation */
    lazy protected val rotasReturningRow = rotas returning rotas.map(_.id) into { (rota, id) =>
        rota.copy(id = id)
    }

    /** Definition of the ROTAS table */
    class Rotas(tag: Tag) extends Table[Rota](tag, "ROTAS") { 
        def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
        def name = column[String]("NAME")
        def description = column[Option[String]]("DESCRIPTION")
        def assigned = column[Option[Int]]("ASSIGNED_USER_ID")
        def * = (name, description, assigned, id) <> ((Rota.apply _).tupled, Rota.unapply _)
    }
}