package com.shocktrade.client.auth

import com.shocktrade.common.auth.{AuthenticationCode, AuthenticationForm, AuthenticationResponse}
import io.scalajs.npm.angularjs.Service
import io.scalajs.npm.angularjs.http.{Http, HttpResponse}

import scala.scalajs.js

/**
 * Authentication Service
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class AuthenticationService($http: Http) extends Service {

  def getCode: js.Promise[HttpResponse[AuthenticationCode]] =
    $http.get[AuthenticationCode](url = "/api/auth/code")

  def login(form: AuthenticationForm): js.Promise[HttpResponse[AuthenticationResponse]] =
    $http.post[AuthenticationResponse](url = "/api/auth/login", data = form)

  def logout(): js.Promise[HttpResponse[AuthenticationResponse]] =
    $http.get[AuthenticationResponse](url = "/api/auth/logout")

}