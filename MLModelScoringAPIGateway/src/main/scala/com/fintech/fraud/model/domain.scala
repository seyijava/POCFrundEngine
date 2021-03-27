package com.fintech.fraud.model

import com.fintech.fraud.model.domain.{Feature, Prediction}


trait CborSerializable

object JsonFormats  {
  // import the default encoders for primitive types (Int, String, Lists etc)
  import spray.json.DefaultJsonProtocol._

  implicit val featureJsonFormat = jsonFormat2(Feature)
  implicit val predictionJsonFormat = jsonFormat1(Prediction)


}


object domain {


  case class Feature(accountNumber: Long, encryptedDataPoint: String) extends CborSerializable

  case class Prediction(score: Double) extends CborSerializable


}
