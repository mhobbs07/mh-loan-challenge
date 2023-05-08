
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import models.{Customer, Experiments}
import spray.json._
import utility.LoanJsonProtocol

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContextExecutor}
import scala.io.Source

object ExampleApp extends App with LoanJsonProtocol with SprayJsonSupport {

  import utility.LoanFinder._
  implicit val system: ActorSystem = ActorSystem("loan-service")
  /*
    Having a default materializer available means that most, if not all, usages of Java ActorMaterializer.create()
    and Scala implicit val materializer = ActorMaterializer() should be removed.
   */
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  val experiments = validOffers(Source.fromResource("experiments.json").mkString.parseJson.convertTo[Experiments])
  //TODO need to handle the case of invalid parameters. Also, would be better to have validOffers return an Option type.
  if(experiments.size  != 1) system.terminate()
  println(experiments)
  val routes =
    (path("api" / "offers") & post) {
      entity(as[Customer]) { customer =>
        //pass the customer to a function that will convert the data to a list of loanOffers.
        complete(findEligibleOffers(customer.creditScore, experiments))
      }
    }
  val binding = Http().newServerAt("localhost", 9000).bind(routes)
  Await.result(system.whenTerminated, Duration.Inf)
}
