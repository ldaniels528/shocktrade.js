package com.shocktrade.webapp.routes
package social

import com.shocktrade.common.Ok
import com.shocktrade.common.forms.MaxResultsForm
import com.shocktrade.common.models.post.Post
import com.shocktrade.webapp.routes.social.PostRoutes._
import io.scalajs.nodejs.console
import io.scalajs.npm.express.{Application, Request, Response}
import io.scalajs.util.JsUnderOrHelper._

import scala.concurrent.ExecutionContext
import scala.scalajs.js
import scala.scalajs.js.JSON
import scala.util.{Failure, Success}

/**
 * Post Routes
 * @author lawrence.daniels@gmail.com
 */
class PostRoutes(app: Application)(implicit ec: ExecutionContext) {
  private val postDAO = PostDAO()

  // Post CRUD
  app.post("/api/post", (request: Request, response: Response, next: NextFunction) => createPost(request, response, next))
  app.put("/api/post", (request: Request, response: Response, next: NextFunction) => updatePost(request, response, next))
  app.get("/api/post/:postID", (request: Request, response: Response, next: NextFunction) => postByID(request, response, next))
  app.delete("/api/post/:postID", (request: Request, response: Response, next: NextFunction) => deletePost(request, response, next))

  // Post Bulk-reads
  app.get("/api/posts", (request: Request, response: Response, next: NextFunction) => listPosts(request, response, next))
  app.get("/api/posts/user/:userID", (request: Request, response: Response, next: NextFunction) => postsByOwner(request, response, next))
  app.get("/api/posts/user/:userID/newsfeed", (request: Request, response: Response, next: NextFunction) => newsFeed(request, response, next))

  // Post Like/Unlike
  app.put("/api/post/:postID/like/:userID", (request: Request, response: Response, next: NextFunction) => like(request, response, next))
  app.delete("/api/post/:postID/like/:userID", (request: Request, response: Response, next: NextFunction) => unlike(request, response, next))

  /**
   * Creates a new post
   * @example POST /api/post
   */
  def createPost(request: Request, response: Response, next: NextFunction): Unit = {
    val post = request.bodyAs[Post].toData
    post.creationTime = new js.Date()
    post.lastUpdateTime = new js.Date()
    postDAO.insertOne(post) onComplete {
      case Success(count) if count == 1 => response.send(new Ok(count)); next()
      case Success(_) => response.badRequest("Post could not be created"); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  /**
   * Deletes a post by ID
   * @example DELETE /api/post/56fd562b9a421db70c9172c1
   */
  def deletePost(request: Request, response: Response, next: NextFunction): Unit = {
    val postID = request.params("postID")
    postDAO.deleteOne(postID) onComplete {
      case Success(count) if count == 1 => response.send(new Ok(count)); next()
      case Success(_) => response.notFound(postID); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  /**
   * Retrieve a post by ID
   * @example GET /api/post/5633c756d9d5baa77a714803
   */
  def postByID(request: Request, response: Response, next: NextFunction): Unit = {
    val postID = request.params("postID")
    postDAO.findOneByID(postID) onComplete {
      case Success(Some(post)) => response.send(post); next()
      case Success(None) => response.notFound(); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  /**
   * Retrieve all posts
   * @example GET /api/posts
   */
  def listPosts(request: Request, response: Response, next: NextFunction): Unit = {
    val maxResults = request.queryAs[MaxResultsForm].getMaxResults()
    val outcome = for {
      posts <- postDAO.findAll(limit = maxResults)
      tags <- postDAO.findTags(postIDs = posts.flatMap(_.postID.toOption))
    } yield (posts, tags)

    outcome onComplete {
      case Success((posts, tags)) =>
        val models = posts.map(_.toModel(tags))
        console.log(s"models = ${JSON.stringify(models)}")
        response.send(models);
        next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  /**
   * Retrieve all posts for a given user
   * @example /api/posts/user/56340a6f3c21a4b485d47c55
   */
  def postsByOwner(request: Request, response: Response, next: NextFunction): Unit = {
    val userID = request.params("userID")
    val maxResults = request.queryAs[MaxResultsForm].getMaxResults()
    postDAO.findByUser(userID, limit = maxResults) onComplete {
      case Success(posts) => response.send(posts); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  /**
   * Likes an post by user ID
   * @example PUT /api/post/564e4b60e7aabce5a152b10b/like/5633c756d9d5baa77a714803
   */
  def like(request: Request, response: Response, next: NextFunction): Unit = {
    val (postID, userID) = (request.params("postID"), request.params("userID"))
    postDAO.like(postID, userID) onComplete {
      case Success(Some(likes)) if likes > 0 => response.send(new Ok(likes)); next()
      case Success(None) => response.notFound(postID); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  /**
   * Unlikes an post by user ID
   * @example DELETE /api/post/564e4b60e7aabce5a152b10b/like/5633c756d9d5baa77a714803
   */
  def unlike(request: Request, response: Response, next: NextFunction): Unit = {
    val (postID, userID) = (request.params("postID"), request.params("userID"))
    postDAO.unlike(postID, userID) onComplete {
      case Success(Some(likes)) if likes > 0 => response.send(new Ok(likes)); next()
      case Success(None) => response.notFound(postID); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  /**
   * Retrieve the news feed posts by user ID
   * @example GET /api/posts/user/5633c756d9d5baa77a714803/newsfeed
   */
  def newsFeed(request: Request, response: Response, next: NextFunction): Unit = {
    val (userID, maxResults) = (request.params("userID"), request.queryAs[MaxResultsForm].getMaxResults())
    postDAO.findNewsFeed(userID, limit = maxResults) onComplete {
      case Success(posts) => response.send(posts); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  /**
   * Updates an existing post
   * @example PUT /api/post
   */
  def updatePost(request: Request, response: Response, next: NextFunction): Unit = {
    val form = request.bodyAs[Post]
    form.postID.flat.toOption match {
      case Some(_id) =>
        val post = form.toData
        postDAO.updateOne(post) onComplete {
          case Success(count) if count == 1 => response.send(post.toModel()); next()
          case Success(_) => response.notFound(_id); next()
          case Failure(e) => response.internalServerError(e); next()
        }
      case None => response.badRequest("Post has no _id field"); next()
    }
  }

}

/**
 * Post Routes
 * @author lawrence.daniels@gmail.com
 */
object PostRoutes {

  /**
   * Shared Content Form
   */
  @js.native
  trait SharedContentForm extends js.Object {
    var url: js.UndefOr[String] = js.native
  }

  /**
   * Post Data Extensions
   * @author lawrence.daniels@gmail.com
   */
  implicit class PostDataExtensions(val data: PostData) extends AnyVal {

    def toModel(tagData: js.Array[PostTagData] = js.Array()) = new Post(
      postID = data.postID,
      text = data.text,
      userID = data.submitterId,
      summary = data.summary,
      likes = data.likes,
      likedBy = data.likedBy,
      creationTime = data.creationTime,
      lastUpdateTime = data.lastUpdateTime,

      // collections
      //attachments = data.attachments,
      //comments = data.comments,
      //replyLikes = data.replyLikes,
      tags = tagData.collect { case tag if tag.postID == data.postID => tag.hashTag.orNull }
    )
  }

  /**
   * Post Extensions
   * @author lawrence.daniels@gmail.com
   */
  implicit class PostExtensions(val post: Post) extends AnyVal {

    def toData = new PostData(
      postID = post.postID,
      text = post.text,
      submitterId = post.userID,
      summary = post.summary,
      likes = post.likes,
      likedBy = post.likedBy,
      creationTime = post.creationTime,
      lastUpdateTime = post.lastUpdateTime,

      // collections
      //attachments = post.attachments,
      //comments = post.comments,
      //replyLikes = post.replyLikes,
      //tags = post.tags collect { }
    )

  }

}
