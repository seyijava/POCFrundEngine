package com.fintech.fraud.actor

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.cluster.sharding.ShardRegion.{ExtractEntityId, ExtractShardId}
import com.fintech.fraud.model.domain.{Feature, Prediction}
import akka.actor.{ActorLogging, Actor, Props}
import akka.cluster.sharding.ShardRegion
import scala.concurrent.Future


object MLServingModelActor {
  def name = "MlServingModel"



  def props (implicit  system: ActorSystem) : Props = Props(new MLServingModelActor())

  def extractShardId: ExtractShardId = {
    case Feature(accountNumber, _) =>
      (accountNumber % 2).toString
  }

  def extractEntityId: ExtractEntityId = {
    case msg @ Feature(accountNumber, _) =>
      (accountNumber.toString, msg)
  }
}


class MLServingModelActor()(implicit  system: ActorSystem) extends Actor with ActorLogging{

  implicit val executor = system.dispatcher

  override def receive: Receive = {
    case Feature(transactionId, dataPoint) =>
     val predict =  score(dataPoint)
      sender ! predict
  }


  def score(features: String): Future[Prediction] = {

    // look up ml model file
    Future(Prediction(0.5))
  }
}
