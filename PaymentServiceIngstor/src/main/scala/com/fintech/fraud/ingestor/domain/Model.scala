package com.fintech.fraud.ingestor.domain
import spray.json.{DefaultJsonProtocol}


object Model {

   case class PaymentTransaction(transactionId: String)
   import DefaultJsonProtocol._
   implicit val paymentTransactionForamt = jsonFormat1(PaymentTransaction)
}
