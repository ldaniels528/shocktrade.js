package com.shocktrade.javascript.profile

import com.github.ldaniels528.scalascript._
import com.github.ldaniels528.scalascript.core.Http
import com.shocktrade.javascript.profile.AwardsController._
import com.shocktrade.javascript.{Award, MySessionService}

import scala.language.postfixOps
import scala.scalajs.js

/**
  * Awards Controller
  * @author lawrence.daniels@gmail.com
  */
class AwardsController($scope: Scope, $http: Http, @injected("MySessionService") mySession: MySessionService)
  extends Controller {

  ///////////////////////////////////////////////////////////////////////////
  //          Public Functions
  ///////////////////////////////////////////////////////////////////////////

  @scoped def getAwards = Award.AvailableAwards map { award =>
    val myAward = award.asInstanceOf[MyAward]
    myAward.owned = mySession.getMyAwards.contains(award.code)
    myAward
  } sortBy (_.owned) reverse

  @scoped def getAwardImage(code: String) = AwardIconsByCode.get(code).orNull

  @scoped def getMyAwards = mySession.getMyAwards map (code => AwardsByCode.get(code).orNull)

}

/**
  * Award with owned information
  */
@js.native
trait MyAward extends Award {
  var owned: Boolean = js.native
}

/**
  * Awards Controller Singleton
  */
object AwardsController {

  private val AwardsByCode = js.Dictionary[Award](Award.AvailableAwards map { award => (award.code, award) }: _*)

  private val AwardIconsByCode = js.Dictionary[String](Award.AvailableAwards map { award => (award.code, award.icon) }: _*)

}