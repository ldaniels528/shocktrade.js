package com.shocktrade.stockguru.directives

import org.scalajs.angularjs.Directive._
import org.scalajs.angularjs.{Attributes, Directive, JQLite, Scope, angular}
import org.scalajs.nodejs.util.ScalaJsHelper._
import org.scalajs.sjs.JsUnderOrHelper._

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Avatar Directive
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  * @example <avatar id="{{ p.facebookID }}" class="avatar-24"></avatar>
  */
class AvatarDirective extends Directive with ElementRestriction with LinkSupport[AvatarDirectiveScope] with TemplateSupport {
  private val UNKNOWN_PERSON = "/images/avatars/avatar100.png"
  override val scope = new AvatarDirectiveInputs(id = "@id", link = "@link", `class` = "@class", style = "@style")
  override val template = """<img ng-src="{{ url }}" ng-class="myClass" class="{{ class }}" style="{{ style }}">"""

  override def link(scope: AvatarDirectiveScope, element: JQLite, attrs: Attributes) = {
    scope.$watch("id", (newValue: Any, oldValue: Any) => populateScope(scope, newValue, oldValue))
    scope.$watch("link", (newValue: Any, oldValue: Any) => populateScope(scope, newValue, oldValue))
  }

  private def populateScope(scope: AvatarDirectiveScope, newValue: Any, oldValue: Any) {
    // determine the image URL
    scope.url = Option(scope.id).orUndefined.map(id => s"http://graph.facebook.com/$id/picture") ??
      (Option(scope.link).orUndefined map angular.fromJson flatMap (_.picture.data.url.asOpt[String].orUndefined)) ??
      UNKNOWN_PERSON

    // set the class
    scope.myClass = if (scope.url.contains(UNKNOWN_PERSON)) "spectatorAvatar" else "playerAvatar"
  }
}

/**
  * Avatar Directive Input Parameters
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@ScalaJSDefined
class AvatarDirectiveInputs(val id: String, val link: String, val `class`: String, val style: String) extends js.Object

/**
  * Avatar Directive Scope
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@js.native
trait AvatarDirectiveScope extends AvatarDirectiveInputs with Scope {
  // output fields
  var myClass: js.UndefOr[String] = js.native
  var url: js.UndefOr[String] = js.native

}
