package com.shocktrade.client.users

import com.shocktrade.client.models.UserProfile
import com.shocktrade.common.auth.{AuthenticationCode, AuthenticationForm, AuthenticationResponse}
import io.scalajs.npm.angularjs.Service
import io.scalajs.npm.angularjs.http.{Http, HttpResponse}

import scala.scalajs.js

/**
 * Authentication Service
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class AuthenticationService($http: Http) extends Service {

  def getCode: js.Promise[HttpResponse[AuthenticationCode]] = $http.get(url = "/api/auth/code")

  def login(form: AuthenticationForm): js.Promise[HttpResponse[UserProfile]] = $http.post(url = "/api/auth/login", data = form)

  def logout(): js.Promise[HttpResponse[AuthenticationResponse]] = $http.get(url = "/api/auth/logout")

}