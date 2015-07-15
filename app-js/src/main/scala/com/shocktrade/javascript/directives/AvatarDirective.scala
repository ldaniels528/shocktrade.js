package com.shocktrade.javascript.directives

import com.shocktrade.javascript.ScalaJsHelper
import ScalaJsHelper._
import com.github.ldaniels528.scalascript.core.{Attributes, JQLite}
import com.github.ldaniels528.scalascript.{Directive, Scope, angular}
import com.shocktrade.javascript.directives.AvatarDirective.UNKNOWN_PERSON

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{literal => JS}

/**
 * Avatar Directive
 * @author lawrence.daniels@gmail.com
 * @example <avatar id="{{ p.facebookID }}" class="avatar-24"></avatar>
 */
class AvatarDirective extends Directive[AvatarDirectiveScope] {
  override val restrict = "E"
  override val scope = JS(id = "@id", link = "@link", `class` = "@class", style = "@style")
  override val transclude = true
  override val replace = false
  override val template = """<img ng-src="{{ url }}" ng-class="myClass" class="{{ class }}" style="{{ style }}">"""

  override def link(scope: AvatarDirectiveScope, element: JQLite, attrs: Attributes) = {
    scope.$watch("id", (newValue: Any, oldValue: Any) => populateScope(scope, newValue, oldValue))
    scope.$watch("link", (newValue: Any, oldValue: Any) => populateScope(scope, newValue, oldValue))
  }

  private def populateScope(scope: AvatarDirectiveScope, newValue: Any, oldValue: Any) {
    // determine the image URL
    scope.url = scope.id.toOption map (id => s"http://graph.facebook.com/$id/picture") getOrElse {
      scope.link.toOption map angular.fromJson flatMap (_.picture.data.url.asOpt[String]) getOrElse UNKNOWN_PERSON
    }

    // set the ng-class
    scope.myClass = if (scope.url == UNKNOWN_PERSON) "spectatorAvatar" else "playerAvatar"
  }
}

/**
 * Avatar Directive Singleton
 * @author lawrence.daniels@gmail.com
 */
object AvatarDirective {
  private val UNKNOWN_PERSON = "/assets/images/avatars/avatar100.png"

}

/**
 * Avatar Directive Scope
 * @author lawrence.daniels@gmail.com
 */
trait AvatarDirectiveScope extends Scope {
  // input fields
  var id: js.UndefOr[String] = js.native
  var link: js.UndefOr[String] = js.native
  var `class`: js.UndefOr[String] = js.native
  var style: js.UndefOr[String] = js.native

  // output fields
  var myClass: String = js.native
  var url: String = js.native

}

/**
 * Avatar Directive Scope Singleton
 * @author lawrence.daniels@gmail.com
 */
object AvatarDirectiveScope {

  def apply(): AvatarDirectiveScope = {
    val scope = new js.Object().asInstanceOf[AvatarDirectiveScope]
    scope.myClass = null
    scope.url = null
    scope
  }

}