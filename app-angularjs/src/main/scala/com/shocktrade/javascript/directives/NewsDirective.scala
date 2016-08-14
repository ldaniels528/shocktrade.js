package com.shocktrade.javascript.directives

import org.scalajs.angularjs.Directive.{ElementRestriction, LinkSupport, TemplateSupport}
import org.scalajs.angularjs.sanitize.Sce
import org.scalajs.angularjs.{Attributes, Directive, JQLite, Scope}
import org.scalajs.dom.browser.console
import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * News Directive
  * @author lawrence.daniels@gmail.com
  * @example {{{ <news content="{{ myDate }}"></news> }}}
  */
class NewsDirective($sce: Sce) extends Directive with ElementRestriction with LinkSupport[NewsDirectiveScope] with TemplateSupport {
  override val scope = new NewsDirectiveInputs(`class` = "@class", content = "@content")
  override val template = """<div class="{{ class }}" ng-bind-html="output"></div>"""

  override def link(scope: NewsDirectiveScope, element: JQLite, attrs: Attributes): Unit = {
    scope.$watch("content", (newContent: js.UndefOr[String]) => scope.output = $sce.trustAsHtml(replaceTokens(newContent)))
  }

  private def replaceTokens(theContent: js.UndefOr[String]) = theContent.flat map { content =>
    val sb = new StringBuilder(content)
    var last = -1

    do {
      val start = sb.indexOf("{$", last)
      val end = sb.indexOf("$}", start)
      if (start != -1 && end > start) {
        last = end
        val token = sb.substring(start + 2, end - 1).trim
        token.split("[|]").map(_.trim).toList match {
          case symbol :: exchange :: changePct :: Nil =>
            console.log(s"symbol = $symbol, exchange = $exchange, changePct = $changePct")
            sb.replace(start, end + 2,
              s"""
              <a href="" ng-click="loadNewsQuote('$symbol')">
                <nobr>
                  <span class="$exchange">
                  $symbol <changearrow value="$changePct"></changearrow> {{ $changePct| quoteChange }}
                  </span>
                </nobr>
              </a>""")

          case values =>
            console.error(s"Invalid token values ('$token') => ${values.mkString(", ")}")
        }
      }
      else last = -1

    } while (last != -1)

    sb.toString()
  }

}

/**
  * News Directive Input Parameters
  * @author lawrence.daniels@gmail.com
  */
@ScalaJSDefined
class NewsDirectiveInputs(val `class`: String, val content: String) extends js.Object

/**
  * News Directive Scope
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait NewsDirectiveScope extends NewsDirectiveInputs with Scope {
  // output fields
  var output: js.Any = js.native

}

