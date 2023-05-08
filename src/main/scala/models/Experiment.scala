package models

case class Experiment(name: String, startDate: String, endDate: String, offers: List[PossibleLoanOffer])
