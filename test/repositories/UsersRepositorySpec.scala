package repositories

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.test.Helpers._
import play.api.test.{Injecting, WithApplication}

import models.User

class UsersRepositorySpec extends PlaySpec with GuiceOneAppPerTest {

  "count" should {
    "return the number of rotas in the database" in new WithUsersRepository() {
      await(usersRepository.count()) mustBe 8
    }
  }

  "retrieve" should {
    "retrieve a user" in new WithUsersRepository() {
      val user = await(usersRepository.retrieve(1)).get
      user.id mustBe 1
      user.name mustBe "Maria"
    }
  }

  "insert" should {
    "insert a new user" in new WithUsersRepository() {
      val user = User("Bob")
      val inserted = await(usersRepository.insert(user))
      inserted.id mustBe 9
      inserted.name mustBe "Bob"
    }
  }
}

trait WithUsersRepository extends WithApplication with Injecting {
  val usersRepository = inject[UsersRepository]
}
