package com.shocktrade.javascript.profile

import com.greencatsoft.angularjs.injectable
import com.shocktrade.javascript.app.model.UserProfile

import scala.concurrent.Future

/**
 * Profile Service
 * @author lawrence.daniels@gmail.com
 */
@injectable("ProfileService")
class ProfileService {

  def getProfileByFacebookID(fbId: String): Future[UserProfile] = ???

}
