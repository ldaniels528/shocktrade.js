package com.shocktrade.javascript.directives

import com.shocktrade.javascript.directives.AvatarDirective.UNKNOWN_PERSON
import org.scalajs.angularjs.Directive._
import org.scalajs.angularjs.{Attributes, Directive, JQLite, Scope, angular}
import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.scalajs.js
import scala.scalajs.js.JSConverters._

/**
  * Avatar Directive
  * @author lawrence.daniels@gmail.com
  * @example <avatar id="{{ p.facebookID }}" class="avatar-24"></avatar>
  */
class AvatarDirective extends Directive with ElementRestriction with LinkSupport[AvatarDirectiveScope] with TemplateSupport {
  override val scope = AvatarDirectiveScope(id = "@id", link = "@link", `class` = "@class", style = "@style")
  override val transclude = true
  override val template = """<img ng-src="{{ url }}" ng-class="myClass" class="{{ class }}" style="{{ style }}">"""

  override def link(scope: AvatarDirectiveScope, element: JQLite, attrs: Attributes) = {
    scope.$watch("id", (newValue: Any, oldValue: Any) => populateScope(scope, newValue, oldValue))
    scope.$watch("link", (newValue: Any, oldValue: Any) => populateScope(scope, newValue, oldValue))
  }

  private def populateScope(scope: AvatarDirectiveScope, newValue: Any, oldValue: Any) {
    // determine the image URL
    scope.url = scope.id.map(id => s"http://graph.facebook.com/$id/picture") ??
      (scope.link.toOption map angular.fromJson flatMap (_.picture.data.url.asOpt[String])).orUndefined ??
      UNKNOWN_PERSON

    // set the class
    scope.myClass = if (scope.url.contains(UNKNOWN_PERSON)) "spectatorAvatar" else "playerAvatar"
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
@js.native
trait AvatarDirectiveScope extends Scope {
  // input fields
  var id: js.UndefOr[String] = js.native
  var link: js.UndefOr[String] = js.native
  var `class`: js.UndefOr[String] = js.native
  var style: js.UndefOr[String] = js.native

  // output fields
  var myClass: js.UndefOr[String] = js.native
  var url: js.UndefOr[String] = js.native

}

/**
  * Avatar Directive Scope Singleton
  * @author lawrence.daniels@gmail.com
  */
object AvatarDirectiveScope {

  def apply(id: String, link: String, `class`: String, style: String): AvatarDirectiveScope = {
    val scope = new js.Object().asInstanceOf[AvatarDirectiveScope]
    scope.id = id
    scope.link = link
    scope.`class` = `class`
    scope.style = style
    scope
  }

}