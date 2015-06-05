package com.shocktrade.javascript.news

import com.greencatsoft.angularjs.{Factory, Service, injectable}
import com.shocktrade.javascript.discover.{FavoriteSymbols, HeldSecurities}
import com.shocktrade.javascript.news.NewsSymbols.NewsQuote

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport

/**
 * News Symbols Service
 * @author lawrence.daniels@gmail.com
 */
@injectable("NewsSymbols")
class NewsSymbols(favoriteSymbols: FavoriteSymbols, heldSecurities: HeldSecurities) extends Service {
  private var quotes: js.Array[NewsQuote] = js.Array()

  def isEmpty = quotes.isEmpty

  def setNewsQuotes(quotes: js.Array[NewsQuote]) = this.quotes = quotes

  def getNewsQuotes = {
    quotes.foreach { quote =>
      quote.favorite = favoriteSymbols.isFavorite(quote.symbol)
      quote.held = heldSecurities.isHeld(quote.symbol)
    }
    quotes
  }

}

@injectable("NewsSymbols")
class NewsSymbolsFactory(favoriteSymbols: FavoriteSymbols, heldSecurities: HeldSecurities) extends Factory[NewsSymbols] {

  override def apply(): NewsSymbols = new NewsSymbols(favoriteSymbols, heldSecurities)

}

/**
 * News Symbols Service Singleton
 * @author lawrence.daniels@gmail.com
 */
object NewsSymbols {

  case class NewsQuote(symbol: String) {
    var favorite: Boolean = _
    var held: Boolean = _
  }

}