package repositories

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.test.Helpers._
import play.api.test.{Injecting, WithApplication}


class RotaUsersRepositorySpec extends PlaySpec with GuiceOneAppPerTest {
  import models._

  "RotaUsersRepository" should {

    "have some test data" in new WithRotaUsersRepository() {
        val count = await(rotaUsersRepository.count())
        count mustBe 11
    }

    "retrive all rota users for a given rota ID" in new WithRotaUsersRepository() {
        val rotaUsers = await(rotaUsersRepository.getRotaUsersWithRotaID(1))
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

    "retrive all rota users for a given user ID" in new WithRotaUsersRepository() {
        val rotaUsers = await(rotaUsersRepository.getRotaUsersWithUserID(7))
        rotaUsers mustBe Seq(
            RotaUser(1, 7), 
            RotaUser(2, 7)
        )
    }

  }
}

trait WithRotaUsersRepository extends WithApplication with Injecting {
  val rotaUsersRepository = inject[RotaUsersRepository]
}

