package com.fintech.fraud

import akka.actor.{ActorSystem, Props}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import com.fintech.fraud.actor.MLServingModelActor
import com.fintech.fraud.api.MLScoringAPIGateway
import com.typesafe.config.ConfigFactory


import scala.concurrent.Future
import scala.util.{Failure, Success}


object ServingMLShadedServer  {




  def main(args: Array[String]): Unit = {

    if (args.isEmpty) {
      startup( 25251)
      startup( 25252)
    } else {
      require(args.length == 1, "Usage:  port")
      startup( args(1).toInt)
    }
  }

  def startup(port: Int): Unit = {
    val config = ConfigFactory.load()
     // .parseString(s"""
       // akka.remote.artery.canonical.port=$port
        //""")
    implicit val system = ActorSystem("MLServingGateway", config)
    val httpPort : Int = 9080
    val httpIPaddress: String = "127.0.0.1"

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
    val futureBinding = Http().newServerAt(httpIPaddress, httpPort).bind(scoringGateway.route)
    futureBinding.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info("MLServingGateway online at http://{}:{}/", address.getHostString, address.getPort)
      case Failure(ex) =>
        system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
        system.terminate()
    }
  }



}
