package com.shocktrade.javascript.discover

import com.greencatsoft.angularjs.core.HttpService
import com.greencatsoft.angularjs.{Factory, Service, injectable}

/**
 * Held Securities Service
 * @author lawrence.daniels@gmail.com
 */
@injectable("HeldSecurities")
class HeldSecurities(http: HttpService) extends Service {

  def isHeld(symbol: String) = true

}

@injectable("HeldSecurities")
class HeldSecuritiesFactory(http: HttpService) extends Factory[HeldSecurities] {

  override def apply(): HeldSecurities = new HeldSecurities(http)

}