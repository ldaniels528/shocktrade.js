package com.shocktrade.javascript.directives

import org.scalajs.angularjs.Directive._
import org.scalajs.angularjs.{Attributes, Directive, JQLite, Scope}
import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.language.postfixOps
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.util.Try

/**
  * Change Arrow Directive
  * @author lawrence.daniels@gmail.com
  * @example <changearrow value="{{ q.change }}"></changearrow>
  */
class ChangeArrowDirective extends Directive with ElementRestriction with LinkSupport[ChangeArrowDirectiveScope] with TemplateSupport {
  override val scope = ChangeArrowDirectiveScope(value = "@value")
  override val transclude = true
  override val template = """<i ng-class="icon"></i>"""

  override def link(scope: ChangeArrowDirectiveScope, element: JQLite, attrs: Attributes) = {
    scope.$watch("value", { (newValue: js.UndefOr[Any], oldValue: js.UndefOr[Any]) =>
      scope.icon = newValue.toOption flatMap getNumericValue map {
        case v if v > 0 => "fa fa-arrow-up positive"
        case v if v < 0 => "fa fa-arrow-down negative"
        case _ => "fa fa-minus null"
      } orUndefined
    })
  }

  private def getNumericValue(newValue: Any): Option[Double] = newValue match {
    case n: Number => Some(n.doubleValue)
    case s: String if s.nonEmpty => Try(s.toDouble).toOption
    case _ => None
  }

}

/**
  * Change Arrow Directive Scope
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait ChangeArrowDirectiveScope extends Scope {
  // input fields
  var value: js.UndefOr[Any] = js.native

  // output fields
  var icon: js.UndefOr[String] = js.native

}

/**
  * Change Arrow Directive Scope Singleton
  * @author lawrence.daniels@gmail.com
  */
object ChangeArrowDirectiveScope {

  def apply(value: String): ChangeArrowDirectiveScope = {
    val scope = New[ChangeArrowDirectiveScope]
    scope.value = value
    scope
  }

}

