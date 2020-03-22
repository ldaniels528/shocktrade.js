package com.shocktrade.client.posts

import com.shocktrade.client.models.UserProfile
import com.shocktrade.client.users.UserService
import com.shocktrade.client.{GlobalLoading, RootScope}
import com.shocktrade.common.models.post.{Comment, Post, Reply}
import io.scalajs.JSON
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.AngularJsHelper._
import io.scalajs.npm.angularjs._
import io.scalajs.npm.angularjs.fileupload.nervgh.{FileItem, FileUploader, FileUploaderConfig}
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.util.DurationHelper._
import io.scalajs.util.JsUnderOrHelper._
import io.scalajs.util.PromiseHelper.Implicits._
import io.scalajs.util.ScalaJsHelper._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * Posting Capabilities
 * @author lawrence.daniels@gmail.com
 */
trait PostingCapabilities extends GlobalLoading {
  self: Controller =>

  // define the last post containing a file upload
  private var lastUploadedPost: Option[Post] = None

  def $scope: PostingCapabilitiesScope

  def fileUploader: FileUploader

  def postService: PostService

  def $timeout: Timeout

  def toaster: Toaster

  def userService: UserService

  $scope.posts = emptyArray
  $scope.tags = emptyArray

  // initialize the file uploader
  $scope.uploader = FileUploader(fileUploader, new FileUploaderConfig(url = "/api/post/@postID/attachment/@userID"))

  ///////////////////////////////////////////////////////////////////////////
  //      Post Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.deletePost = (aPost: js.UndefOr[Post]) => {
    for {
      userID <- $scope.userProfile.flatMap(_.userID)
      post <- aPost
      postID <- post.postID
    } {
      if ($scope.isDeletable(aPost) ?== true) {
        post.deleteLoading = true
        postService.deletePost(postID) onComplete {
          case Success(response) =>
            val result = response.data
            $scope.$apply(() => post.deleteLoading = false)
            if (result.success && removePostFromList(post)) {
              toaster.success("Post deleted")
            }
          case Failure(e) =>
            $scope.$apply(() => post.deleteLoading = false)
            console.error(s"Failed while delete the post ($postID) for userID ($userID): ${e.displayMessage}")
            toaster.error("Error deleting post", e.displayMessage)
        }
      } else {
        toaster.warning("Access denied", "You cannot delete this post")
      }
    }
  }

  private def removePostFromList(post: Post) = {
    val index = $scope.posts.indexWhere(_.postID ?== post.postID)
    val found = index != -1
    if (found) $scope.$apply(() => $scope.posts.splice(index, 1))
    found
  }

  $scope.isDeletable = (aPost: js.UndefOr[Post]) => {
    for {
      post <- aPost
      userID <- $scope.userProfile.flatMap(_.userID)
    } yield post.userID.contains(userID)
  }

  $scope.isLikedPost = (aPost: js.UndefOr[Post]) => {
    for {
      post <- aPost
      userID <- $scope.userProfile.flatMap(_.userID)
    } yield post.likedBy.exists(_.contains(userID))
  }

  $scope.likePost = (aPost: js.UndefOr[Post]) => likeOrUnlikePost(aPost, like = true)

  $scope.unlikePost = (aPost: js.UndefOr[Post]) => likeOrUnlikePost(aPost, like = false)

  private def likeOrUnlikePost(aPost: js.UndefOr[Post], like: Boolean) {
    val aPostID = aPost.flatMap(_.postID)
    val aUserID = $scope.userProfile.flatMap(_.userID)
    val result = for {
      post <- aPost.toOption
      postID <- post.postID.toOption
      userID <- $scope.userProfile.flatMap(_.userID).toOption
    } yield (post, postID, userID)

    result match {
      case Some((post, postID, userID)) =>
        post.likeLoading = true
        val promise = if (like) postService.likePost(postID, userID) else postService.unlikePost(postID, userID)
        promise onComplete {
          case Success(response) =>
            val updatedPost = response.data
            console.log(s"updatedPost = ${angular.toJson(updatedPost, pretty = true)}")
            $timeout(() => post.likeLoading = false, 1.second)
            $scope.updatePost(updatedPost)
          case Failure(e) =>
            $scope.$apply(() => post.likeLoading = false)
            console.error(s"Failed while liking the post ($aPostID) for userID ($aUserID): ${e.displayMessage}")
            toaster.error("Error liking a post", e.displayMessage)
        }
      case None =>
        console.error(s"Either the post ($aPostID) or userID ($aUserID) was missing")
    }
  }

  $scope.publishPost = (aPost: js.UndefOr[Post]) => {
    for {
      post <- aPost
      user <- $scope.userProfile
    } {
      post.loading = true
      post.submitter = user
      post.userID = user.userID
      post.creationTime = new js.Date()

      // finally, save the post
      savePost(user, post) onComplete {
        case Success(updatedPost) =>
          console.log(s"updatedPost = ${angular.toJson(updatedPost)}")
          $timeout(() => post.loading = false, 1.second)

          // are there files pending for upload?
          if ($scope.uploader.getNotUploadedItems().nonEmpty) {
            console.log("Scheduling pending files for upload...")
            lastUploadedPost = Option(updatedPost)
            $scope.uploader.uploadAll()
          }

          // update the UI
          $scope.setupNewPost()
          $scope.updatePost(updatedPost)
        case Failure(e) =>
          post.loading = false
          console.error(s"Failed saving a post: ${e.displayMessage}")
          toaster.error("Posting Error", "General fault while publishing a post")
      }
    }
  }

  $scope.reloadPost = (aPostID: js.UndefOr[String]) => aPostID foreach { postID =>
    console.log(s"Attempting to reload post $postID...")
    for (post <- $scope.posts.find(_.postID.exists(_ == postID))) {
      post.loading = true
    }

    postService.getPostByID(postID) onComplete {
      case Success(response) =>
        val updatedPost = response.data
        $scope.$apply(() => $scope.updatePost(updatedPost))
      case Failure(e) =>
        console.error(s"Failed to reload post $postID")
    }
  }

  $scope.setupNewPost = () => {
    console.log(s"Setting up a new post...")
    $scope.newPost = $scope.userProfile.map(Post.apply)
  }

  $scope.updatePost = (anUpdatedPost: js.UndefOr[Post]) => anUpdatedPost foreach { updatedPost =>
    $scope.posts.indexWhere(_.postID ?== updatedPost.postID) match {
      case -1 => $scope.posts.push(updatedPost)
      case index => $scope.posts(index) = updatedPost
    }

    if (updatedPost.submitter.nonAssigned) {
      updatedPost.userID.flat foreach { submitterId =>
        userService.findUserByID(submitterId) onComplete {
          case Success(user) => $scope.$apply(() => updatedPost.submitter = user.data)
          case Failure(e) => toaster.error("Submitter retrieval", e.displayMessage)
        }
      }
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  //      Comment Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.isLikedComment = (aComment: js.UndefOr[Comment]) => {
    for {
      comment <- aComment
      userID <- $scope.userProfile.flatMap(_.userID)
    } yield comment.likedBy.exists(_.contains(userID))
  }

  $scope.likeComment = (aPostID: js.UndefOr[String], aComment: js.UndefOr[Comment]) => {
    likeOrUnlikeComment(aPostID, aComment, like = true)
  }

  $scope.unlikeComment = (aPostID: js.UndefOr[String], aComment: js.UndefOr[Comment]) => {
    likeOrUnlikeComment(aPostID, aComment, like = false)
  }

  private def likeOrUnlikeComment(aPostID: js.UndefOr[String], aComment: js.UndefOr[Comment], like: Boolean) {
    val aUserID = $scope.userProfile.flatMap(_.userID)
    val result = for {
      comment <- aComment.toOption
      commentID <- comment._id.toOption
      postID <- aPostID.toOption
      userID <- aUserID.toOption
    } yield (comment, postID, commentID, userID)

    result match {
      case Some((comment, postID, commentID, userID)) =>
        comment.likeLoading = true
        val promise = if (like) postService.likeComment(postID, commentID, userID) else postService.unlikeComment(postID, commentID, userID)
        promise onComplete {
          case Success(response) =>
            val updatedPost = response.data
            $timeout(() => comment.likeLoading = false, 1.second)
            val index = $scope.posts.indexWhere(_.postID ?== updatedPost.postID)
            if (index != -1) {
              console.log(s"Updating post index $index")
              $scope.$apply(() => $scope.posts(index) = updatedPost)
            }
          case Failure(e) =>
            comment.likeLoading = false
            console.error(s"Failed while liking the comment ($aComment) or userID ($aUserID): ${e.displayMessage}")
            toaster.error("Error performing LIKE", e.displayMessage)
        }
      case None =>
        console.error(s"Either the postID ($aPostID), comment (${aComment.flatMap(_._id)}) or userID ($aUserID) was missing")
    }
  }

  $scope.publishComment = (aPost: js.UndefOr[Post], aComment: js.UndefOr[String]) => {
    for {
      post <- aPost
      postID <- post.postID
      user <- $scope.userProfile
      text <- aComment
    } {
      val submitter = user
      val comment = Comment(text, submitter)

      postService.createComment(postID, comment) onComplete {
        case Success(response) => $scope.$apply(() => $scope.updatePost(response.data))
        case Failure(e) =>
          console.error(s"Failed while adding a new comment the post ($aPost) or userID (${user.userID}): ${e.displayMessage}")
          toaster.error("Error adding comment", e.displayMessage)
      }
    }
  }

  $scope.setupNewComment = (aPost: js.UndefOr[Post]) => aPost foreach (_.newComment = true)

  ///////////////////////////////////////////////////////////////////////////
  //      Reply Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.isLikedReply = (aPost: js.UndefOr[Post], aReply: js.UndefOr[Reply]) => {
    for {
      post <- aPost
      reply <- aReply
      replyLikes <- post.replyLikes
      userID <- $scope.userProfile.flatMap(_.userID)
    } yield replyLikes.exists(_.likedBy.exists(_.contains(userID)))
  }

  $scope.likeReply = (aPostID: js.UndefOr[String], aCommentID: js.UndefOr[String], aReply: js.UndefOr[Reply]) => {
    likeOrUnlikeReply(aPostID, aCommentID, aReply, like = true)
  }

  $scope.unlikeReply = (aPostID: js.UndefOr[String], aCommentID: js.UndefOr[String], aReply: js.UndefOr[Reply]) => {
    likeOrUnlikeReply(aPostID, aCommentID, aReply, like = false)
  }

  private def likeOrUnlikeReply(aPostID: js.UndefOr[String], aCommentID: js.UndefOr[String], aReply: js.UndefOr[Reply], like: Boolean) {
    val aUserID = $scope.userProfile.flatMap(_.userID)
    val result = for {
      postID <- aPostID.toOption
      commentID <- aCommentID.toOption
      reply <- aReply.toOption
      replyID <- reply.replyID.toOption
      userID <- aUserID.toOption
    } yield (reply, postID, commentID, replyID, userID)

    result match {
      case Some((reply, postID, commentID, replyID, userID)) =>
        reply.likeLoading = true
        val promise = if (like) postService.likeReply(postID, commentID, replyID, userID) else postService.unlikeReply(postID, commentID, replyID, userID)
        promise onComplete {
          case Success(response) =>
            val updatedPost = response.data
            $timeout(() => reply.likeLoading = false, 1.second)
            val index = $scope.posts.indexWhere(_.postID ?== updatedPost.postID)
            if (index != -1) {
              console.log(s"Updating post index $index")
              $scope.$apply(() => $scope.posts(index) = updatedPost)
            }
          case Failure(e) =>
            reply.likeLoading = false
            console.error(s"Failed while liking the reply ($aReply) or userID ($aUserID): ${e.displayMessage}")
            toaster.error("Error performing LIKE", e.displayMessage)
        }
      case None =>
        console.error(s"Either the postID ($aPostID), reply (${angular.toJson(aReply)}) or userID ($aUserID) was missing")
    }
  }

  $scope.publishReply = (aPost: js.UndefOr[Post], aComment: js.UndefOr[Comment], aText: js.UndefOr[String]) => {
    for {
      post <- aPost
      postID <- post.postID
      comment <- aComment
      commentID <- comment._id
      user <- $scope.userProfile
      text <- aText
    } {
      val reply = new Reply(text, user.userID)
      postService.createReply(postID, commentID, reply) onComplete {
        case Success(updatedPost) =>
          console.info(s"updatedPost = ${JSON.stringify(updatedPost)}")
          $scope.$apply { () =>
            comment.replies.foreach(_.push(reply))
            comment.newReply = false
          }
        case Failure(e) =>
          console.error(s"Failed while adding a new reply the post ($aPost) or userID (${user.userID}): ${e.displayMessage}")
          toaster.error("Error adding reply", e.displayMessage)
      }
    }
  }

  $scope.setupNewReply = (aComment: js.UndefOr[Comment]) => aComment foreach (_.newReply = true)

  ///////////////////////////////////////////////////////////////////////////
  //      Tag Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.getTags = (aPost: js.UndefOr[Post]) => aPost flatMap (post => post.text.flatMap(extractHashTags) ?? post.tags)

  private def extractHashTags(text: String): js.UndefOr[js.Array[String]] = {
    if (text.contains('#')) {
      val tags = js.Array[String]()
      var lastPos = -1
      do {
        val start = text.indexOf('#', lastPos)
        if (start != -1) {
          val end = text.indexOf(' ', start)
          val limit = if (end != -1) end else text.length
          val hashTag = text.substring(start, limit)
          tags.push(hashTag.tail)
          lastPos = start + hashTag.length
        }
        else lastPos = -1
      } while (lastPos != -1 && lastPos < text.length)

      tags
    }

    else js.undefined
  }

  $scope.appendTag = (aTag: js.UndefOr[String]) => aTag foreach { tag =>
    console.log(s"Adding '$tag' to filter...")
    $scope.tags.push(tag)
    loadPostsByTags($scope.tags)
  }

  $scope.removeTag = (aTag: js.UndefOr[String]) => aTag foreach { tag =>
    $scope.tags.indexOf(tag) match {
      case -1 =>
      case index =>
        $scope.tags.remove(index)
        loadPostsByTags($scope.tags)
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  //      Private Functions
  ///////////////////////////////////////////////////////////////////////////

  private def loadPostsByTags(tags: js.Array[String]): Unit = {
    asyncLoading($scope)(postService.getPostsByTag(tags)) onComplete {
      case Success(response) =>
        val posts = response.data
        $timeout(() => $scope.postsLoading = false, 1.second)
        $scope.$apply(() => $scope.posts = posts)
      case Failure(e) =>
        $scope.postsLoading = false
        console.error(s"Error loading posts for tags '${tags.mkString(", ")}'")
        toaster.error(s"Error loading posts for tags", e.displayMessage)
    }
  }

  private def savePost(user: UserProfile, post: Post): Future[Post] = {
    val alreadySaved = post.postID.isAssigned
    console.log(s"${if (alreadySaved) s"Updating (${post.postID}) " else "Saving"} post...")

    // perform the update
    (if (alreadySaved) postService.updatePost(post) else postService.createPost(post)).map(_.data) map { post =>
      if (post.submitter.isEmpty) post.submitter = user
      post
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  //      Event Listener Functions
  ///////////////////////////////////////////////////////////////////////////

  // clear the queue after all uploads have complete
  $scope.uploader.onCompleteAll = () => {
    $scope.uploader.clearQueue()
    lastUploadedPost.foreach { post =>
      $scope.reloadPost(post.postID)
      lastUploadedPost = None
    }
  }

  // listen for the "onAfterAddingAll" event
  $scope.uploader.onAfterAddingAll = (addedFileItems: js.Array[FileItem]) => {
    console.log("Updating upload endpoints for attachments...")
    console.log(s"newPost = ${angular.toJson($scope.newPost)}")
    console.log(addedFileItems)

    for {
      newPost <- $scope.newPost
      user <- $scope.userProfile
      userId <- user.userID.flat
    } {
      // if the post itself has not already been created ...
      if (newPost.postID.nonAssigned) {
        newPost.submitter = user
        postService.createPost(newPost) onComplete {
          case Success(response) =>
            val post = response.data
            $scope.$apply { () =>
              newPost.postID = post.postID
              for {
                fileItem <- addedFileItems
                postId <- newPost.postID.flat
              } {
                fileItem.url = postService.getUploadURL(postId, userId)
              }
            }
          case Failure(e) =>
            console.error(s"Failed while creating post for upload: ${e.displayMessage}")
            toaster.error("Post Error", "Failed while creating post for upload")
        }
      } else {
        for {
          fileItem <- addedFileItems
          postId <- newPost.postID.flat
        } {
          fileItem.url = postService.getUploadURL(postId, userId)
        }
      }
    }
  }

}

/**
 * Posting Capabilities Scope
 * @author lawrence.daniels@gmail.com
 */
@js.native
trait PostingCapabilitiesScope extends RootScope {
  var newPost: js.UndefOr[Post] = js.native
  var posts: js.Array[Post] = js.native
  var postsLoading: js.UndefOr[Boolean] = js.native
  var tags: js.Array[String] = js.native
  var uploader: FileUploader = js.native

  // posts
  var deletePost: js.Function1[js.UndefOr[Post], Unit] = js.native
  var isDeletable: js.Function1[js.UndefOr[Post], js.UndefOr[Boolean]] = js.native
  var isLikedPost: js.Function1[js.UndefOr[Post], js.UndefOr[Boolean]] = js.native
  var likePost: js.Function1[js.UndefOr[Post], Unit] = js.native
  var publishPost: js.Function1[js.UndefOr[Post], Unit] = js.native
  var reloadPost: js.Function1[js.UndefOr[String], Unit] = js.native
  var setupNewPost: js.Function0[Unit] = js.native
  var unlikePost: js.Function1[js.UndefOr[Post], Unit] = js.native
  var updatePost: js.Function1[js.UndefOr[Post], Unit] = js.native

  // comments
  var isLikedComment: js.Function1[js.UndefOr[Comment], js.UndefOr[Boolean]] = js.native
  var likeComment: js.Function2[js.UndefOr[String], js.UndefOr[Comment], Unit] = js.native
  var unlikeComment: js.Function2[js.UndefOr[String], js.UndefOr[Comment], Unit] = js.native
  var publishComment: js.Function2[js.UndefOr[Post], js.UndefOr[String], Unit] = js.native
  var setupNewComment: js.Function1[js.UndefOr[Post], Unit] = js.native

  // replies
  var isLikedReply: js.Function2[js.UndefOr[Post], js.UndefOr[Reply], js.UndefOr[Boolean]] = js.native
  var likeReply: js.Function3[js.UndefOr[String], js.UndefOr[String], js.UndefOr[Reply], Unit] = js.native
  var unlikeReply: js.Function3[js.UndefOr[String], js.UndefOr[String], js.UndefOr[Reply], Unit] = js.native
  var publishReply: js.Function3[js.UndefOr[Post], js.UndefOr[Comment], js.UndefOr[String], Unit] = js.native
  var setupNewReply: js.Function1[js.UndefOr[Comment], Unit] = js.native

  // tag functions
  var getTags: js.Function1[js.UndefOr[Post], js.UndefOr[js.Array[String]]] = js.native
  var appendTag: js.Function1[js.UndefOr[String], Unit] = js.native
  var removeTag: js.Function1[js.UndefOr[String], Unit] = js.native

}
