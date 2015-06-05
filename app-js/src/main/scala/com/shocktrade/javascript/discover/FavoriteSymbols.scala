package com.shocktrade.javascript.discover

import com.greencatsoft.angularjs.core.HttpService
import com.greencatsoft.angularjs.{Factory, Service, injectable}

/**
 * Favorite Symbols Service
 * @author lawrence.daniels@gmail.com
 */
@injectable("FavoriteSymbols")
class FavoriteSymbols(http: HttpService) extends Service {

  def add(symbol: String) = true

  def isFavorite(symbol: String) = true

  def remove(symbol: String) = true

}

@injectable("FavoriteSymbols")
class FavoriteSymbolsFactory(http: HttpService) extends Factory[FavoriteSymbols] {

  override def apply(): FavoriteSymbols = new FavoriteSymbols(http)

}