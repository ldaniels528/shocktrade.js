package com.shocktrade.controllers

import play.api.mvc._

/**
 * Secured Resource: JSON Web Token Implementation
 * @see http://stackoverflow.com/questions/22154977/token-based-authentication-using-play-2-framework
 */
trait SecuredResource extends Controller {

  /*
  def Secured[A](username: String, password: String)(action: Action[A]) = Action(action.parser) { implicit request =>
    request.headers.get("Authorization").flatMap { authorization =>
      authorization.split(" ").drop(1).headOption.filter { encoded =>
        new String(org.apache.commons.codec.binary.Base64.decodeBase64(encoded.getBytes)).split(":").toList match {
          case u :: p :: Nil if u == username && password == p => true
          case _ => false
        }
      }.map(_ => action(request))
    }.getOrElse {
      //Unauthorized.withHeaders("WWW-Authenticate" -> """Basic realm="Secured"""")
      BadRequest("Security Exception")
    }
  }*/

}