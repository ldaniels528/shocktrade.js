package com.shocktrade.javascript.directives

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g, literal => JS}

/**
 * Stock Change Arrow Directive
 * @author lawrence.daniels@gmail.com
 *         <changeArrow value="{{ q.change }}"></changeArrow>
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

/*
class ChangeArrowDirective extends Directive {
  override type ScopeType = js.Dynamic
  override type ControllerType = js.Dynamic
  override val scope = true
  override val restrict = "E"
  override val transclude = true
  override val replace = false
  override val template = """<i ng-class="icon"></i>"""

  override def isolateScope = js.Dictionary[String]("value" -> "@value")

  override def preLink(scope: ScopeType, element: JQLite, attrs: Attributes) {
    scope.$watch("value", { (newValue: Any, oldValue: Any) =>
      g.console.log(s"scope.value = ${scope.value} (${Option(scope.value).map(_.getClass.getName).orNull}), newValue = $newValue, oldValue = $oldValue")
      val value = newValue match {
        case s: String if s.nonEmpty => s.toDouble
        case d: Double => d
        case _ => 0.0d
      }
      scope.icon = if (value >= 0) "fa fa-arrow-up positive" else "fa fa-arrow-down negative"
    })
  }
}*/
