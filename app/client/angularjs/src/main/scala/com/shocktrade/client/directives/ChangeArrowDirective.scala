package com.shocktrade.client.directives

import org.scalajs.angularjs.Directive._
import org.scalajs.angularjs.{Attributes, Directive, JQLite, Scope}

import scala.language.postfixOps
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.ScalaJSDefined
import scala.util.Try

/**
  * Change Arrow Directive
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  * @example <changearrow value="{{ q.change }}"></changearrow>
  */
class ChangeArrowDirective extends Directive with ElementRestriction with LinkSupport[ChangeArrowDirectiveScope] with TemplateSupport {
  override val scope = new ChangeArrowDirectiveInputs(value = "@value")
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
  * Change Arrow Directive Input Parameters
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@ScalaJSDefined
class ChangeArrowDirectiveInputs(val value: String) extends js.Object

/**
  * Change Arrow Directive Scope
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@js.native
trait ChangeArrowDirectiveScope extends ChangeArrowDirectiveInputs with Scope {
  // output fields
  var icon: js.UndefOr[String] = js.native

}


