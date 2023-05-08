package models

case class Customer(phoneNumber: String, creditScore: Float) {
  require((phoneNumber.length >= 10 && phoneNumber.length <= 13) && phoneNumber.forall(c => Character.isDigit(c)))
  require(0.0 <= creditScore && creditScore <= 1.0)
}
