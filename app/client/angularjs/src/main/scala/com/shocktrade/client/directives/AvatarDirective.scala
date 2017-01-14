package com.shocktrade.client.directives

import io.scalajs.npm.angularjs.Directive._
import io.scalajs.npm.angularjs.{Attributes, Directive, JQLite, Scope, angular}
import io.scalajs.nodejs.social.facebook.TaggableFriend
import io.scalajs.util.JsUnderOrHelper._

import scala.scalajs.js
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
    scope.url = scope.id.map(id => s"http://graph.facebook.com/$id/picture") ??
      (scope.link map angular.fromJson[TaggableFriend] map (_.picture.data.url)) ?? UNKNOWN_PERSON

    // set the class
    scope.myClass = if (scope.url.contains(UNKNOWN_PERSON)) "spectatorAvatar" else "playerAvatar"
  }
}

/**
  * Avatar Directive Input Parameters
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@ScalaJSDefined
class AvatarDirectiveInputs(val id: js.UndefOr[String], val link: js.UndefOr[String], val `class`: String, val style: String) extends js.Object

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
