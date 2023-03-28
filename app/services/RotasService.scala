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
    * @param name
    *   the name of the rota to retrieve
    * @return
    *   the requested rota with its assigned user and all unassigned users
    */
  def retrieve(name: String): Future[Option[RotaWithUsers]] = {
    rotasRepository.retrieve(name).flatMap { rota =>
      rota match {
        case Some(rota) =>
          for {
            rotaUsers <- rotaUsersRepository.retrieveRotaUsersInRota(rota.name)
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
  ): Future[Option[Rota]] = {
    rotasRepository.update(name, description, assigned)
  }

  /** Delete a rota
    *
    * This will also delete all users associated with the rota
    *
    * @param name
    *   the name of the rota to delete
    * @return
    *   the number of rotas deleted
    */
  def delete(name: String): Future[Int] = {
    for {
      _ <- rotaUsersRepository.deleteRotaUsersInRota(name)
      deletedRotas <- rotasRepository.delete(name)
    } yield {
      deletedRotas
    }
  }

  /** Add users to a rota
    *
    * This will not add users that are already in the rota
    *
    * @param rotaName
    *   the name of the rota to add to
    * @return
    *   the rota users that were inserted
    */
  def addUsersToRota(rotaName: String, users: Seq[User]): Future[Seq[RotaUser]] = {
    val rotaUsers = users.map(user => RotaUser(rotaName, user.id))
    val rotaUsersToInsert = for {
      existingRotaUsers <- rotaUsersRepository.retrieveRotaUsersInRota(rotaName)
      rotaUsersToInsert = rotaUsers.filterNot(rotaUser =>
        existingRotaUsers.exists(existingRotaUser => existingRotaUser.userID == rotaUser.userID)
      )
    } yield {
      rotaUsersToInsert
    }
    rotaUsersToInsert.flatMap {
      case rotaUsersToInsert if rotaUsersToInsert.nonEmpty =>
        rotaUsersRepository.createRotaUsers(rotaUsersToInsert)
        Future.successful(rotaUsersToInsert)
      case _ =>
        Future.successful(Seq.empty)
    }
  }

  /** Rotate a rota's assigned user
    *
    * @param rotaName
    *   the name of the rota to rotate
    * @return
    *   the updated rota
    */
  def rotate(rotaName: String): Future[Option[Rota]] = {
    for {
      rota <- rotasRepository.retrieve(rotaName)
      rotaUsers <- rotaUsersRepository.retrieveRotaUsersInRota(rotaName)
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
          rotasRepository.update(rotaName, None, assigned = newAssigned.map(_.userID))
        case None =>
          Future.successful(None)
      }
    } yield {
      updatedRota
    }
  }

}
