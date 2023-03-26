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
              Rota("retrospective", Some("Reflection"), Some(4)),
              Rota("standup", Some("Daily Standup"), Some(2))
            )
          )
        )

      // WHEN we retrieve all rotas
      val result = rotasService.list()

      // THEN the list of rotas is returned
      await(result) mustBe Seq(
        Rota("retrospective", Some("Reflection"), Some(4)),
        Rota("standup", Some("Daily Standup"), Some(2))
      )
    }
  }

  "create" should {
    "create a new rota" in new WithRotasService() {
      // GIVEN the RotasRepository returns a successfully inserted Rota
      val rota = Rota("retrospective", None, Some(4))
      when(mockRotasRepository.insert(rota))
        .thenReturn(Future.successful(rota))

      // WHEN we create a new Rota
      val result = rotasService.create(rota)

      // THEN the newly created Rota is returned
      await(result) mustBe rota
    }
  }

  "retrieve" should {
    "retrieve a rota with an assigned user and some unassigned users" in new WithRotasService() {
      // GIVEN the RotasRepository returns a Rota
      when(mockRotasRepository.retrieve("retrospective"))
        .thenReturn(
          Future.successful(
            Some(Rota("retrospective", None, Some(1)))
          )
        )
      // AND the RotaUsersRepository returns a list of RotaUsers
      when(mockRotaUsersRepository.retrieveRotaUsersInRota("retrospective"))
        .thenReturn(
          Future.successful(
            Seq(
              RotaUser("retrospective", 1),
              RotaUser("retrospective", 2),
              RotaUser("retrospective", 3)
            )
          )
        )
      // AND the UsersRepository returns a list of Users attached to the rota
      when(mockUsersRepository.retrieveList(Seq(1, 2, 3)))
        .thenReturn(
          Future.successful(
            Seq(User("@Sofia", 1), User("@Emma", 2), User("@Bob", 3))
          )
        )
      // AND the UsersRepository returns a single User assigned as the active user
      when(mockUsersRepository.retrieve(1))
        .thenReturn(Future.successful(Some(User("@Sofia", 1))))

      // WHEN we retrieve the Rota
      val result = rotasService.retrieve("retrospective")

      // THEN a RotaWithUsers will be returned
      await(result) mustBe Some(
        RotaWithUsers(
          rota = Rota("retrospective", None, Some(1)),
          assigned = Some(User("@Sofia", 1)),
          users = Seq(User("@Sofia", 1), User("@Emma", 2), User("@Bob", 3))
        )
      )
    }
  }

  "update" should {
    "update a rota's details" in new WithRotasService() {
      // GIVEN the repository will return an updated rota
      val original = Rota(
        name = "standup",
        description = Some("Daily check-in"),
        assigned = Some(4)
      )
      val updated = original.copy(assigned = Some(5))

      when(mockRotasRepository.retrieve("standup"))
        .thenReturn(Future.successful(Some(original)))

      when(mockRotasRepository.update("standup", None, Some(5)))
        .thenReturn(Future.successful(Some(updated)))

      // WHEN we update the rota
      val result = rotasService.update(
        name = "standup",
        description = None,
        assigned = Some(5)
      )

      // THEN the updated rota is returned
      await(result) mustBe Some(updated)
    }
  }

  "delete" should {
    "delete RotaUsers and then Rota" in new WithRotasService() {
      // GIVEN the RotaUsersRepository will delete 8 RotaUsers
      when(mockRotaUsersRepository.deleteRotaUsersInRota("standup"))
        .thenReturn(Future.successful(8))
      // AND the RotasRepository will delete a Rota
      when(mockRotasRepository.delete("standup"))
        .thenReturn(Future.successful(1))

      // WHEN we call deleteRota
      val result = rotasService.delete("standup")

      // THEN the RotaUsers and Rota will be deleted
      await(result) mustBe 1
      verify(mockRotaUsersRepository).deleteRotaUsersInRota("standup")
      verify(mockRotasRepository).delete("standup")
    }
  }

  "rotate" should {
    "rotate a rota" in new WithRotasService() {
      // GIVEN a rota wih some users and Emma is the assigned user
      val user1 = User(name = "@Sofia", id = 1)
      val user2 = User(name = "@Emma", id = 2)
      val user3 = User(name = "@Bob", id = 3)
      val rota = Rota(name = "retrospective", assigned = Some(2))
      val rotaUser1 = RotaUser(rotaName = "retrospective", userID = 1)
      val rotaUser2 = RotaUser(rotaName = "retrospective", userID = 2)
      val rotaUser3 = RotaUser(rotaName = "retrospective", userID = 3)

      // AND the rota will be successfully retrieved
      when(mockRotasRepository.retrieve("retrospective"))
        .thenReturn(Future.successful(Some(rota)))

      // AND the rota users will be successfully retrieved
      when(mockRotaUsersRepository.retrieveRotaUsersInRota("retrospective"))
        .thenReturn(Future.successful(Seq(rotaUser1, rotaUser2, rotaUser3)))

      // AND the rota will be successfully updated to assign the next user
      when(
        mockRotasRepository.update(name = "retrospective", description = None, assigned = Some(3))
      ).thenReturn(
        Future.successful(Some(rota.copy(assigned = Some(3))))
      )

      // WHEN we rotate the rota
      val result = rotasService.rotate("retrospective")

      // THEN Bob is the new assigned user
      await(result) mustBe Some(Rota(name = "retrospective", assigned = Some(3)))
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
