package com.shocktrade.client

import com.shocktrade.client.EmoticonSupport.Emoticon

import scala.scalajs.js

/**
  * Emoticon Support
  * @author lawrence.daniels@gmail.com
  */
trait EmoticonSupport {

  def enrichWithEmoticons(text: String) = Emoticons.foldLeft(text) { (html, emoticon) =>
    html.replaceAllLiterally(emoticon.symbol, s"""<img src="/images/smilies/${emoticon.uri}">""")
  }

  private val Emoticons = js.Array(
    new Emoticon(symbol = ":-@", uri = "icon_mrgreen.gif", tooltip = "Big Grin"),
    new Emoticon(symbol = ":-)", uri = "icon_smile.gif", tooltip = "Smile"),
    new Emoticon(symbol = ";-)", uri = "icon_wink.gif", tooltip = "Wink"),
    new Emoticon(symbol = ":-D", uri = "icon_biggrin.gif", tooltip = "Big Smile"),
    new Emoticon(symbol = ":->", uri = "icon_razz.gif", tooltip = "Razzed"),
    new Emoticon(symbol = "B-)", uri = "icon_cool.gif", tooltip = "Cool"),
    new Emoticon(symbol = "$-|", uri = "icon_rolleyes.gif", tooltip = "Roll Eyes"),
    new Emoticon(symbol = "8-|", uri = "icon_eek.gif", tooltip = "Eek"),
    new Emoticon(symbol = ":-/", uri = "icon_confused.gif", tooltip = "Confused"),
    new Emoticon(symbol = "|-|", uri = "icon_redface.gif", tooltip = "Blush"),
    new Emoticon(symbol = ":-(", uri = "icon_sad.gif", tooltip = "Sad"),
    new Emoticon(symbol = ":'-(", uri = "icon_cry.gif", tooltip = "Cry"),
    new Emoticon(symbol = ">:-(", uri = "icon_evil.gif", tooltip = "Enraged"),
    new Emoticon(symbol = ":-|", uri = "icon_neutral.gif", tooltip = "Neutral"),
    new Emoticon(symbol = ":-O", uri = "icon_surprised.gif", tooltip = "Surprised"),
    new Emoticon(symbol = "(i)", uri = "icon_idea.gif", tooltip = "Idea"),
    new Emoticon(symbol = "(!)", uri = "icon_exclaim.gif", tooltip = "Exclamation"),
    new Emoticon(symbol = "(?)", uri = "icon_question.gif", tooltip = "Question"),
    new Emoticon(symbol = "=>", uri = "icon_arrow.gif", tooltip = "Arrow"))

}

/**
  * Emoticon Support Companion
  * @author lawrence.daniels@gmail.com
  */
object EmoticonSupport {

  /**
    * Represents an Emoticon
    * @author lawrence.daniels@gmail.com
    */
  class Emoticon(var symbol: String, var uri: String, var tooltip: String) extends js.Object

}
