package services

import org.scalatest._
import org.scalatestplus.mockito._
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers._

import play.api.test.Helpers._
import play.api.test.WithApplication

import repositories._
import models._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RotasServiceSpec extends PlaySpec with GuiceOneAppPerTest {

  "list" should {
    "retrive a list of all rotas" in new WithRotasService() {
      // GIVEN the RotasRepository returns a list of Rotas
      when(mockRotasRepository.list())
        .thenReturn(
          Future.successful(
            Seq(
              Rota("Retrospective", None, Some(4), Some(1)),
              Rota("Standup", Some("Daily Standup"), Some(2), Some(2))
            )
          )
        )

      // WHEN we retrieve all rotas
      val result = rotasService.list()

      // THEN the list of rotas is returned
      await(result) mustBe Seq(
        Rota("Retrospective", None, Some(4), Some(1)),
        Rota("Standup", Some("Daily Standup"), Some(2), Some(2))
      )
    }
  }

  "create" should {
    "create a new Rota" in new WithRotasService() {
      // GIVEN the RotasRepository returns an inserted Rota with an ID
      val rota = Rota("Retrospective", None, Some(4), None)
      val inserted = rota.copy(id = Some(1))
      when(mockRotasRepository.insert(rota))
        .thenReturn(Future.successful(inserted))

      // WHEN we create a new Rota
      val result = rotasService.create(rota)

      // THEN the newly created Rota is returned
      await(result) mustBe inserted
    }
  }

  "retrieve" should {
    "retrieve a rota with an assigned user and some unassigned users" in new WithRotasService() {
      // GIVEN the RotasRepository returns a Rota
      when(mockRotasRepository.retrieve(1))
        .thenReturn(
          Future.successful(
            Some(Rota("Retrospective", None, Some(1), Some(1)))
          )
        )
      // AND the RotaUsersRepository returns a list of RotaUsers
      when(mockRotaUsersRepository.retrieveRotaUsersWithRotaID(1))
        .thenReturn(
          Future.successful(
            Seq(RotaUser(1, 1), RotaUser(1, 2), RotaUser(1, 3))
          )
        )
      // AND the UsersRepository returns a list of Users
      when(mockUsersRepository.retrieveList(Seq(1, 2, 3)))
        .thenReturn(
          Future.successful(
            Seq(User("Sofia", 1), User("Emma", 2), User("Bob", 3))
          )
        )
      // AND the UsersRepository returns a single User
      when(mockUsersRepository.retrieve(1))
        .thenReturn(Future.successful(Some(User("Sofia", 1))))

      // WHEN we retrieve the Rota
      val result = rotasService.retrieve(1)

      // THEN a RotaWithUsers will be returned
      await(result) mustBe Some(
        RotaWithUsers(
          rota = Rota("Retrospective", None, Some(1), Some(1)),
          assigned = Some(User("Sofia", 1)),
          users = Seq(User("Sofia", 1), User("Emma", 2), User("Bob", 3))
        )
      )
    }
  }

  "update" should {
    "update a rota's details" in new WithRotasService() {
      // GIVEN the repository will return an updated rota
      val original = Rota("Retrospective", Some("Reflect on the past sprint"), Some(4), Some(1))
      val updated = original.copy(assigned = Some(5))

      when(mockRotasRepository.retrieve(1))
        .thenReturn(Future.successful(Some(original)))

      when(mockRotasRepository.update(1, None, None, Some(5)))
        .thenReturn(Future.successful(Some(updated)))

      // WHEN we update the rota
      val result = rotasService.update(1, None, None, assigned = Some(5))

      // THEN the updated rota is returned
      await(result) mustBe Some(updated)
    }
  }

  "delete" should {
    "delete RotaUsers and then Rota" in new WithRotasService() {
      // GIVEN the RotaUsersRepository will delete 8 RotaUsers
      when(mockRotaUsersRepository.deleteRotaUsersWithRotaID(1))
        .thenReturn(Future.successful(8))
      // AND the RotasRepository will delete a Rota
      when(mockRotasRepository.delete(1))
        .thenReturn(Future.successful(1))

      // WHEN we call deleteRota
      val result = rotasService.delete(1)

      // THEN the RotaUsers and Rota will be deleted
      await(result) mustBe 1
      verify(mockRotaUsersRepository).deleteRotaUsersWithRotaID(1)
      verify(mockRotasRepository).delete(1)
    }
  }

  "rotate" should {
    "rotate a rota" in new WithRotasService() {
      // GIVEN a rota wih some users and Emma is the assigned user
      val user1 = User(name = "Sofia", id = 1)
      val user2 = User(name = "Emma", id = 2)
      val user3 = User(name = "Bob", id = 3)
      val rota = Rota(name = "Retrospective", assigned = Some(2), id = Some(1))
      val rotaUser1 = RotaUser(rotaID = 1, userID = 1)
      val rotaUser2 = RotaUser(rotaID = 1, userID = 2)
      val rotaUser3 = RotaUser(rotaID = 1, userID = 3)

      // AND the rota will be successfully retrieved
      when(mockRotasRepository.retrieve(1))
        .thenReturn(Future.successful(Some(rota)))

      // AND the rota users will be successfully retrieved
      when(mockRotaUsersRepository.retrieveRotaUsersWithRotaID(1))
        .thenReturn(Future.successful(Seq(rotaUser1, rotaUser2, rotaUser3)))

      // AND the rota will be successfully updated to assign the next user
      when(mockRotasRepository.update(id = 1, name = None, description = None, assigned = Some(3)))
        .thenReturn(Future.successful(Some(rota.copy(assigned = Some(3)))))

      // WHEN we rotate the rota
      val result = rotasService.rotate(1)

      // THEN Bob is the new assigned user
      await(result) mustBe Some(Rota(name = "Retrospective", assigned = Some(3), id = Some(1)))
    }
  }

}

trait WithRotasService extends WithApplication with MockitoSugar {
  val mockRotasRepository = mock[RotasRepository]
  val mockUsersRepository = mock[UsersRepository]
  val mockRotaUsersRepository = mock[RotaUsersRepository]

  val rotasService = new RotasService(
    mockRotasRepository,
    mockUsersRepository,
    mockRotaUsersRepository
  )
}
