package com.shocktrade.client.users

import com.shocktrade.client.models.UserProfile
import com.shocktrade.common.forms.SignUpForm
import io.scalajs.npm.angularjs.Service
import io.scalajs.npm.angularjs.http.{Http, HttpResponse}

import scala.scalajs.js

/**
 * User Account Service
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class UserAccountService($http: Http) extends Service {

  def createAccount(form: SignUpForm): js.Promise[HttpResponse[UserProfile]] = {
    $http.post[UserProfile]("/api/user", form)
  }

}
