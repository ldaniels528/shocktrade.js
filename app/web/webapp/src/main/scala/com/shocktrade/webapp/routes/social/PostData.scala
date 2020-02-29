package com.shocktrade.webapp.routes.social

import com.shocktrade.common.models.post._

import scala.scalajs.js

/**
  * Represents a Post data object
  * @author lawrence.daniels@gmail.com
  */
class PostData(var postID: js.UndefOr[String] = js.undefined,
               var text: js.UndefOr[String] = js.undefined,
               var submitterId: js.UndefOr[String] = js.undefined,
               var summary: js.UndefOr[SharedContent] = js.undefined,
               var likes: js.UndefOr[Int] = js.undefined,
               var likedBy: js.UndefOr[js.Array[String]] = js.undefined,
               var creationTime: js.Date = new js.Date(),
               var lastUpdateTime: js.Date = new js.Date()) extends js.Object

