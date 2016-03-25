package com.shocktrade.services.currency

import java.text.SimpleDateFormat
import java.util.Date

import scala.xml.{Node, NodeSeq, XML}

/**
 * European FX Currency Service
 * @author lawrence.daniels@gmail.com
 */
object EuroFXCurrencyService {

  /**
   * Parses currencies from Europa.eu
   * @see http://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml
   */
  def getQuotes: Seq[EFXCurrency] = {
    // create the date parser
    implicit val sdf = new SimpleDateFormat("yyyy-MM-dd")

    // capture the service start time
    val startTime = System.currentTimeMillis()

    // retrieve & parse the document
    val doc = XML.load("http://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml")
    parseDocument(doc, startTime)
  }

  private def parseDocument(doc: NodeSeq, startTime: Long)(implicit sdf: SimpleDateFormat): Seq[EFXCurrency] = {
    // get the document date
    val pubDate = ((doc \ "Cube" \ "Cube" \ "@time") map (_.text)).headOption map sdf.parse
    val baseCurrency = Some("EUR")

    // transform the document into a sequence of quotes
    (doc \ "Cube" \ "Cube" \ "Cube") map { node =>
      val currency = extract(node, "@currency")
      val rate = extract(node, "@rate") map (_.toDouble)

      EFXCurrency(
        getTitle(currency, rate),
        getDescription(currency, rate),
        pubDate,
        baseCurrency,
        currency,
        extract(node, "@rate") map (_.toDouble))
    }
  }

  private def getTitle(currencyCode: Option[String], rate: Option[Double]) = {
    for {
      c <- currencyCode
      r <- rate
    } yield s"1 EUR = $r $c"
  }

  private def getDescription(currencyCode: Option[String], rate: Option[Double]) = {
    for {
      c <- getCurrencyName(currencyCode)
      r <- rate
    } yield s"1 Euro = $r $c"
  }

  private def getCurrencyName(currencyCode: Option[String]) = {
    for {
      code <- currencyCode
      name = currencies.getOrElse(code, code)
    } yield name
  }

  private def extract(node: Node, name: String): Option[String] = {
    (node \ name).map(_.text).headOption
  }

  case class EFXCurrency(
                          title: Option[String],
                          description: Option[String],
                          pubDate: Option[Date],
                          baseCurrency: Option[String],
                          targetCurrency: Option[String],
                          exchangeRate: Option[Double])

  val currencies = Map(
    "ANG" -> "Neth. Antillean Guilder",
    "ARS" -> "Argentine Peso",
    "AUD" -> "Australian Dollar",
    "BGN" -> "Bulgarian Lev",
    "BND" -> "Brunei dollar",
    "BSD" -> "Bahamian Dollar",
    "BWP" -> "Botswana pula",
    "CAD" -> "Canadian Dollar",
    "CHF" -> "Swiss Franc",
    "CLP" -> "Chilean peso",
    "CNY" -> "Chinese yuan",
    "CZK" -> "Czech koruna",
    "DKK" -> "Danish krone",
    "FJD" -> "Fiji Dollar",
    "GBP" -> "U.K. Pound Sterling",
    "GHS" -> "Ghanaian Cedi",
    "HKD" -> "Hong Kong Dollar",
    "HNL" -> "Honduran Lempira",
    "HRK" -> "Croatian Kuna",
    "HUF" -> "Hungarian forint",
    "IDR" -> "Indonesian rupiah",
    "ILS" -> "Israeli new sheqel",
    "ISK" -> "Icelandic krona",
    "JMD" -> "Jamaican Dollar",
    "JPY" -> "Japanese Yen",
    "KRW" -> "South Korean Won",
    "KWD" -> "Kuwaiti dinar",
    "KTZ" -> "Kazakhstani tenge",
    "LKR" -> "Sri Lanka rupee",
    "LTL" -> "Lithuanian Litas",
    "LVL" -> "Latvian Lats",
    "LYD" -> "Libyan dinar",
    "MAD" -> "Moroccan Dirham",
    "MMK" -> "Myanma Kyat",
    "MUR" -> "Mauritian rupee",
    "MXN" -> "Mexican Peso",
    "NOK" -> "Norwegian krone",
    "NZD" -> "New Zealand dollar",
    "NPR" -> "Nepalese rupee",
    "OMR" -> "Omani rial",
    "PAB" -> "Panamanian Balboa",
    "PHP" -> "Philippine Peso",
    "PKR" -> "Pakistani rupee",
    "QAR" -> "Qatari rial",
    "RON" -> "Romanian New Leu",
    "RSD" -> "Serbian Dinar",
    "RUB" -> "Russian rouble",
    "SAR" -> "Saudi riyal",
    "SGD" -> "Singapore dollar",
    "THB" -> "Thai baht",
    "TND" -> "Tunisian dinar",
    "TRY" -> "Turkish Lira",
    "TTD" -> "Trinidad Tobago Dollar",
    "TWD" -> "New Taiwan Dollar",
    "USD" -> "U.S. Dollar",
    "VEF" -> "Venezuelan Bolivar",
    "XCD" -> "East Caribbean Dollar",
    "XPF" -> "CFP Franc",
    "ZAR" -> "South African rand")

}