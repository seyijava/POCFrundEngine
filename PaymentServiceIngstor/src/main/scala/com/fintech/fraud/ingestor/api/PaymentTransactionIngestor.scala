package com.fintech.fraud.ingestor.api

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl.Source
import com.fintech.fraud.ingestor.domain.Model.PaymentTransaction
import com.google.gson.Gson

import scala.concurrent.Future


object PaymentTransactionIngestor{
    def apply(topic: String)(implicit  system: ActorSystem, producerSettings: ProducerSettings[String,String]) = new PaymentTransactionIngestor(topic)
}

class PaymentTransactionIngestor(topic: String)(implicit  system: ActorSystem, producerSettings: ProducerSettings[String,String]){

  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

  import com.fintech.fraud.ingestor.domain.JsonFormats._

  val gson = new Gson()

  def publishInboundPayment(paymentTransaction: PaymentTransaction): Future[Done]={
    val done: Future[Done] = Source(Seq(gson.toJson(paymentTransaction)))
      .map(value => new ProducerRecord[String, String](topic, value))
      .runWith(Producer.plainSink(producerSettings))
    done
  }

  val route : Route ={
      pathPrefix("ingestor") {
        concat(
          post {
            entity(as[PaymentTransaction]) { payment =>
              system.log.info("Incoming " + payment.accountNumber)
              complete(publishInboundPayment(payment))
              }
          }
        )
      }
  }

}
