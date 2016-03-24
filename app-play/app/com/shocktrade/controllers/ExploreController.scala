package com.shocktrade.controllers

import javax.inject.Inject

import com.shocktrade.dao.SecuritiesDAO
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{JsArray, Json}
import play.api.mvc.Action
import play.modules.reactivemongo.json.BSONFormats._
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}

/**
  * Explore Controller
  * @author lawrence.daniels@gmail.com
  */
class ExploreController @Inject()(val reactiveMongoApi: ReactiveMongoApi) extends MongoController with ReactiveMongoComponents {
  private val securitiesDAO = SecuritiesDAO(reactiveMongoApi)

  def exploreSectors(userID: String) = Action.async {
    securitiesDAO.exploreSectors(userID) map (quotes => Ok(JsArray(quotes.map(Json.toJson(_)))))
  }

  def exploreIndustries(userID: String, sector: String) = Action.async {
    securitiesDAO.exploreIndustries(userID, sector) map (quotes => Ok(JsArray(quotes.map(Json.toJson(_)))))
  }

  def exploreSubIndustries(userID: String, sector: String, industry: String) = Action.async {
    securitiesDAO.exploreSubIndustries(userID, sector, industry) map (quotes => Ok(JsArray(quotes.map(Json.toJson(_)))))
  }

  def exploreQuotesBySubIndustry(userID: String, sector: String, industry: String, subIndustry: String) = Action.async {
    securitiesDAO.exploreQuotesBySubIndustry(userID, sector, industry, subIndustry) map (quotes => Ok(JsArray(quotes.map(Json.toJson(_)))))
  }

  def exploreNAICSSectors = Action.async {
    securitiesDAO.exploreNAICSSectors map (js => Ok(Json.toJson(js)))
  }

  def exploreSICSectors = Action.async {
    securitiesDAO.exploreSICSectors map (js => Ok(Json.toJson(js)))
  }

  def getSectorInfo(symbol: String) = Action.async {
    securitiesDAO.getSectorInfo(symbol) map (quote => Ok(Json.toJson(quote)))
  }

}
