import cats.effect._
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.freespec.AsyncFreeSpec

class MainTest extends AsyncFreeSpec with AsyncIOSpec with Matchers {

  "My Code " - {
    "works" in {
      IO(1).asserting(_ shouldBe 1)
    }
  }

}