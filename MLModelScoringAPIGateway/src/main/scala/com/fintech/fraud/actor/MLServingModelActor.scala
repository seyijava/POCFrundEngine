package com.fintech.fraud.actor

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.cluster.sharding.ShardRegion.{ExtractEntityId, ExtractShardId}
import com.fintech.fraud.model.domain.{Feature, Prediction}
import akka.actor.{ActorLogging, Actor, Props}
import akka.cluster.sharding.ShardRegion
import scala.concurrent.Future


object MLServingModelActor {
  def name = "MlServingModel"

  def props = Props[MLServingModelActor]

  def extractShardId: ExtractShardId = {
    case Feature(transactionId, _) =>
      (transactionId % 2).toString
  }

  def extractEntityId: ExtractEntityId = {
    case msg @ Feature(transactionId, _) =>
      (transactionId.toString, msg)
  }
}


class MLServingModelActor(implicit val system: ActorSystem) extends Actor with ActorLogging{

  implicit val executor = system.dispatcher

  override def receive: Receive = {
    case Feature(transactionId, dataPoint) =>
     val predict =  predict(dataPoint)
      sender ! predict
  }


  def predict(features: String): Future[Prediction] = {

    // look up ml model file
    Future(Prediction(123, 456, 0.5))
  }
}
