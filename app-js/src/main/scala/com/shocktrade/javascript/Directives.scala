package com.shocktrade.javascript

import biz.enef.angulate.Directive
import biz.enef.angulate.core.{Attributes, JQLite}
import com.shocktrade.javascript.ScalaJsHelper._

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g}
import scala.scalajs.js.JSON

/**
 * ShockTrade Directives
 * @author lawrence.daniels@gmail.com
 */
object Directives {

  /**
   * Avatar Directive
   * @author lawrence.daniels@gmail.com
   * <avatar id="{{ p.facebookID }}" class="avatar-24"></avatar>
   */
  class Avatar() extends Directive {

    override def restrict = "E"

    override def transclude = true

    override def replace = false

    override def template = """<img ng-src="{{ url }}" ng-class="myClass" class="{{ class }}" style="{{ style }}">"""

    override def postLink(scope: ScopeType, element: JQLite, attrs: Attributes): Unit = {

      def setURL(scope: js.Dynamic) {
        if (isDefined(scope.id)) {
          scope.url = s"http://graph.facebook.com/${scope.id}/picture"
          scope.myClass = "playerAvatar"
        }
        else if (isDefined(scope.`object`)) {
          val json = JSON.parse(scope.`object`.as[String])
          if (!isDefined(json)) scope.url = if (isDefined(scope.alt)) scope.alt.as[String] else "/assets/images/avatars/avatar100.png"
          else if (isDefined(json.picture)) scope.url = json.picture.data.url
          else if (isDefined(json.facebookID)) scope.url = s"http://graph.facebook.com/${json.facebookID}/picture"
          else {
            g.console.error(s"Avatar object type could not be determined - ${scope.`object`}")
            scope.url = if (isDefined(scope.alt)) scope.alt.as[String] else "/assets/images/avatars/avatar100.png"
          }
          scope.myClass = "playerAvatar"
        }
        else {
          scope.url = if (isDefined(scope.alt)) scope.alt.as[String] else "/assets/images/avatars/avatar100.png"
          scope.myClass = "spectatorAvatar"
        }
      }

      val myScope = scope.asInstanceOf[js.Dynamic]
      myScope.$watch("object", () => setURL(myScope))
      myScope.$watch("id", () => setURL(myScope))
    }
  }

}
