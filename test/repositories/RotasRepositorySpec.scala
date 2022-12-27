package repositories

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.test.Helpers._
import play.api.test.{Injecting, WithApplication}

import models.Rota

class RotasRepositorySpec extends PlaySpec with GuiceOneAppPerTest {

  "count" should {
    "return the number of Rotas in the database" in new WithRotasRepository() {
      await(rotasRepository.count()) mustBe 3
    }
  }

  "list" should {
    "return the number of Rotas in the database" in new WithRotasRepository() {
      await(rotasRepository.list()) mustBe Seq(
        Rota(
          name = "Daily Standup",
          description = Some("Share updates and kick off the day"),
          assigned = Some(8),
          id = Some(1)
        ),
        Rota(
          name = "Three Amigos",
          description = None,
          assigned = Some(7),
          id = Some(2)
        ),
        Rota(
          name = "Placeholder",
          description = None,
          assigned = None,
          id = Some(3)
        )
      )
    }
  }

  "insert" should {
    "insert a new rota" in new WithRotasRepository() {
      val rota = Rota("Coffee", Some("Whose turn it is to make coffee"), None)
      val inserted = await(rotasRepository.insert(rota))
      inserted.id mustBe Some(4)
      inserted.name mustBe "Coffee"
      inserted.description mustBe Some("Whose turn it is to make coffee")
      inserted.assigned mustBe None
    }
  }

  "retrieve" should {
    "retrieve a rota" in new WithRotasRepository() {
      val rota = await(rotasRepository.retrieve(1)).get
      rota.id mustBe Some(1)
      rota.name mustBe "Daily Standup"
      rota.description mustBe Some("Share updates and kick off the day")
      rota.assigned mustBe Some(8)
    }

    "not retrieve a non-existing rota" in new WithRotasRepository() {
      await(rotasRepository.retrieve(4)) mustBe None
    }
  }

  "delete" should {
    "delete an existing rota" in new WithRotasRepository() {
      await(rotasRepository.delete(3)) mustBe 1
    }

    "not delete an non-existing rota" in new WithRotasRepository() {
      await(rotasRepository.delete(4)) mustBe 0
    }
  }

}

trait WithRotasRepository extends WithApplication with Injecting {
  val rotasRepository = inject[RotasRepository]
}
