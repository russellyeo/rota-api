package services

import javax.inject.{Inject}

import repositories._
import models.{Rota, RotaUser, RotaWithUsers, User}

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
    *   the rota to insert
    * @return
    *   the inserted rota
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

  /** Delete a rota
    *
    * This will also delete all RotaUsers associated with the Rota
    *
    * @param id
    *   the ID of the rota to get
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

  /** Add users to a rota
    *
    * @param rotaID
    *   the ID of the rota to add to
    * @return
    *   the rota users that were inserted
    */
  def addUsersToRota(rotaID: Int, users: Seq[User]): Future[Seq[RotaUser]] = {
    val rotaUsers = users.map(user => RotaUser(rotaID, user.id))
    rotaUsersRepository.createRotaUsers(rotaUsers)
    Future.successful(rotaUsers)
  }

  /** Rotate a rota's assigned user
    *
    * @param rotaID
    *   the ID of the rota to rotate
    * @return
    *   the updated rota
    */
  def rotate(rotaID: Int): Future[Option[Rota]] = {
    for {
      rota <- rotasRepository.retrieve(rotaID)
      rotaUsers <- rotaUsersRepository.retrieveRotaUsersWithRotaID(rotaID)
      updatedRota <- rota match {
        case Some(rota) =>
          val newAssigned = rota.assigned match {
            case Some(assigned) =>
              val assignedIndex = rotaUsers.indexWhere(_.userID == assigned)
              val nextIndex = (assignedIndex + 1) % rotaUsers.length
              Some(rotaUsers(nextIndex))
            case None =>
              Some(rotaUsers.head)
          }
          rotasRepository.update(rotaID, None, None, assigned = newAssigned.map(_.userID))
        case None =>
          Future.successful(None)
      }
    } yield {
      updatedRota
    }
  }

}
