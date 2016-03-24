package com.shocktrade.javascript.social

import com.github.ldaniels528.scalascript.angular
import com.github.ldaniels528.scalascript.util.ScalaJsHelper._
import org.scalajs.dom.console
import org.scalajs.jquery._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g, literal => JS}
import scala.util.{Failure, Success}

/**
 * Facebook Injector
 * @author lawrence.daniels@gmail.com
 */
object FacebookInjector {
  private val elemName = "#ShockTradeMain"
  private lazy val FB = Facebook.SDK

  /**
   * Returns the Facebook application ID based on the running host
   * @return {*}
   */
  def getShockTradeAppID: js.Function0[String] = () => {
    console.log(s"Facebook - hostname: ${g.location.hostname}")
    g.location.hostname.as[String] match {
      case "localhost" => "522523074535098" // local dev
      case "www.shocktrade.biz" => "616941558381179"
      case "shocktrade.biz" => "616941558381179"
      case "www.shocktrade.com" => "364507947024983"
      case "shocktrade.com" => "364507947024983"
      case "www.shocktrade.net" => "616569495084446"
      case "shocktrade.net" => "616569495084446"
      case _ =>
        console.log(s"Unrecognized hostname '${g.location.hostname}'")
        "522523074535098" // unknown, so local dev
    }
  }

  /**
   * Initializes the Facebook SDK
   */
  g.fbAsyncInit = (() => {
    val appId = getShockTradeAppID()
    console.log(s"Initializing Facebook SDK (App ID $appId)...")
    FB.init(JS(
      appId = appId,
      status = true,
      xfbml = true
    ))

    // capture the user ID and access token
    val rootElem = jQuery(elemName)
    val injector = angular.element(rootElem).injector()
    injector.get[Facebook]("Facebook") foreach { facebook =>
      facebook.appID = getShockTradeAppID()
      facebook.getLoginStatus onComplete {
        case Success(response) =>
          console.log("Facebook login successful.")

          // react the the login status
          val $scope = angular.element(rootElem).scope()
          if ($scope != null) {
            $scope.dynamic.postLoginUpdates(facebook.facebookID, false)
          }
          else {
            console.log(s"Scope for '$elemName' could not be retrieved")
          }
        case Failure(e) =>
          console.log(s"Facebook Service: ${e.getMessage}")
      }
    }
    ()
  }): js.Function0[Unit]

  /**
   * Injects the Facebook SDK
   * @param fbroot the Facebook root element
   */
  private def inject(fbroot: js.Dynamic) {
    // is the element our script?
    val id = "facebook-jssdk"
    if (!isDefined(fbroot.getElementById(id))) {
      // dynamically create the script
      val fbScript = fbroot.createElement("script")
      fbScript.id = id
      fbScript.async = true
      fbScript.src = "http://connect.facebook.net/en_US/all.js"

      // get the script and insert our dynamic script
      val ref = fbroot.getElementsByTagName("script").asArray[js.Dynamic](0)
      ref.parentNode.insertBefore(fbScript, ref)
    }
    ()
  }

  /**
   * Inject the Facebook SDK
   */
  def init: js.Function0[Unit] = () => inject(g.document)

}
