package com.shocktrade.javascript.dashboard

import com.greencatsoft.angularjs.core._
import com.greencatsoft.angularjs.{Factory, Service, injectable}
import com.shocktrade.javascript.{MySession, ServiceSupport}
import prickle.Unpickle

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scala.scalajs.js
import scala.scalajs.js.JSON

/**
 * Explorer Services
 * @author lawrence.daniels@gmail.com
 */
@injectable("ExplorerService")
class ExplorerService($rootScope: RootScope, $http: HttpService, $log: Log, mySession: MySession) extends Service {

  def loadSectorInfo(symbol: String) = $http.get(s"/api/explore/symbol/$symbol") //.mapTo[js.Dynamic]

  def loadSectors = $http.get(s"/api/${profile}explore/sectors") //.mapTo[js.Dynamic]

  def loadNAICSSectors = $http.get(s"/api/${profile}explore/naics/sectors") //.mapTo[js.Dynamic]

  def loadIndustries(sector: String) = $http.get(s"/api/${profile}explore/industries?sector=$sector") //.mapTo[js.Dynamic]

  def loadSubIndustries(sector: String, industry: String) = {
    $http.get(s"/api/${profile}explore/subIndustries?sector=$sector&industry=$industry") //.mapTo[js.Dynamic]
  }

  def loadIndustryQuotes(sector: String, industry: String, subIndustry: String) {
    $http.get(s"/api/${profile}explore/quotes?sector=$sector&industry=$industry&subIndustry=$subIndustry") //.mapTo[js.Dynamic]
  }

  private def profile = mySession.getUserID.map(id => s"profile/$id/").getOrElse("")

}

object ExplorerService {

  case class ExQuote(symbol: String, sector: String, industry: String, subIndustry: String, lastTrade: Double, open: Double, close: Double)

  implicit class HttpPromiseExtensions(promise: HttpPromise) extends ServiceSupport {

    def toExplore = flatten {
      val future: Future[js.Any] = promise
      future
        .map(JSON.stringify(_))
        .map(Unpickle[ExQuote].fromString(_))
    }
  }

}

@injectable("ExplorerService")
class ExplorerServiceFactory($rootScope: RootScope, $http: HttpService, $log: Log, mySession: MySession) extends Factory[ExplorerService] {

  override def apply(): ExplorerService = new ExplorerService($rootScope, $http, $log, mySession)

}