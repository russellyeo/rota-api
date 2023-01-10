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

class UsersServiceSpec extends PlaySpec with GuiceOneAppPerTest {
  "createUserIfNeeded" should {
    "create a user if it does not exist" in new WithUsersService() {
      // GIVEN the user does not exist
      val user = User("Richard", 0)
      when(mockUsersRepository.retrieve("Richard"))
        .thenReturn(Future.successful(None))
      // AND will be successfully inserted
      when(mockUsersRepository.insert(user))
        .thenReturn(Future.successful(user))

      // WHEN we call createUserIfNeeded
      val result = rotasService.createUserIfNeeded("Richard")

      // THEN the newly created user is returned
      await(result) mustBe user
    }

    "return a user if they already exist" in new WithUsersService() {
      // GIVEN the user exists
      val user = User("Ayla", 1)
      when(mockUsersRepository.retrieve("Ayla"))
        .thenReturn(Future.successful(Some(user)))

      // WHEN we call createUserIfNeeded
      val result = rotasService.createUserIfNeeded("Ayla")

      // THEN the newly created user is returned
      await(result) mustBe user
      // AND the user is not inserted
      verify(mockUsersRepository, never()).insert(any[User])
    }
  }
}

trait WithUsersService extends WithApplication with MockitoSugar {
  val mockUsersRepository = mock[UsersRepository]
  val mockRotaUsersRepository = mock[RotaUsersRepository]

  val rotasService = new UsersService(
    mockUsersRepository,
    mockRotaUsersRepository
  )
}
