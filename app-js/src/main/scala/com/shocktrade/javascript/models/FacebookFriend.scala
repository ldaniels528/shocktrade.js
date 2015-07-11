package com.shocktrade.javascript.models

import scala.scalajs.js

/**
 * Facebook Friend
 */
trait FacebookFriend extends js.Object {
  var id: String = js.native
  var name: String = js.native
  var picture: FacebookPicture = js.native
}

/**
 * Facebook Friend Picture
 */
trait FacebookPicture extends js.Object  {
  var data: FacebookPictureData = js.native

}

/**
 * Facebook Friend Picture Data
 */
trait FacebookPictureData extends js.Object  {
  var is_silhouette: Boolean = js.native
  var url: String = js.native

}
