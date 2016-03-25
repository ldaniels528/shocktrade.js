package test

import scala.xml.XML
import org.junit.Test

class TagsoupTest {
  
  @Test
  def test() {
    val symbol = "AAPL"
    val doc = XML
      .withSAXParser(new org.ccil.cowan.tagsoup.jaxp.SAXFactoryImpl().newSAXParser())
      .load(s"http://finance.yahoo.com/q/ks?s=${symbol}+Key+Statistics")
    System.out.println(doc)
  }

}