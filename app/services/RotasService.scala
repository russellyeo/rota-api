package services

import javax.inject.{Inject}

import repositories._
import models.{Rota, RotaWithUsers}

import scala.concurrent.{ExecutionContext, Future}

class RotasService @Inject() (
    rotasRepository: RotasRepository,
    usersRepository: UsersRepository,
    rotaUsersRepository: RotaUsersRepository
)(implicit executionContext: ExecutionContext) {

  /** Retrieve all rotas
    *
    * @return
    *   a list of rotas
    */
  def list(): Future[Seq[Rota]] = {
    rotasRepository.list()
  }

  /** Create a new rota
    *
    * @param rota
    *   the Rota to insert
    * @return
    *   the inserted Rota
    */
  def create(rota: Rota): Future[Rota] = {
    rotasRepository.insert(rota)
  }

  /** Retrieve a rota
    *
    * @param id
    *   the ID of the rota to retrieve
    * @return
    *   the requested rota with its assigned user and all unassigned users
    */
  def retrieve(id: Int): Future[Option[RotaWithUsers]] = {
    rotasRepository.retrieve(id).flatMap { rota =>
      rota match {
        case Some(rota) =>
          for {
            rotaUsers <- rotaUsersRepository.retrieveRotaUsersWithRotaID(id)
            users <- usersRepository.retrieveList(rotaUsers.map(_.userID))
            assigned <- rota.assigned
              .map(usersRepository.retrieve)
              .getOrElse(Future.successful(None))
          } yield {
            Some(RotaWithUsers(rota, assigned, users))
          }
        case None =>
          Future.successful(None)
      }
    }
  }

  /** Update a rota's details
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
      name: Option[String] = None,
      description: Option[String] = None,
      assigned: Option[Int] = None
  ): Future[Option[Rota]] = {
    rotasRepository.update(id, name, description, assigned)
  }

  /** Delete a Rota
    *
    * This will also delete all RotaUsers associated with the Rota
    *
    * @param id
    *   the ID of the Rota to get
    * @return
    *   the number of rotas deleted
    */
  def delete(id: Int): Future[Int] = {
    for {
      _ <- rotaUsersRepository.deleteRotaUsersWithRotaID(id)
      deletedRotas <- rotasRepository.delete(id)
    } yield {
      deletedRotas
    }
  }

}
