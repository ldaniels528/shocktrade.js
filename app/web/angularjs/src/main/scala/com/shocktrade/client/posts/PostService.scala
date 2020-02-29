package com.shocktrade.client.posts

import com.shocktrade.common.models.OperationResult
import com.shocktrade.common.models.post.{Comment, Post, Reply, SharedContent}
import io.scalajs.npm.angularjs.Service
import io.scalajs.npm.angularjs.http.Http

import scala.concurrent.ExecutionContext
import scala.scalajs.js

/**
  * Posts Service
  * @author lawrence.daniels@gmail.com
  */
class PostService($http: Http) extends Service {

  ///////////////////////////////////////////////////////////////////////////
  //      Comment Functions
  ///////////////////////////////////////////////////////////////////////////

  /**
    * Publishes (appends) the given comment to the post represented by the given post ID
    * @param postID  the given post ID
    * @param comment the given [[Comment comment]]
    * @return a promise of a [[Post post]]
    */
  def createComment(postID: String, comment: Comment)(implicit ec: ExecutionContext) = {
    $http.post[Post](s"/api/post/$postID/comment", comment)
  }

  /**
    * Performs a 'Like' on a comment for the given user by ID
    * @param postID    the given post ID
    * @param commentID the given comment ID
    * @param userID    the given user ID
    * @return a promise of the updated [[Post post]]
    */
  def likeComment(postID: String, commentID: String, userID: String)(implicit ec: ExecutionContext) = {
    $http.put[Post](s"/api/post/$postID/comment/$commentID/like/$userID")
  }

  /**
    * Performs a 'UnLike' on a comment for the given user by ID
    * @param postID    the given post ID
    * @param commentID the given comment ID
    * @param userID    the given user ID
    * @return a promise of the updated [[Post post]]
    */
  def unlikeComment(postID: String, commentID: String, userID: String)(implicit ec: ExecutionContext) = {
    $http.delete[Post](s"/api/post/$postID/comment/$commentID/like/$userID")
  }

  ///////////////////////////////////////////////////////////////////////////
  //      Reply Functions
  ///////////////////////////////////////////////////////////////////////////

  /**
    * Publishes (appends) the given reply to the post represented by the given post ID
    * @param postID    the given post ID
    * @param commentID the given comment ID
    * @param reply     the given [[Reply comment]]
    * @return a promise of a [[Post post]]
    */
  def createReply(postID: String, commentID: String, reply: Reply)(implicit ec: ExecutionContext) = {
    $http.post[Post](s"/api/post/$postID/comment/$commentID/reply", reply)
  }

  /**
    * Performs a 'Like' on a comment/reply for the given user by ID
    * @param postID    the given post ID
    * @param commentID the given comment ID
    * @param replyID   the given reply ID
    * @param userID    the given user ID
    * @return a promise of the updated [[Post post]]
    */
  def likeReply(postID: String, commentID: String, replyID: String, userID: String)(implicit ec: ExecutionContext) = {
    $http.put[Post](s"/api/post/$postID/comment/$commentID/reply/$replyID/like/$userID")
  }

  /**
    * Performs a 'UnLike' on a comment/reply for the given user by ID
    * @param postID    the given post ID
    * @param commentID the given comment ID
    * @param replyID   the given reply ID
    * @param userID    the given user ID
    * @return a promise of the updated [[Post post]]
    */
  def unlikeReply(postID: String, commentID: String, replyID: String, userID: String)(implicit ec: ExecutionContext) = {
    $http.delete[Post](s"/api/post/$postID/comment/$commentID/reply/$replyID/like/$userID")
  }

  ///////////////////////////////////////////////////////////////////////////
  //      Post Functions
  ///////////////////////////////////////////////////////////////////////////

  /**
    * Publishes (creates) a new post
    * @param post the given [[Post post]] to create
    * @return a promise of the newly created [[Post post]]
    */
  def createPost(post: Post)(implicit ec: ExecutionContext) = {
    $http.post[Post]("/api/post", post)
  }

  /**
    * Deletes a post by ID
    * @param postID the given post ID
    * @return a promise of the deletion result
    */
  def deletePost(postID: String)(implicit ec: ExecutionContext) = {
    $http.delete[OperationResult](s"/api/post/$postID")
  }

  /**
    * Performs a 'Like' on a post for the given user by ID
    * @param postID the given post ID
    * @param userID the given user ID
    * @return a promise of the updated [[Post post]]
    */
  def likePost(postID: String, userID: String)(implicit ec: ExecutionContext) = {
    $http.put[Post](s"/api/post/$postID/like/$userID")
  }

  /**
    * Performs a 'UnLike' on a post for the given user by ID
    * @param postID the given post ID
    * @param userID the given user ID
    * @return a promise of the updated [[Post post]]
    */
  def unlikePost(postID: String, userID: String)(implicit ec: ExecutionContext) = {
    $http.delete[Post](s"/api/post/$postID/like/$userID")
  }

  /**
    * Retrieves a collection of attachments by user ID
    * @param uid the given [[String user ID]]
    * @return a promise of the retrieved collection of attachments
    */
  def getAttachmentsByUserID(uid: String)(implicit ec: ExecutionContext) = {
    $http.get[js.Array[Attachment]](s"/api/posts/attachments/user/$uid")
  }

  /**
    * Retrieves a post by its unique identifier
    * @param id the given unique identifier
    * @return a promise of the retrieved [[Post post]]
    */
  def getPostByID(id: String)(implicit ec: ExecutionContext) = {
    $http.get[Post](s"/api/post/$id")
  }

  /**
    * Retrieves all posts
    * @return a promise of an array of [[Post posts]]
    */
  def getPosts(implicit ec: ExecutionContext) = {
    $http.get[js.Array[Post]]("/api/posts")
  }

  /**
    * Retrieves all posts for a given user by ID
    * @param aUserID the given user ID
    * @return a promise of an array of [[Post posts]]
    */
  def getPostsByUserID(aUserID: js.UndefOr[String])(implicit ec: ExecutionContext) = aUserID.toOption match {
    case Some(id) => $http.get[js.Array[Post]](s"/api/posts/user/$id")
    case None => getPosts
  }

  /**
    * Retrieves all posts for a given user by ID
    * @param aUserID the given user ID
    * @return a promise of an array of [[Post posts]]
    */
  def getNewsFeed(aUserID: js.UndefOr[String])(implicit ec: ExecutionContext) = aUserID.toOption match {
    case Some(id) => $http.get[js.Array[Post]](s"/api/posts/user/$id/newsfeed")
    case None => getPosts
  }

  /**
    * Retrieves all posts for a array of tags
    * @param tags the given array of tags
    * @return a promise of an array of [[Post posts]]
    */
  def getPostsByTag(tags: js.Array[String])(implicit ec: ExecutionContext) = {
    $http.get[js.Array[Post]](s"/api/posts/tags?${tags.map(tag => s"tags=$tag").mkString("&")}")
  }

  /**
    * Generates the appropriate attachment upload URL for the given post and user
    * @param postID the given post ID
    * @param userID the given user ID
    * @return the attachment upload URL (e.g. "/api/post/@postID/attachment/@userID")
    */
  def getUploadURL(postID: String, userID: String)(implicit ec: ExecutionContext) = {
    s"/api/post/$postID/attachment/$userID"
  }

  /**
    * Updates an existing post
    * @param post the given [[Post post]] to update
    * @return a promise of the updated [[Post post]]
    */
  def updatePost(post: Post)(implicit ec: ExecutionContext) = {
    $http.put[Post]("/api/post", post)
  }

  ///////////////////////////////////////////////////////////////////////////
  //      Shared Content Functions
  ///////////////////////////////////////////////////////////////////////////

  /**
    * Retrieves the shared content for the given URL
    * @param url the given URL
    * @return the [[SharedContent shared content]]
    */
  def getSharedContent(url: String)(implicit ec: ExecutionContext) = {
    $http.get[SharedContent](s"/api/social/content?url=${js.Dynamic.global.encodeURI(url)}")
  }

}

