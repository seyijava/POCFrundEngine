package com.fintech.fraud.transaction.handler

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest, HttpResponse, StatusCodes}
import akka.http.scaladsl.unmarshalling.Unmarshal
import com.fintech.fraud.transaction.domain.domain.{HttpError, MLGatewayResponse, PaymentTransaction, Prediction, predictionFormat}
import scala.concurrent.Future
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.{Producer, SendProducer}
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import com.google.gson.Gson
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}
import spray.json._

trait ModelServingGateway{

        val modelServingGatewayUrl: String
        implicit val actorSystem : ActorSystem
        implicit val executionContext = actorSystem.dispatcher

     def getTransactionScore(paymentTransaction: PaymentTransaction) : Future[Either[MLGatewayResponse,HttpError]]= {
        val gson = new Gson()
       val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(method = HttpMethods.POST, uri = modelServingGatewayUrl,
         entity = HttpEntity(ContentTypes.`application/json`, gson.toJson(paymentTransaction))))
        responseFuture.map {
         case HttpResponse(StatusCodes.OK, headers, entity, _) =>{
           Left(MLGatewayResponse(paymentTransaction,Prediction(0)))
         }
         case _ => Right(HttpError(paymentTransaction,""))
       }
     }

  def parseJson(str: String): Prediction = {
    str.parseJson.convertTo[Prediction]
  }
}


class TransactionEventHandler(bootstrapServer:String, val modelServingGatewayUrl: String)(implicit val  actorSystem: ActorSystem, mat: Materializer) extends ModelServingGateway {

  val producerSettings = ProducerSettings(actorSystem, new StringSerializer, new StringSerializer)
    .withBootstrapServers(bootstrapServer)

  def processInboundPaymentTransaction(paymentEvent: String): Future[Unit] = {
    val paymentTransaction: PaymentTransaction  = null
    val gatewayResponse = getTransactionScore(paymentTransaction)
    gatewayResponse.map {
      case Left(gatewayResponse) => routeOutboundPaymentTransaction(gatewayResponse)
    }

  }


    def routeOutboundPaymentTransaction(mlGatewayResponse: MLGatewayResponse) : Future[Done]={
      val isFraud = getScore(mlGatewayResponse.prediction)
      val gson = new Gson()
      val destinationTopic = getTopic(isFraud,_)
      val paymentTopic =   routePaymentToKafkaDestinationTopic(mlGatewayResponse.paymentTransaction.paymentType,destinationTopic)
      val done: Future[Done] = Source(List(gson.toJson(mlGatewayResponse.paymentTransaction)))
        .map(_.toString)
           .map(value => new ProducerRecord[String, String](paymentTopic, value))
        .runWith(Producer.plainSink(producerSettings))
       done
    }

  def getScore(prediction: Prediction) : Boolean ={
    prediction.score match {case 1  => true
    case 0 => false
    }
  }

  def routePaymentToKafkaDestinationTopic(paymentType: String,f: String => String ): String  = {
    f(s"OutBoundPayment/$paymentType")
  }

  def getTopic(isFraud: Boolean, topic: String): String = isFraud match {
    case true => "FraudulentPayment"
    case  false => topic
  }
}
