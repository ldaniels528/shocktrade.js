package com.shocktrade.server.dao.reference

import com.shocktrade.common.models.post._
import io.scalajs.npm.mongodb.ObjectID
import io.scalajs.util.JsUnderOrHelper._

import scala.scalajs.js

/**
  * Represents a Post data object
  * @author lawrence.daniels@gmail.com
  */
class PostData(var _id: js.UndefOr[ObjectID] = js.undefined,
               var text: js.UndefOr[String] = js.undefined,
               var submitterId: js.UndefOr[String] = js.undefined,
               var summary: js.UndefOr[SharedContent] = js.undefined,
               var likes: js.UndefOr[Int] = js.undefined,
               var likedBy: js.UndefOr[js.Array[String]] = js.undefined,
               var creationTime: js.Date = new js.Date(),
               var lastUpdateTime: js.Date = new js.Date(),

               // collections
               var attachments: js.UndefOr[js.Array[String]] = js.undefined,
               var comments: js.UndefOr[js.Array[Comment]] = js.undefined,
               var replyLikes: js.UndefOr[js.Array[ReplyLikes]] = js.undefined,
               var tags: js.UndefOr[js.Array[String]] = js.undefined) extends PostLike

/**
  * Post Data Companion
  * @author lawrence.daniels@gmail.com
  */
object PostData {

  /**
    * Post Data Extensions
    * @author lawrence.daniels@gmail.com
    */
  implicit class PostDataExtensions(val data: PostData) extends AnyVal {

    def toModel = new Post(
      _id = data._id.map(_.toHexString()),
      text = data.text,
      submitterId = data.submitterId,
      summary = data.summary,
      likes = data.likes,
      likedBy = data.likedBy,
      creationTime = data.creationTime,
      lastUpdateTime = data.lastUpdateTime,

      // collections
      attachments = data.attachments,
      comments = data.comments,
      replyLikes = data.replyLikes,
      tags = data.tags
    )
  }

  /**
    * Post Extensions
    * @author lawrence.daniels@gmail.com
    */
  implicit class PostExtensions(val post: Post) extends AnyVal {

    def toData = new PostData(
      _id = post._id.map(new ObjectID(_)) ?? new ObjectID(),
      text = post.text,
      submitterId = post.submitterId,
      summary = post.summary,
      likes = post.likes,
      likedBy = post.likedBy,
      creationTime = post.creationTime,
      lastUpdateTime = post.lastUpdateTime,

      // collections
      attachments = post.attachments,
      comments = post.comments,
      replyLikes = post.replyLikes,
      tags = post.tags
    )

  }

}
