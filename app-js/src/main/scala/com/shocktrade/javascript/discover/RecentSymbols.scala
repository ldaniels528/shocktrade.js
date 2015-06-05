package com.shocktrade.javascript.discover

import com.greencatsoft.angularjs.core.{HttpService, Log, Q, RootScope}
import com.greencatsoft.angularjs.{Factory, Service, injectable}
import com.shocktrade.javascript.MySession

/**
 * Recent Symbols Service
 * @author lawrence.daniels@gmail.com
 */
@injectable("RecentSymbols")
class RecentSymbols($rootScope: RootScope, $http: HttpService, $log: Log, $q: Q, mySession: MySession) extends Service {

  def add(symbol: String) = {}

  def getLast: String = "AAPL"

  def isFavorite(symbol: String) = true

  def remove(symbol: String) = {}

  def setSymbols(symbols: Array[String]) = {

  }

}

@injectable("RecentSymbols")
class RecentSymbolsFactory($rootScope: RootScope, $http: HttpService, $log: Log, $q: Q, mySession: MySession) extends Factory[RecentSymbols] {

  override def apply(): RecentSymbols = new RecentSymbols($rootScope, $http, $log, $q, mySession)

}