package com.shocktrade.client.directives

import com.shocktrade.client.EmoticonSupport
import io.scalajs.npm.angularjs.Directive._
import io.scalajs.npm.angularjs.sanitize.Sce
import io.scalajs.npm.angularjs.{Attributes, Directive, JQLite, Scope}
import io.scalajs.util.ScalaJsHelper._
import io.scalajs.util.JsUnderOrHelper._

import scala.scalajs.js

/**
  * News Post Directive
  * @author lawrence.daniels@gmail.com
  * @example <news-post text="{{ post.text }}"></news-post>
  */
class NewsPostDirective($sce: Sce) extends Directive
  with ElementRestriction with EmoticonSupport with LinkSupport[NewsPostDirectiveScope] with TemplateSupport {

  override val scope = NewsPostDirectiveScope(text = "=", callback = "&")
  override val template = """<span ng-bind-html="html"></span>"""

  override def link(scope: NewsPostDirectiveScope, element: JQLite, attrs: Attributes): Unit = {
    scope.$watch("text", (newText: js.UndefOr[String], oldText: js.UndefOr[String]) => {
      scope.html = newText.flat map enrichHashTags map enrichWithEmoticons
    })
  }

  private def enrichHashTags(text: String) = {
    if (text.contains('#')) {
      val sb = new StringBuilder(text)
      var lastPos = -1
      do {
        val start = sb.indexOf('#', lastPos)
        if (start != -1) {
          val end = sb.indexOf(' ', start)
          val limit = if (end != -1) end else sb.length
          val hashTag = sb.substring(start, limit)
          val tag = hashTag.tail
          val hashTagWithLink = s"""<a ng-click="callback({'tag': '$tag'})">$hashTag</a>"""

          sb.replace(start, limit, hashTagWithLink)
          lastPos = start + hashTagWithLink.length
        }
        else lastPos = -1
      } while (lastPos != -1 && lastPos < sb.length)

      sb.toString()
    }
    else text
  }

}

/**
  * News Post Directive Scope
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait NewsPostDirectiveScope extends Scope {
  // input fields
  var text: js.UndefOr[String] = js.native
  var callback: js.UndefOr[String] = js.native

  /// output fields
  var html: js.UndefOr[String] = js.native
}

/**
  * News Post Directive Scope Companion
  * @author lawrence.daniels@gmail.com
  */
object NewsPostDirectiveScope {

  def apply(text: String, callback: String): NewsPostDirectiveScope = {
    val scope = New[NewsPostDirectiveScope]
    scope.text = text
    scope.callback = callback
    scope
  }

}

