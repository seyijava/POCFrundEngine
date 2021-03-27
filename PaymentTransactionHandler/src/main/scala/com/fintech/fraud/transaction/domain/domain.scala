package com.fintech.fraud.transaction.domain

import spray.json.{DefaultJsonProtocol, JsArray, JsNumber, JsObject, JsString, JsValue, RootJsonFormat,deserializationError}


object domain {
  import DefaultJsonProtocol._
  case class Prediction(score: Int)
  implicit val predictionFormat = jsonFormat1(Prediction)


  case class PaymentTransaction(transactionId: String, paymentType: String,creditCardNumber: String, amount: Double)

   case class MLGatewayResponse(paymentTransaction: PaymentTransaction, prediction: Prediction)
  case class HttpError(paymentTransaction: PaymentTransaction, msg: String)


}
