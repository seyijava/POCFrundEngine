package com.fintech.fraud

import akka.actor.{ActorSystem, Props}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import com.fintech.fraud.actor.MLServingModelActor
import com.fintech.fraud.api.MLScoringAPIGateway
import com.typesafe.config.ConfigFactory

import scala.concurrent.Future


object ServingMLShadedServer extends App {

  val config = ConfigFactory.load("sharded")

  implicit val system = ActorSystem(config getString "application.name", config)

  val httpPort : Int = 0

  val httpIPaddress: String = "localhost"

  implicit  val ec = system.dispatcher

  ClusterSharding(system).start(
    typeName = MLServingModelActor.name,
    entityProps = MLServingModelActor.props,
    settings = ClusterShardingSettings(system),
    extractShardId = MLServingModelActor.extractShardId,
    extractEntityId = MLServingModelActor.extractEntityId
  )

  val mlServingModel = ClusterSharding(system).shardRegion(MLServingModelActor.name)

  val scoringGateway = new MLScoringAPIGateway(mlServingModel)

  val bindingFuture: Future[ServerBinding] =
    Http().bindAndHandle(scoringGateway.route, httpIPaddress, httpPort)

  bindingFuture.failed.foreach { ex =>
    system.log.error(ex, "Failed to bind to {}:{}!", httpIPaddress, httpPort)
  }
}
