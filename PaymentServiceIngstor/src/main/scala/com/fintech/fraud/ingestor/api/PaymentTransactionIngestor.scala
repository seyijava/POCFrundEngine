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
import com.fintech.fraud.ingestor.domain.Model._
import scala.concurrent.Future
import scala.concurrent.duration._



class PaymentTransactionIngestor(topic: String)(implicit val system: ActorSystem, producerSettings: ProducerSettings[String,String]){

  def publishInboundPayment(transaction: PaymentTransaction): Future[Done]={
    val done: Future[Done] = Source(Seq(""))
      .map(value => new ProducerRecord[String, String](topic, value))
      .runWith(Producer.plainSink(producerSettings))
    done
  }


  val route : Route ={
      pathPrefix("ingestor") {
        concat(
          post {
            entity(as[PaymentTransaction]) { payment =>
              onSuccess(publishInboundPayment(payment))
              complete(StatusCodes.OK)
              }
          }
        )
      }
  }


}
