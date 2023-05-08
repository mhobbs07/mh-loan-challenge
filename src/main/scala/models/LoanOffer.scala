package models

case class LoanOffer(amount: Int, fee: Int, term: Int) {
  require(amount > 0)
  require(0 <= fee && fee <= 100)
  require(term > 0)
}
