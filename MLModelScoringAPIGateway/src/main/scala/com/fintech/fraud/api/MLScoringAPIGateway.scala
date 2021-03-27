package com.fintech.fraud.api

import akka.pattern.ask
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.fintech.fraud.model.domain.{Feature, Prediction}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._

trait APIJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val featureFormat: JsonFormat[Feature] = jsonFormat2(Feature)
  implicit val predictionFormat: JsonFormat[Prediction] = jsonFormat3(Prediction)
}

class MLScoringAPIGateway(mLServingModelActor: ActorRef)(implicit val actorSystem: ActorSystem) extends APIJsonSupport {



  val route: Route = {
    path("trans") {
      post {
        entity(as[Feature]) { feature =>
          complete {
            mLServingModelActor.ask(feature)(5 seconds).mapTo[Prediction]
          }
        }
      }
     }
  }
}



