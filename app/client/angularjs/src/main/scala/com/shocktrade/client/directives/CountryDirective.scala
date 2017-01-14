package com.shocktrade.client.directives

import io.scalajs.npm.angularjs.Directive.{ElementRestriction, LinkSupport, TemplateSupport}
import io.scalajs.npm.angularjs.{Attributes, Directive, JQLite, Scope}

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Country Directive
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class CountryDirective extends Directive with ElementRestriction with LinkSupport[CountryDirectiveScope] with TemplateSupport {

  override def scope = new CountryDirectiveInputs(profile = "@profile")

  override def template = """<img ng-src="{{ url }}" style="vertical-align: middle" />"""

  override def link(scope: CountryDirectiveScope, element: JQLite, attrs: Attributes): Unit = {
    scope.$watch("profile", (newProfile: js.Any, oldProfile: js.Any) => {
      scope.url = "/images/country/us.png"
    })
  }

}

/**
  * Country Directive Input Parameters
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@ScalaJSDefined
class CountryDirectiveInputs(val profile: String) extends js.Object

/**
  * Country Directive Scope
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@js.native
trait CountryDirectiveScope extends CountryDirectiveInputs with Scope {
  // output fields
  var url: js.UndefOr[String] = js.native
}
