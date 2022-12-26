package repositories

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.test.Helpers._
import play.api.test.{Injecting, WithApplication}

class RotasRepositorySpec extends PlaySpec with GuiceOneAppPerTest {
  import models._

  "RotasRepository" should {

    "have some test data" in new WithRotasRepository() {
      val count = await(rotasRepository.count())
      count mustBe 2
    }

    "retrieve a rota" in new WithRotasRepository() {
      val rota = await(rotasRepository.get(1)).get
      rota.id mustBe Some(1)
      rota.name mustBe "Daily Standup"
      rota.description mustBe Some("Share updates and kick off the day")
      rota.assigned mustBe Some(8)
    }

    "insert a new rota" in new WithRotasRepository() {
      val rota = Rota("Coffee", Some("Whose turn it is to make coffee"), None)
      val inserted = await(rotasRepository.insert(rota))
      inserted.id mustBe Some(3)
      inserted.name mustBe "Coffee"
      inserted.description mustBe Some("Whose turn it is to make coffee")
      inserted.assigned mustBe None
    }

  }
}

trait WithRotasRepository extends WithApplication with Injecting {
  val rotasRepository = inject[RotasRepository]
}
