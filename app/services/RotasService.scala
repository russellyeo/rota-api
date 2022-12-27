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

  /** Retrieve all Rotas
    *
    * @return
    *   A list of Rotas
    */
  def list(): Future[Seq[Rota]] = {
    rotasRepository.list()
  }

  /** Create a new Rota
    *
    * @param rota
    *   The Rota to insert
    * @return
    *   The inserted Rota
    */
  def create(rota: Rota): Future[Rota] = {
    rotasRepository.insert(rota)
  }

  /** Retrieve a Rota with its assigned user and all unassigned users
    *
    * @param id
    *   The ID of the Rota to retrieve
    * @return
    *   RotaWithUsers
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

  /** Delete a Rota
    *
    * This will also delete all RotaUsers associated with the Rota
    *
    * @param id
    *   The ID of the Rota to get
    * @return
    *   RotaWithUsers
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
