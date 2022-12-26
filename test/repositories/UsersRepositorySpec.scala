package repositories

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.test.Helpers._
import play.api.test.{Injecting, WithApplication}

class UsersRepositorySpec extends PlaySpec with GuiceOneAppPerTest {
  import models._

  "UsersRepository" should {

    "have some test data" in new WithUsersRepository() {
      val count = await(usersRepository.count())
      count mustBe 8
    }

    "retrieve a user" in new WithUsersRepository() {
      val user = await(usersRepository.get(1)).get
      user.id mustBe 1
      user.name mustBe "Maria"
    }

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
