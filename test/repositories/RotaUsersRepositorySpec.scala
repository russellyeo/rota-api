package repositories

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.test.Helpers._
import play.api.test.{Injecting, WithApplication}

import models.RotaUser

class RotaUsersRepositorySpec extends PlaySpec with GuiceOneAppPerTest {

  "count" should {
    "return the number of RotaUsers in the database" in new WithRotaUsersRepository() {
      await(rotaUsersRepository.count()) mustBe 11
    }
  }

  "retrieveRotaUsersWithRotaID" should {
    "retrive all rota users for a given rota ID" in new WithRotaUsersRepository() {
      val rotaUsers = await(rotaUsersRepository.retrieveRotaUsersWithRotaID(1))
      rotaUsers mustBe Seq(
        RotaUser(1, 1),
        RotaUser(1, 2),
        RotaUser(1, 3),
        RotaUser(1, 4),
        RotaUser(1, 5),
        RotaUser(1, 6),
        RotaUser(1, 7),
        RotaUser(1, 8)
      )
    }

    "not retrieve rota users for a rota ID that does not exist" in new WithRotaUsersRepository() {
      await(rotaUsersRepository.retrieveRotaUsersWithRotaID(4)) mustBe Seq.empty
    }
  }

  "retrieveRotaUsersWithUserID" should {
    "retrieve all rota users for a given user ID" in new WithRotaUsersRepository() {
      val rotaUsers = await(rotaUsersRepository.retrieveRotaUsersWithUserID(7))
      rotaUsers mustBe Seq(
        RotaUser(1, 7),
        RotaUser(2, 7)
      )
    }

    "not retrieve rota users with user ID that does not exist" in new WithRotaUsersRepository() {
      await(
        rotaUsersRepository.retrieveRotaUsersWithUserID(10)
      ) mustBe Seq.empty
    }
  }

  "deleteRotaUsersWithRotaID" should {
    "delete all rota users for a given rota ID" in new WithRotaUsersRepository() {
      await(rotaUsersRepository.deleteRotaUsersWithRotaID(1)) mustBe 8
    }

    "not delete any rota users for a rota ID if the rota does not exist" in new WithRotaUsersRepository() {
      await(rotaUsersRepository.deleteRotaUsersWithRotaID(4)) mustBe 0
    }
  }

}

trait WithRotaUsersRepository extends WithApplication with Injecting {
  val rotaUsersRepository = inject[RotaUsersRepository]
}
