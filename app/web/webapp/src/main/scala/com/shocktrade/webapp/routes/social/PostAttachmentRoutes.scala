package com.shocktrade.webapp.routes.social

import com.shocktrade.webapp.routes.NextFunction
import io.scalajs.npm.express.fileupload.UploadedFiles
import io.scalajs.npm.express.{Application, Request, Response}

import scala.concurrent.ExecutionContext

/**
 * Post Attachment Routes
 * @author lawrence.daniels@gmail.com
 */
class PostAttachmentRoutes(app: Application)(implicit ec: ExecutionContext) {

  // Post Attachments
  app.get("/api/posts/attachments/:attachmentID", (request: Request, response: Response, next: NextFunction) => downloadAttachment(request, response, next))
  app.get("/api/posts/attachments/user/:userID", (request: Request, response: Response, next: NextFunction) => getAttachementIDs(request, response, next))
  app.post("/api/post/:postID/attachment/:userID", (request: Request with UploadedFiles, response: Response, next: NextFunction) => uploadAttachment(request, response, next))

  /**
   * Downloads a post attachment by ID
   * @example GET /api/posts/attachments/56fd562b9a421db70c9172c1
   */
  def downloadAttachment(request: Request, response: Response, next: NextFunction): Unit = {
    val attachmentID = request.params("attachmentID")
    /*
    attachmentDAO map (_.openDownloadStream(attachmentID.$oid).pipe(response)) onComplete {
      case Success(downloadStream) => downloadStream.onEnd(() => next())
      case Failure(e) => response.internalServerError(e); next()
    }*/
    response.internalServerError("Not yet implemented")
  }

  /**
   * Retrieves all attachment IDs for a given user
   * @example GET /api/posts/attachments/user/5633c756d9d5baa77a714803
   */
  def getAttachementIDs(request: Request, response: Response, next: NextFunction): Unit = {
    val userID = request.params("userID")
    /*
    attachmentDAO.flatMap(_.find[Attachment]("metadata.userID" $eq userID.$oid).toArray()) onComplete {
      case Success(attachments) => response.send(attachments); next()
      case Failure(e) => response.internalServerError(e); next()
    }*/
    response.internalServerError("Not yet implemented")
  }

  /**
   * Uploads an attachment for the given post and user
   * @example POST /api/post/563cff811b591f4c7870aaa1/attachment/5633c756d9d5baa77a714803
   */
  def uploadAttachment(request: Request with UploadedFiles, response: Response, next: NextFunction): Unit = {
    val (postID, userID) = (request.params("postID"), request.params("userID"))
    request.files.values foreach { file =>
      /*
      val outcome = for {
        (attachmentId, success) <- attachmentDAO map { fs =>
          val ustream = fs.openUploadStream(file.name, new UploadStreamOptions(metadata = doc("userID" -> userID.$oid, "postID" -> postID.$oid)))
          val id = new String()
          (id, ustream.end(file.data))
        }
        result <- postDAO.flatMap(_.findOneAndUpdate("_id" $eq postID.$oid, "attachments" $addToSet attachmentId.toHexString()))
      } yield result

      outcome onComplete {
        case Success(result) => response.send(result.value); next()
        case Failure(e) => response.internalServerError(e); next()
      }*/
    }
    response.internalServerError("Not yet implemented")
  }

}
