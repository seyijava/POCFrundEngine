package com.fintech.fraud

import akka.actor.{ActorSystem, Props}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import com.fintech.fraud.actor.MLServingModelActor
import com.typesafe.config.ConfigFactory


object ServingMLShadedServer extends App {

  val config = ConfigFactory.load("sharded")

  implicit val system = ActorSystem(config getString "application.name", config)

  ClusterSharding(system).start(
    typeName = MLServingModelActor.name,
    entityProps = MLServingModelActor.props,
    settings = ClusterShardingSettings(system),
    extractShardId = MLServingModelActor.extractShardId,
    extractEntityId = MLServingModelActor.extractEntityId
  )

  val mlServingModel = ClusterSharding(system).shardRegion(MLServingModelActor.name)

}
