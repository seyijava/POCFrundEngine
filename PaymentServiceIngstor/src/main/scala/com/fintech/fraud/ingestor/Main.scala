package com.fintech.fraud.ingestor

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.kafka.ProducerSettings
import akka.stream.Materializer
import com.fintech.fraud.ingestor.api.PaymentTransactionIngestor
import com.typesafe.config.ConfigFactory
import org.apache.kafka.common.serialization.StringSerializer

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

object Main extends App {

  val config = ConfigFactory.load()
  implicit val system: ActorSystem = ActorSystem("PaymentTransactionIngestor")
  implicit val ec: ExecutionContextExecutor = system.dispatcher
  implicit val mat: Materializer = Materializer(system)
  val topic = config.getString("payment.kafka.inboundTopic")
  val bootStrapServer = config.getString("payment.kafka.bootstrap-server")
  val httpHost = config.getString("payment.http.host")
  val httpPort = config.getString("payment.http.port")

 implicit val producerSettings = ProducerSettings(system, new StringSerializer, new StringSerializer)
    .withBootstrapServers(bootStrapServer)

  val paymentIngestorRoute =  PaymentTransactionIngestor(topic)

  val futureBinding = Http().newServerAt(httpHost,httpPort.toInt).bind(paymentIngestorRoute.route)
  futureBinding.onComplete {
    case Success(binding) =>
      val address = binding.localAddress
      system.log.info("\nMLServingGateway online at http://{}:{}/", address.getHostString, address.getPort)
    case Failure(ex) =>
      system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
      system.terminate()
  }

}
