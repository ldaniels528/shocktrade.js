package com.shocktrade.webapp.routes

import com.shocktrade.common.forms.MaxResultsForm
import com.shocktrade.common.models.OperationResult
import com.shocktrade.common.models.post.{Attachment, Post}
import com.shocktrade.server.dao._
import com.shocktrade.server.dao.reference.PostAttachmentDAO._
import com.shocktrade.server.dao.reference.PostDAO._
import com.shocktrade.server.dao.reference.PostData._
import com.shocktrade.server.dao.reference.{PostAttachmentDAO, PostDAO}
import com.shocktrade.server.dao.users.ProfileDAO._
import com.shocktrade.server.dao.users.{ProfileDAO, UserDAO, UserProfileData}
import com.shocktrade.webapp.routes.PostRoutes._
import com.shocktrade.webapp.{SharedContentParser, SharedContentProcessor}
import io.scalajs.npm.express.fileupload.UploadedFiles
import io.scalajs.npm.express.{Application, Request, Response}
import io.scalajs.npm.mongodb._
import io.scalajs.npm.mongodb.gridfs.UploadStreamOptions
import io.scalajs.util.JsUnderOrHelper._
import io.scalajs.util.PromiseHelper.Implicits._

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
  * Post Routes
  * @author lawrence.daniels@gmail.com
  */
class PostRoutes(app: Application, dbFuture: Future[Db])(implicit ec: ExecutionContext) {
    implicit val postDAO: Future[PostDAO] = dbFuture.map(_.getPostDAO)
    implicit val profileDAO: Future[ProfileDAO] = dbFuture.map(_.getProfileDAO)
    implicit val userDAO: UserDAO = UserDAO()
    implicit val attachmentDAO: Future[PostAttachmentDAO] = dbFuture.map(_.getPostAttachmentDAO)
    implicit val seoMetaParser: SharedContentParser = new SharedContentParser()

    // Post CRUD
    app.post("/api/post", (request: Request, response: Response, next: NextFunction) => createPost(request, response, next))
    app.put("/api/post", (request: Request, response: Response, next: NextFunction) => updatePost(request, response, next))
    app.get("/api/post/:postID", (request: Request, response: Response, next: NextFunction) => getPost(request, response, next))
    app.delete("/api/post/:postID", (request: Request, response: Response, next: NextFunction) => deletePost(request, response, next))

    // Post Bulk-reads
    app.get("/api/posts", (request: Request, response: Response, next: NextFunction) => getPosts(request, response, next))
    app.get("/api/posts/user/:ownerID", (request: Request, response: Response, next: NextFunction) => getPostsByOwner(request, response, next))
    app.get("/api/posts/user/:ownerID/newsfeed", (request: Request, response: Response, next: NextFunction) => getNewsFeed(request, response, next))

    // Post Attachments
    app.get("/api/posts/attachments/:attachmentID", (request: Request, response: Response, next: NextFunction) => downloadAttachment(request, response, next))
    app.get("/api/posts/attachments/user/:userID", (request: Request, response: Response, next: NextFunction) => getAttachementIDs(request, response, next))
    app.post("/api/post/:postID/attachment/:userID", (request: Request with UploadedFiles, response: Response, next: NextFunction) => uploadAttachment(request, response, next))

    // Post Like/Unlike
    app.put("/api/post/:postID/like/:userID", (request: Request, response: Response, next: NextFunction) => likePost(request, response, next))
    app.delete("/api/post/:postID/like/:userID", (request: Request, response: Response, next: NextFunction) => unlike(request, response, next))

    // Social Content / SEO
    app.get("/api/social/content", (request: Request, response: Response, next: NextFunction) => getSEOContent(request, response, next))

    /**
      * Creates a new post
      * @example POST /api/post
      */
    def createPost(request: Request, response: Response, next: NextFunction): Unit = {
      val post = request.bodyAs[Post].toData
      post.creationTime = new js.Date()
      post.lastUpdateTime = new js.Date()
      postDAO.flatMap(_.insertOne(post)) onComplete {
        case Success(result) if result.insertedCount == 1 => response.send(result.ops.headOption.orNull); next()
        case Success(result) => response.badRequest("Post could not be created"); next()
        case Failure(e) => response.internalServerError(e); next()
      }
    }

    /**
      * Deletes a post by ID
      * @example DELETE /api/post/56fd562b9a421db70c9172c1
      */
    def deletePost(request: Request, response: Response, next: NextFunction): Unit = {
      val postID = request.params.apply("postID")
      postDAO.flatMap(_.deleteOne("_id" $eq postID.$oid)) onComplete {
        case Success(outcome) => response.send(new OperationResult(success = outcome.result.isOk)); next()
        case Failure(e) => response.internalServerError(e); next()
      }
    }

    /**
      * Downloads a post attachment by ID
      * @example GET /api/posts/attachments/56fd562b9a421db70c9172c1
      */
    def downloadAttachment(request: Request, response: Response, next: NextFunction): Unit = {
      val attachmentID = request.params.apply("attachmentID")
      attachmentDAO map (_.openDownloadStream(attachmentID.$oid).pipe(response)) onComplete {
        case Success(downloadStream) => downloadStream.onEnd(() => next())
        case Failure(e) => response.internalServerError(e); next()
      }
    }

    /**
      * Retrieves all attachment IDs for a given user
      * @example GET /api/posts/attachments/user/5633c756d9d5baa77a714803
      */
    def getAttachementIDs(request: Request, response: Response, next: NextFunction): Unit = {
      val userID = request.params.apply("userID")
      attachmentDAO.flatMap(_.find[Attachment]("metadata.userID" $eq userID.$oid).toArray()) onComplete {
        case Success(attachments) => response.send(attachments); next()
        case Failure(e) => response.internalServerError(e); next()
      }
    }

    /**
      * Uploads an attachment for the given post and user
      * @example POST /api/post/563cff811b591f4c7870aaa1/attachment/5633c756d9d5baa77a714803
      */
    def uploadAttachment(request: Request with UploadedFiles, response: Response, next: NextFunction): Unit = {
      val (postID, userID) = (request.params.apply("postID"), request.params.apply("userID"))
      request.files.values foreach { file =>
        val outcome = for {
          (attachmentId, success) <- attachmentDAO map { fs =>
            val ustream = fs.openUploadStream(file.name, new UploadStreamOptions(metadata = doc("userID" -> userID.$oid, "postID" -> postID.$oid)))
            val id = new ObjectID()
            (id, ustream.end(file.data))
          }
          result <- postDAO.flatMap(_.findOneAndUpdate("_id" $eq postID.$oid, "attachments" $addToSet attachmentId.toHexString()))
        } yield result

        outcome onComplete {
          case Success(result) => response.send(result.value); next()
          case Failure(e) => response.internalServerError(e); next()
        }
      }
    }

    /**
      * Retrieve a post by ID
      * @example GET /api/post/5633c756d9d5baa77a714803
      */
    def getPost(request: Request, response: Response, next: NextFunction): Unit = {
      val postID = request.params.apply("postID")
      postDAO.flatMap(_.findById[Post](postID)) onComplete {
        case Success(Some(post)) => response.send(post); next()
        case Success(None) => response.notFound(); next()
        case Failure(e) => response.internalServerError(e); next()
      }
    }

    /**
      * Retrieve all posts
      * @example GET /api/posts
      */
    def getPosts(request: Request, response: Response, next: NextFunction): Unit = {
      val maxResults = request.queryAs[MaxResultsForm].getMaxResults()
      postDAO.flatMap(_.find[Post]().limit(maxResults).toArray()) onComplete {
        case Success(posts) => response.send(posts); next()
        case Failure(e) => response.internalServerError(e); next()
      }
    }

    /**
      * Retrieve all posts for a given user
      * @example /api/posts/user/56340a6f3c21a4b485d47c55
      */
    def getPostsByOwner(request: Request, response: Response, next: NextFunction): Unit = {
      val ownerID = request.params.apply("ownerID")
      val maxResults = request.queryAs[MaxResultsForm].getMaxResults()
      postDAO.flatMap(_.find[Post]("submitterId" $eq ownerID).limit(maxResults).toArray()) onComplete {
        case Success(posts) => response.send(posts); next()
        case Failure(e) => response.internalServerError(e); next()
      }
    }

    /**
      * Retrieve the news feed posts by user ID
      * @example GET /api/posts/user/5633c756d9d5baa77a714803/newsfeed
      */
    def getNewsFeed(request: Request, response: Response, next: NextFunction): Unit = {
      val ownerID = request.params.apply("ownerID")
      val maxResults = request.queryAs[MaxResultsForm].getMaxResults()
      val outcome = for {
        submitters <- profileDAO.flatMap(_.findById[UserProfileData](ownerID, js.Array("followers"))).map(_.flatMap(_.followers.toOption).getOrElse(js.Array()))
        _ = submitters.push(ownerID)
        posts <- postDAO.flatMap(_.find[Post]("submitterId" $in submitters).limit(maxResults).toArray())
      } yield posts

      outcome onComplete {
        case Success(posts) => response.send(posts); next()
        case Failure(e) => response.internalServerError(e); next()
      }
    }

    /**
      * Retrieves the SEO information for embedding within a post
      * @example /api/social/content?url=http://www.businessinsider.com/how-to-remember-everything-you-learn-2015-10
      */
    def getSEOContent(request: Request, response: Response, next: NextFunction): Unit = {
      val form = request.queryAs[SharedContentForm]
      form.url.toOption match {
        case Some(url) =>
          seoMetaParser.parse(url) onComplete {
            case Success(result) =>
              SharedContentProcessor.parseMetaData(result) match {
                case Some(content) => response.send(content.toJson)
                case None => response.send(doc())
              }
              next()
            case Failure(e) => response.internalServerError(e); next()
          }
        case None => response.missingParams("url"); next()
      }
    }

    /**
      * Likes an post by user ID
      * @example PUT /api/post/564e4b60e7aabce5a152b10b/like/5633c756d9d5baa77a714803
      */
    def likePost(request: Request, response: Response, next: NextFunction): Unit = {
      val (postID, userID) = (request.params.apply("postID"), request.params.apply("userID"))
      postDAO.flatMap(_.like(postID, userID)) onComplete {
        case Success(outcome) =>
          outcome.ok match {
            case 1 => response.send(outcome.value); next()
            case _ => response.notFound(postID); next()
          }
        case Failure(e) =>
          response.internalServerError(e); next()
      }
    }

    /**
      * Unlikes an post by user ID
      * @example DELETE /api/post/564e4b60e7aabce5a152b10b/like/5633c756d9d5baa77a714803
      */
    def unlike(request: Request, response: Response, next: NextFunction): Unit = {
      val (postID, userID) = (request.params.apply("postID"), request.params.apply("userID"))
      postDAO.flatMap(_.unlike(postID, userID)) onComplete {
        case Success(outcome) =>
          outcome.ok match {
            case 1 => response.send(outcome.value); next()
            case _ => response.notFound(postID); next()
          }
        case Failure(e) => response.internalServerError(e); next()
      }
    }

    /**
      * Updates an existing post
      * @example PUT /api/post
      */
    def updatePost(request: Request, response: Response, next: NextFunction): Unit = {
      val form = request.bodyAs[Post]
      form._id.flat.toOption match {
        case Some(_id) =>
          val post = form.toData
          postDAO.flatMap(_.updateOne(filter = "_id" $eq _id.$oid, update = post, new UpdateOptions(upsert = false))) onComplete {
            case Success(outcome) if outcome.modifiedCount == 1 => response.send(post.toModel); next()
            case Success(outcome) => response.notFound(_id); next()
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

}

