package com.shocktrade.javascript.directives

import com.ldaniels528.scalascript.core.{JQLite, Attributes}
import com.ldaniels528.scalascript.{Scope, Directive}

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g, literal => JS}

/**
 * Stock Change Arrow Directive
 * @author lawrence.daniels@gmail.com
 * @example <changeArrow value="{{ q.change }}"></changeArrow>
 */
object ChangeArrowDirective {

  def init(): Unit = {
    val app = g.angular.module("shocktrade")

    val linkFx = { (scope: js.Dynamic, element: js.Dynamic, attrs: js.Dynamic) =>
      scope.$watch("value", { (newValue: Any, oldValue: Any) =>
        val value = newValue match {
          case s: String if s.nonEmpty => s.toDouble
          case d: Double => d
          case _ => 0.0d
        }
        scope.icon = value match {
          case v if v > 0 => "fa fa-arrow-up positive"
          case v if v < 0 => "fa fa-arrow-down negative"
          case _ => "fa fa-minus null"
        }
      })
    }

    app.directive("changearrow", js.Array({ () =>
      JS(
        "restrict" -> "E",
        "scope" -> JS(value = "@value"),
        "transclude" -> true,
        "replace" -> false,
        "template" -> """<i ng-class="icon"></i>""",
        "link" -> linkFx
      )
    }: js.Function0[js.Dynamic]))
  }

}

/**
 * Change Arrow Directive
 * @author lawrence.daniels@gmail.com
 * @example <changeArrow value="{{ q.change }}"></changeArrow>
 */
class ChangeArrowDirective extends Directive[ChangeArrowDirectiveScope] {
  override val scope = ChangeArrowDirectiveScope()
  override val restrict = "E"
  override val transclude = true
  override val replace = false
  override val template = """<i ng-class="icon"></i>"""

  def link(scope: ChangeArrowDirectiveScope, element: JQLite, attrs: Attributes) = {
    scope.$watch("value", { (newValue: Any, oldValue: Any) =>
      val value = newValue match {
        case s: String if s.nonEmpty => s.toDouble
        case d: Double => d
        case _ => 0.0d
      }
      scope.icon = value match {
        case v if v > 0 => "fa fa-arrow-up positive"
        case v if v < 0 => "fa fa-arrow-down negative"
        case _ => "fa fa-minus null"
      }
    })
  }
}

/**
 * Change Arrow Directive Scope
 * @author lawrence.daniels@gmail.com
 */
trait ChangeArrowDirectiveScope extends Scope {
  var value: String = js.native
  var icon: String = js.native

}

/**
 * Change Arrow Directive Scope Singleton
 * @author lawrence.daniels@gmail.com
 */
object ChangeArrowDirectiveScope {

  def apply(): ChangeArrowDirectiveScope = {
    val scope = new js.Object().asInstanceOf[ChangeArrowDirectiveScope]
    scope
  }

}

