package com.fintech.fraud.api

import akka.pattern.ask
import akka.actor.{ActorRef, ActorSystem}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import akka.http.scaladsl.server.Route
import com.fintech.fraud.model.domain.{Feature, Prediction}
import akka.http.scaladsl.server.Directives._
import java.util.concurrent.TimeUnit

import spray.json._


class MLScoringAPIGateway(mLServingModelActor: ActorRef)(implicit val actorSystem: ActorSystem)  {


  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import com.fintech.fraud.model.JsonFormats._

  val route: Route = {
    path("api/score") {
      post {
        entity(as[Feature]) { feature =>
          complete {
            mLServingModelActor.ask(feature)(Duration(5,TimeUnit.SECONDS)).mapTo[Prediction]
          }
        }
      }
     }
  }
}



