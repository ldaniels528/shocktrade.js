package com.shocktrade.client.posts

import com.shocktrade.common.Ok
import com.shocktrade.common.models.post.{Comment, Post, Reply, SharedContent}
import io.scalajs.npm.angularjs.Service
import io.scalajs.npm.angularjs.http.{Http, HttpResponse}

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
  def createComment(postID: String, comment: Comment): js.Promise[HttpResponse[Post]] =
    $http.post(s"/api/post/$postID/comment", comment)

  /**
   * Performs a 'Like' on a comment for the given user by ID
   * @param postID    the given post ID
   * @param commentID the given comment ID
   * @param userID    the given user ID
   * @return a promise of the updated [[Post post]]
   */
  def likeComment(postID: String, commentID: String, userID: String): js.Promise[HttpResponse[Post]] =
    $http.put(s"/api/post/$postID/comment/$commentID/like/$userID")

  /**
   * Performs a 'UnLike' on a comment for the given user by ID
   * @param postID    the given post ID
   * @param commentID the given comment ID
   * @param userID    the given user ID
   * @return a promise of the updated [[Post post]]
   */
  def unlikeComment(postID: String, commentID: String, userID: String): js.Promise[HttpResponse[Post]] =
    $http.delete(s"/api/post/$postID/comment/$commentID/like/$userID")

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
  def createReply(postID: String, commentID: String, reply: Reply): js.Promise[HttpResponse[Post]] =
    $http.post(s"/api/post/$postID/comment/$commentID/reply", reply)

  /**
   * Performs a 'Like' on a comment/reply for the given user by ID
   * @param postID    the given post ID
   * @param commentID the given comment ID
   * @param replyID   the given reply ID
   * @param userID    the given user ID
   * @return a promise of the updated [[Post post]]
   */
  def likeReply(postID: String, commentID: String, replyID: String, userID: String): js.Promise[HttpResponse[Nothing]] =
    $http.put(s"/api/post/$postID/comment/$commentID/reply/$replyID/like/$userID")

  /**
   * Performs a 'UnLike' on a comment/reply for the given user by ID
   * @param postID    the given post ID
   * @param commentID the given comment ID
   * @param replyID   the given reply ID
   * @param userID    the given user ID
   * @return a promise of the updated [[Post post]]
   */
  def unlikeReply(postID: String, commentID: String, replyID: String, userID: String): js.Promise[HttpResponse[Post]] =
    $http.delete(s"/api/post/$postID/comment/$commentID/reply/$replyID/like/$userID")

  ///////////////////////////////////////////////////////////////////////////
  //      Post Functions
  ///////////////////////////////////////////////////////////////////////////

  /**
   * Publishes (creates) a new post
   * @param post the given [[Post post]] to create
   * @return a promise of the newly created [[Post post]]
   */
  def createPost(post: Post): js.Promise[HttpResponse[Post]] = $http.post("/api/post", post)

  /**
   * Deletes a post by ID
   * @param postID the given post ID
   * @return a promise of the deletion result
   */
  def deletePost(postID: String): js.Promise[HttpResponse[Ok]] = $http.delete(s"/api/post/$postID")

  /**
   * Performs a 'Like' on a post for the given user by ID
   * @param postID the given post ID
   * @param userID the given user ID
   * @return a promise of the updated [[Post post]]
   */
  def likePost(postID: String, userID: String): js.Promise[HttpResponse[Post]] =
    $http.put(s"/api/post/$postID/like/$userID")

  /**
   * Performs a 'UnLike' on a post for the given user by ID
   * @param postID the given post ID
   * @param userID the given user ID
   * @return a promise of the updated [[Post post]]
   */
  def unlikePost(postID: String, userID: String): js.Promise[HttpResponse[Post]] =
    $http.delete(s"/api/post/$postID/like/$userID")

  /**
   * Retrieves a collection of attachments by user ID
   * @param uid the given [[String user ID]]
   * @return a promise of the retrieved collection of attachments
   */
  def getAttachmentsByUserID(uid: String): js.Promise[HttpResponse[js.Array[Attachment]]] =
    $http.get(s"/api/posts/attachments/user/$uid")

  /**
   * Retrieves a post by its unique identifier
   * @param id the given unique identifier
   * @return a promise of the retrieved [[Post post]]
   */
  def getPostByID(id: String): js.Promise[HttpResponse[Post]] = $http.get(s"/api/post/$id")

  /**
   * Retrieves all posts for a given user by ID
   * @param aUserID the given user ID
   * @return a promise of an array of [[Post posts]]
   */
  def getPostsByUserID(aUserID: js.UndefOr[String]): js.Promise[HttpResponse[js.Array[Post]]] = aUserID.toOption match {
    case Some(id) => $http.get[js.Array[Post]](s"/api/posts/user/$id")
    case None => getPosts
  }

  /**
   * Retrieves all posts
   * @return a promise of an array of [[Post posts]]
   */
  def getPosts: js.Promise[HttpResponse[js.Array[Post]]] = $http.get("/api/posts")

  /**
   * Retrieves all posts for a given user by ID
   * @param aUserID the given user ID
   * @return a promise of an array of [[Post posts]]
   */
  def getNewsFeed(aUserID: js.UndefOr[String]): js.Promise[HttpResponse[js.Array[Post]]] = aUserID.toOption match {
    case Some(id) => $http.get[js.Array[Post]](s"/api/posts/user/$id/newsfeed")
    case None => getPosts
  }

  /**
   * Retrieves all posts for a array of tags
   * @param tags the given array of tags
   * @return a promise of an array of [[Post posts]]
   */
  def getPostsByTag(tags: js.Array[String]): js.Promise[HttpResponse[js.Array[Post]]] =
    $http.get(s"/api/posts/tags?${tags.map(tag => s"tags=$tag").mkString("&")}")

  /**
   * Generates the appropriate attachment upload URL for the given post and user
   * @param postID the given post ID
   * @param userID the given user ID
   * @return the attachment upload URL (e.g. "/api/post/@postID/attachment/@userID")
   */
  def getUploadURL(postID: String, userID: String): String = s"/api/post/$postID/attachment/$userID"

  /**
   * Updates an existing post
   * @param post the given [[Post post]] to update
   * @return a promise of the updated [[Post post]]
   */
  def updatePost(post: Post): js.Promise[HttpResponse[Post]] = $http.put[Post]("/api/post", post)

  ///////////////////////////////////////////////////////////////////////////
  //      Shared Content Functions
  ///////////////////////////////////////////////////////////////////////////

  /**
   * Retrieves the shared content for the given URL
   * @param url the given URL
   * @return the [[SharedContent shared content]]
   */
  def getSharedContent(url: String): js.Promise[HttpResponse[SharedContent]] = {
    $http.get(s"/api/social/content?url=${js.Dynamic.global.encodeURI(url)}")
  }

}

