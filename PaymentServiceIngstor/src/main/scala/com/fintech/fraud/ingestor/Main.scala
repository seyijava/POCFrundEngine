package com.fintech.fraud.ingestor

import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.stream.Materializer
import com.fintech.fraud.ingestor.api.PaymentTransactionIngestor
import com.typesafe.config.ConfigFactory
import org.apache.kafka.common.serialization.StringSerializer

import scala.concurrent.ExecutionContextExecutor

object Main extends App {

  val config = ConfigFactory.load()
  implicit val system: ActorSystem = ActorSystem("PaymentTransactionIngestor")
  implicit val ec: ExecutionContextExecutor = system.dispatcher
  implicit val mat: Materializer = Materializer(system)
  val topic = config.getString("InboundPayment")
  val bootStrapServer = config.getString("payment.kafka.bootstrap-server")


 implicit val producerSettings = ProducerSettings(system, new StringSerializer, new StringSerializer)
    .withBootstrapServers(bootStrapServer)

  val paymentIngestorRoute = new PaymentTransactionIngestor(topic)
}
