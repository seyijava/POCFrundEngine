package com.fintech.fraud.transaction

import akka.actor.ActorSystem
import akka.kafka.{CommitterSettings, ConsumerMessage, ConsumerSettings, ProducerSettings, Subscriptions}
import akka.stream.{ActorMaterializer, Materializer}
import com.fintech.fraud.transaction.handler.TransactionEventHandler
import com.typesafe.config.ConfigFactory
import org.apache.kafka.common.serialization.StringDeserializer

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}
import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.kafka.ConsumerMessage.{CommittableMessage, CommittableOffset}
import akka.kafka.scaladsl.{Committer, Consumer, Producer}
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}
import spray.json._
import scala.util.Success
import scala.util.Failure
import akka.kafka.scaladsl.Consumer.DrainingControl
import akka.util.ByteString
import scala.concurrent.Future


object Main extends App {

  val config = ConfigFactory.load()
  implicit val system: ActorSystem = ActorSystem("PaymentTransactionHandler")
  implicit val ec: ExecutionContextExecutor = system.dispatcher
  implicit val mat: Materializer = Materializer(system)

  val topic = config.getString("payment.kafka.topic")
  val groupId = config.getString("payment.kafka.group-id")
  val bootStrapServer = config.getString("payment.kafka.bootstrap-server")
  val mLServingGatewayUrl = config.getString("payment.mlServing.gatewayurl")
  val committerSettings = CommitterSettings(system)

  val consumerSettings: ConsumerSettings[String, String] =
    ConsumerSettings(system, new StringDeserializer, new StringDeserializer).withBootstrapServers(bootStrapServer).withGroupId(groupId)
  val transactionHandler = new TransactionEventHandler(bootStrapServer,mLServingGatewayUrl)
  val control: DrainingControl[Done] =
    Consumer
      .committableSource(consumerSettings, Subscriptions.topics(topic))
      .mapAsync(1) { msg =>
        (transactionHandler.processInboundPaymentTransaction(msg.record.value)).map(
          _ => msg.committableOffset
        )
      }
      .toMat(Committer.sink(committerSettings))(DrainingControl.apply)
      .run()

  sys.addShutdownHook({
    println("Shutdown requested...")
    val done = control.shutdown()
    implicit val ec: ExecutionContextExecutor = system.dispatcher
    done
      .onComplete {
        case Success(_) => println("Exiting ...")
        case Failure(err) => println("Error", err)
      }
  })
}
