
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import spray.json._

import java.time.LocalDate
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContextExecutor}
import scala.io.Source

//Models
case class Experiment(name: String, startDate: String, endDate: String, offers: List[PossibleLoanOffer])
case class Experiments(experiments: List[Experiment])
case class Customer(phoneNumber: String, creditScore: Float) {
  require(phoneNumber.length == 13 && phoneNumber.forall(c => Character.isDigit(c)))
  require(0.0 <= creditScore && creditScore <= 1.0)
}
//TODO ran out of time here but was thinking that I can also add require restrictions here as well.
case class PossibleLoanOffer(minScore: Float, amount: Float, fee: Int, term: Int)
case class LoanOffer(amount: Int, fee: Int, term: Int) {
  require(amount > 0)
  require(0 <= fee && fee <= 100)
  require(term > 0)
}

//JSON formatters
trait LoanJsonProtocol extends DefaultJsonProtocol {
  implicit val customerFormat: RootJsonFormat[Customer] = jsonFormat2(Customer)
  implicit val possibleLoanOfferFormat: RootJsonFormat[PossibleLoanOffer] = jsonFormat4(PossibleLoanOffer)
  implicit val experimentFormat: RootJsonFormat[Experiment] = jsonFormat4(Experiment)
  implicit val experimentsFormat: RootJsonFormat[Experiments] = jsonFormat1(Experiments)
  implicit val loanOfferFormat: RootJsonFormat[LoanOffer] = jsonFormat3(LoanOffer)
}

object LoanFinder {

  def findEligibleOffers(creditScore: Float, validOffers: List[Experiment]): List[LoanOffer] = {
    val eligibleOffers = validOffers.flatMap(elem => elem.offers.filter(_.minScore <= creditScore))
    for (f <- eligibleOffers) yield {
      val amount = f.amount.toInt
      val fee = f.fee
      val term = f.term
      LoanOffer(amount, fee, term)
    }
  }

  def validOffers(experiments: Experiments): List[Experiment] = {
    val todayDate = LocalDate.now
    val validOffers = experiments.experiments.filter {
      experiment =>
        val isWithinDateRange = LocalDate.parse(experiment.startDate).isBefore(todayDate) &&
          LocalDate.parse(experiment.endDate).isAfter(todayDate)
        //This is one criteria, but there are at least couple more that need validation.
        isWithinDateRange
    }
    validOffers
  }
}

object ExampleApp extends App with LoanJsonProtocol with SprayJsonSupport {

  import LoanFinder._
  implicit val system: ActorSystem = ActorSystem("loan-service")
  /*
    Having a default materializer available means that most, if not all, usages of Java ActorMaterializer.create()
    and Scala implicit val materializer = ActorMaterializer() should be removed.
   */
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  val experiments = validOffers(Source.fromResource("experiments.json").mkString.parseJson.convertTo[Experiments])
  //TODO need to handle the case of invalid parameters. Also, would be better to have validOffers return an Option type.
  if(experiments.size != 1) system.terminate()
  println(experiments)
  val routes =
    (path("api" / "offers") & post) {
      entity(as[Customer]) { customer =>
        //pass the customer to a function that will convert the data to a list of loanOffers.
        complete(findEligibleOffers(customer.creditScore, experiments))
      }
    }
  val binding = Http().newServerAt("localhost", 9000).bind(routes)
  Await.result(system.whenTerminated, Duration.Inf)
}
