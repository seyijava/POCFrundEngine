package com.fintech.fraud.model
import spray.json.DefaultJsonProtocol._

object domain {


  case class Feature(transactionId: String, encryptedDataPoint: String)

  case class Prediction(id: Long, timestamp: Long, value: Double)

  object Prediction {
    implicit val predictJson = jsonFormat1(Prediction.apply)
  }

}
