package com.fintech.fraud.ingestor.domain
import com.fintech.fraud.ingestor.domain.Model.PaymentTransaction
import spray.json.DefaultJsonProtocol


object JsonFormats  {
   // import the default encoders for primitive types (Int, String, Lists etc)
   import spray.json.DefaultJsonProtocol._


   implicit val paymentTransactionForamt = jsonFormat4(PaymentTransaction)


}


object Model {

   case class PaymentTransaction(accountNumber: Long, creditCardNumber: String,amount:Double, paymentType: String)

}
