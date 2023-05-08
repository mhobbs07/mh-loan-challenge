package utility

import models.{Customer, Experiment, Experiments, LoanOffer, PossibleLoanOffer}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import java.time.LocalDate

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