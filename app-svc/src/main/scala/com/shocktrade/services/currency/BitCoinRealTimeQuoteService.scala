package com.shocktrade.services.currency

import com.shocktrade.services.HttpUtil

/**
 * BitCoin Real-time Quote Service
 * @author lawrence.daniels@gmail.com
 */
object BitCoinRealTimeQuoteService extends HttpUtil {
  private val logger = org.slf4j.LoggerFactory.getLogger(getClass)
  private val SCIENTIFIC_NOTATION_r = """(\d+(\.\d*)?)""".r

  /**
   * Retrieves a multi-market BitCoin quote (including BTCE, mtgox and others)
   */
  def getQuote: BitCoinQuote = {
    // capture the start time
    val startTime = System.currentTimeMillis()

    // retrieve & parse the document
    val doc = getResource("http://preev.com/pulse/source:bitstamp,btce,localbitcoins,mtgox/unit:btc,usd")

    transform(new String(doc), startTime)
  }

  /**
   * Transforms the TxBitCoinResults instance into a BitCoinQuote
   */
  private def transform(jsonString: String, startTime: Long): BitCoinQuote = {
    import net.liftweb.json._
    implicit val formats = DefaultFormats

    // capture the response time (in milliseconds)
    val responseTimeMsec = System.currentTimeMillis() - startTime

    // transform the JSON string into an object graph
    val graph = parse(jsonString).extract[TxBitCoinResults]

    // return the quote
    BitCoinQuote(
      "BTC=X",
      asDouble(graph.markets.bitstamp.price),
      asDouble(graph.markets.bitstamp.vol) map (_.toLong),
      asDouble(graph.markets.btce.price),
      asDouble(graph.markets.btce.vol) map (_.toLong),
      asDouble(graph.markets.localbitcoins.price),
      asDouble(graph.markets.localbitcoins.vol) map (_.toLong),
      asDouble(graph.markets.mtgox.price),
      asDouble(graph.markets.mtgox.vol) map (_.toLong),
      responseTimeMsec)
  }

  /**
   * Converts scientific notation into a double
   */
  private def asDouble(s: String): Option[Double] = {
    SCIENTIFIC_NOTATION_r.findAllIn(s).toList match {
      case a :: b :: Nil => Some(a.toDouble * Math.pow(10, b.toInt))
      case list =>
        logger.warn(s"scientific notation elements from '$s' are [${list map (s => s"'$s'") mkString ", "}]")
        None
    }
  }

  /**
   * Represents a multi-market BitCoin quote.
   *
   * BitCoin Markets/Exchanges:
   * --------------------------
   * 1. Bitstamp is an exchange based in Slovenia, where users can trade between
   * Bitcoins and US Dollars (https://www.bitstamp.net/).
   * 2. BTC-e (BTC) is a platform based in Bulgaria for buying and selling Bitcoin
   * and other crypto-currencies (https://btc-e.com/).
   * 3. Coinbase is the (self-proclaimed) easiest way for Americans to get money in
   * and out of Bitcoin (https://coinbase.com/).
   * 4. LocalBitcoins is a service that enables people to buy and sell Bitcoins between
   * each other in OTC fashion (https://localbitcoins.com/).
   * 5. MtGox (RTBTC) is an exchange based in Japan, which allows users to trade Bitcoins for US Dollars
   * and several other currencies (http://bitcoin.clarkmoody.com/ticker/).
   *
   * Other sources:
   * --------------
   * http://www.heavy.com/tech/2013/11/btc-e-bitcoin-cheaper-lower-price-less-cost/
   * http://bitcointicker.co/
   * http://bitcoincharts.com/markets/
   * https://en.bitcoin.it/wiki/MtGox/API/HTTP
   * http://bitcoinwatch.com/
   */
  case class BitCoinQuote(
                           symbol: String,
                           bitStampPrice: Option[Double],
                           bitStampVolume: Option[Long],
                           btcePrice: Option[Double],
                           btceVolume: Option[Long],
                           localBitcoinsPrice: Option[Double],
                           localBitcoinsVolume: Option[Long],
                           mtgoxPrice: Option[Double],
                           mtgoxVolume: Option[Long],
                           responseTimeMsec: Long)

  /**
   * BitCoin Response Object Graph
   * {
   * "markets":{
   * "bitstamp":{"price":"7.330e+02","vol":"7.586e+03"},
   * "btce":{"price":"7.300e+02","vol":"6.707e+03"},
   * "localbitcoins":{"price":"7.691e+02","vol":"3.724e+02"},
   * "mtgox":{"price":"8.059e+02","vol":"8.773e+03"}
   * },
   * "slot":null,
   * "ver":"b"
   * }
   */
  case class TxBitCoinResults(markets: TxBitCoinMarkets, slot: String)

  case class TxBitCoinMarkets(bitstamp: TxBitCoinQuote, btce: TxBitCoinQuote, localbitcoins: TxBitCoinQuote, mtgox: TxBitCoinQuote)

  case class TxBitCoinQuote(price: String, vol: String)

}