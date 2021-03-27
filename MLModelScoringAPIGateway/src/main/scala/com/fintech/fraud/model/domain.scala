package com.fintech.fraud.model
import spray.json.DefaultJsonProtocol._

object domain {


  case class Feature(accountNumber: String, encryptedDataPoint: String)

  case class Prediction(score: Double)


}
