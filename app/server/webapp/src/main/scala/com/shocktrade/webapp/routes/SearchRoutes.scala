package com.shocktrade.webapp.routes

import com.shocktrade.common.forms.MaxResultsForm
import com.shocktrade.common.models.EntitySearchResult
import com.shocktrade.common.models.quote.ResearchQuote
import com.shocktrade.common.models.user.User
import com.shocktrade.server.dao.securities.SecuritiesDAO
import com.shocktrade.server.dao.users.UserDAO
import io.scalajs.npm.express.{Application, Request, Response}
import io.scalajs.npm.mongodb.{Collection, Db, _}
import io.scalajs.util.JsUnderOrHelper._
import io.scalajs.util.PromiseHelper.Implicits._

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
  * Search Routes
  * @author lawrence.daniels@gmail.com
  */
object SearchRoutes {

  def init(app: Application, dbFuture: Future[Db])(implicit ec: ExecutionContext): Unit = {
    implicit val securities: Future[SecuritiesSearchAgent] = dbFuture.map(SecuritiesDAO.apply).map(SecuritiesSearchAgent)
    implicit val users: Future[UserSearchAgent] = Future.successful(UserSearchAgent(UserDAO()))

    app.get("/api/search", (request: Request, response: Response, next: NextFunction) => getSearchResults(request, response, next))

    /**
      * Searches for people, groups, organizations and events
      * @example GET /api/search?searchTerm=mic&maxResults=10
      */
    def getSearchResults(request: Request, response: Response, next: NextFunction): Unit = {
      val searchAgents = Seq(users, securities)
      val form = request.queryAs[SearchForm]

      def search(searchTerm: String, maxResults: Int): Future[Seq[EntitySearchResult]] =
        Future.sequence(searchAgents map (_.flatMap(_.search(searchTerm, maxResults)))) map (_.flatten)

      form.searchTerm.toOption map ((_, form.getMaxResults())) match {
        case Some((searchTerm, maxResults)) =>
          search(searchTerm, maxResults) onComplete {
            case Success(searchResults) => response.send(js.Array(searchResults: _*)); next()
            case Failure(e) => response.internalServerError(e); next()
          }
        case None =>
          response.badRequest("Bad Request: searchTerm is required"); next()
      }
    }

  }

  /**
    * String Extensions
    * @param text the given string
    */
  implicit class StringExtensions(val text: String) extends AnyVal {

    def limit(maxLength: Int): String = if (text.length > maxLength) text.take(maxLength) + "..." else text

  }

  /**
    * Search Form
    * @author lawrence.daniels@gmail.com
    */
  @js.native
  trait SearchForm extends MaxResultsForm {
    var searchTerm: js.UndefOr[String] = js.native
  }

  /**
    * Abstract Search Agent
    * @author lawrence.daniels@gmail.com
    */
  trait SearchAgent[T <: js.Any] {

    def coll: Collection

    def fields: js.Array[String]

    def search(searchTerm: String, maxResults: Int)(implicit ec: ExecutionContext): Future[js.Array[EntitySearchResult]] = {
      coll.find[T](selector = getSelection(searchTerm)).limit(maxResults).toArray() map (_ map toSearchResult)
    }

    def toSearchResult(entity: T): EntitySearchResult

    private def getSelection(searchTerm: String): js.Dictionary[js.Any] = {
      fields.foldLeft[List[(String, js.Any)]](Nil) { (list, field) =>
        (field $regex(s"^$searchTerm", ignoreCase = true)) :: list
      } match {
        case Nil => doc()
        case one :: Nil => doc(one)
        case many => doc($or(many: _*))
      }
    }
  }

  /**
    * Securities Search Agent
    * @author lawrence.daniels@gmail.com
    */
  case class SecuritiesSearchAgent(dao: SecuritiesDAO) extends SearchAgent[ResearchQuote] {
    val fields = js.Array("symbol", "name")

    override def toSearchResult(quote: ResearchQuote): EntitySearchResult = {
      new EntitySearchResult(
        _id = quote.symbol,
        name = quote.symbol,
        description = quote.name,
        `type` = "STOCK"
      )
    }

    override def coll: Collection = ???
  }

  /**
    * User Search Agent
    * @author lawrence.daniels@gmail.com
    */
  case class UserSearchAgent(userDAO: UserDAO) extends SearchAgent[User] {
    val fields: js.Array[String] = js.Array("name")

    def coll: Collection = ???

    override def toSearchResult(user: User): EntitySearchResult = {
      new EntitySearchResult(
        _id = user.facebookID,
        name = user.name,
        description = user.description.flat.map(_.limit(50)) ?? "Day Trader",
        `type` = "USER"
      )
    }
  }

}
