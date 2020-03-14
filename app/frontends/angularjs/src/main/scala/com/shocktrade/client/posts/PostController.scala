package com.shocktrade.client.posts

import com.shocktrade.client.GlobalLoading
import com.shocktrade.client.users.{UserFactory, UserService}
import com.shocktrade.common.models.post._
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.AngularJsHelper._
import io.scalajs.npm.angularjs.fileupload.nervgh.FileUploader
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.npm.angularjs.{Controller, Location, Timeout, injected, _}
import io.scalajs.util.DurationHelper._
import io.scalajs.util.JsUnderOrHelper._
import io.scalajs.util.OptionHelper._
import io.scalajs.util.PromiseHelper.Implicits._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.util.{Failure, Success}

/**
 * Post Controller
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
case class PostController($scope: PostControllerScope, $compile: js.Dynamic, $location: Location, $timeout: Timeout, toaster: Toaster,
                          @injected("FileUploader") fileUploader: FileUploader,
                          @injected("PostService") postService: PostService,
                          @injected("UserFactory") userFactory: UserFactory,
                          @injected("UserService") userService: UserService)
  extends Controller with PostingCapabilities with GlobalLoading {

  ///////////////////////////////////////////////////////////////////////////
  //      Initialization Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.showUpload = false

  $scope.init = () => {
    console.log(s"Initializing '${getClass.getSimpleName}'...")
    loadFollowersAndPostings()
    $scope.setupNewPost()
  }

  ///////////////////////////////////////////////////////////////////////////
  //      SEO / Web Summary Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.detectURL = (aPost: js.UndefOr[Post]) => aPost foreach { post =>
    if (!post.summaryLoaded.contains(true) && !post.summaryLoadQueued.contains(true)) {
      post.summaryLoadQueued = true
      $timeout(() => {
        console.log("Launching webpage summary loading process...")
        post.loading = true
        loadWebPageSummary(post) onComplete {
          case Success(_) =>
            $timeout(() => post.loading = false, 1.second)
            $scope.$apply(() => post.summaryLoadQueued = false)
          case Failure(e) =>
            $scope.$apply(() => post.loading = false)
            console.error(s"Metadata retrieval failed: ${e.displayMessage}")
        }
      }, 1.1.seconds)
    }
  }

  $scope.isRefreshable = (aPost: js.UndefOr[Post]) => {
    val user = $scope.userProfile
    for {
      post <- aPost
      text <- post.text
      submitterId <- post.userID
      userId <- user.flatMap(_.userID)
    } yield text.contains("http") && (user.flatMap(_.isAdmin).contains(true) || (submitterId == userId))
  }

  $scope.updateWebSummary = (aPost: js.UndefOr[Post]) => aPost foreach { post =>
    post.refreshLoading = true
    val outcome = for {
      summary <- loadWebPageSummary(post)
      updatedPost <- postService.updatePost(post).map(_.data)
    } yield updatedPost

    outcome onComplete {
      case Success(updatedPost) =>
        $timeout(() => post.refreshLoading = false, 1.second)
        $scope.$apply(() => $scope.updatePost(updatedPost))
      case Failure(e) =>
        $scope.$apply(() => post.refreshLoading = false)
        console.error(s"Metadata retrieval failed: ${e.displayMessage}")
    }
  }

  private def loadWebPageSummary(post: Post) = {
    val result = for {
      text <- post.text.flat.toOption.map(_.trim)
      lcText = text.toLowerCase
      start <- lcText.indexOfOpt("http://") ?? lcText.indexOfOpt("https://")
    } yield (text, start)

    result match {
      case Some((text, start)) =>
        // determine the span of the URL
        val limit = text.indexWhere(nonUrlCharacter, start)
        val end = if (limit != -1) limit else text.length
        val url = text.substring(start, end)
        console.log(s"webpage url => $url")

        // load the page summary information
        postService.getSharedContent(url).map(_.data) map { summary =>
          post.summary = summary
          post.tags = summary.tags
        }
      case None => Future.successful((): Unit)
    }
  }

  private def nonUrlCharacter(c: Char) = !(c.isLetterOrDigit || "_-+.:/?&=#%".contains(c))

  ///////////////////////////////////////////////////////////////////////////
  //      Upload Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.hideUploadPanel = () => $scope.showUpload = false

  $scope.isUploadVisible = () => $scope.showUpload

  $scope.toggleUploadPanel = () => $scope.showUpload = !$scope.showUpload

  ///////////////////////////////////////////////////////////////////////////
  //      Private Functions
  ///////////////////////////////////////////////////////////////////////////

  /**
   * Loads the user's followers and posts
   */
  private def loadFollowersAndPostings(): Unit = {
    val outcome = asyncLoading($scope) {
      for {
        posts <- postService.getPosts.map(_.data)
        enrichedPosts <- enrichPosts(posts)
      } yield enrichedPosts
    }

    outcome onComplete {
      case Success(posts) =>
        $scope.$apply { () =>
          $scope.posts = posts
        }
      case Failure(e) =>
        toaster.error("Retrieval Error", "Error loading posts")
        console.error(s"Failed while retrieving posts: ${e.displayMessage}")
    }
  }

  private def enrichPosts(posts: js.Array[Post]): Future[js.Array[Post]] = {
    val userIds = posts.flatMap(_.userID.flat.toOption)
    userFactory.getUsers(userIds) map { users =>
      console.log(s"users = ${angular.toJson(users)}")
      val userMapping = Map(users.map(u => u.userID.orNull -> u): _*)
      posts foreach { post =>
        post.submitter = post.userID.flatMap(id => userMapping.get(id).orUndefined)
      }
      posts
    }
  }

}

/**
 * Post Controller Scope
 * @author lawrence.daniels@gmail.com
 */
@js.native
trait PostControllerScope extends PostingCapabilitiesScope {

  var showUpload: Boolean = js.native
  var viewURL: String = js.native

  ///////////////////////////////////////////////////////////////////////////
  //      Public Functions
  ///////////////////////////////////////////////////////////////////////////

  // initialization
  var init: js.Function0[Unit] = js.native

  // dialogs
  var profileEditorPopup: js.Function1[js.UndefOr[String], Unit] = js.native

  // SEO/web summary
  var detectURL: js.Function1[js.UndefOr[Post], Unit] = js.native
  var isRefreshable: js.Function1[js.UndefOr[Post], js.UndefOr[Boolean]] = js.native
  var updateWebSummary: js.Function1[js.UndefOr[Post], Unit] = js.native

  // upload functions
  var isUploadVisible: js.Function0[Boolean] = js.native
  var hideUploadPanel: js.Function0[Unit] = js.native
  var toggleUploadPanel: js.Function0[Unit] = js.native

}
