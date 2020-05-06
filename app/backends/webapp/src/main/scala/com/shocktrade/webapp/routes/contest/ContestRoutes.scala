package com.shocktrade.webapp.routes
package contest

import java.util.UUID

import com.shocktrade.common.api.ContestAPI
import com.shocktrade.common.events.RemoteEvent
import com.shocktrade.common.forms.{ContestCreationRequest, ContestSearchOptions, ValidationErrors}
import com.shocktrade.common.models.contest.{ChatMessage, ContestRanking}
import com.shocktrade.webapp.routes
import com.shocktrade.webapp.routes.account.UserRoutes
import com.shocktrade.webapp.routes.contest.dao._
import com.shocktrade.webapp.vm.VirtualMachine
import com.shocktrade.webapp.vm.dao.VirtualMachineDAO
import com.shocktrade.webapp.vm.opcodes.{CreateContest, JoinContest, QuitContest, SendChatMessage}
import io.scalajs.nodejs.fs.Fs
import io.scalajs.npm.express.{Application, Request, Response}

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js.JSConverters._
import scala.util.{Failure, Success}

/**
 * Contest Routes
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class ContestRoutes(app: Application)(implicit ec: ExecutionContext, contestDAO: ContestDAO, vmDAO: VirtualMachineDAO, vm: VirtualMachine)
  extends ContestAPI {

  // individual contests
  app.post(createContestURL, (request: Request, response: Response, next: NextFunction) => createContest(request, response, next))
  app.get(findContestByIDURL(":id"), (request: Request, response: Response, next: NextFunction) => findContestByID(request, response, next))

  // chat messages
  app.get(findChatMessagesURL(":id"), (request: Request, response: Response, next: NextFunction) => findChatMessages(request, response, next))
  app.post(sendChatMessageURL(":id"), (request: Request, response: Response, next: NextFunction) => sendChatMessage(request, response, next))

  // contest participation
  app.delete(quitContestURL(":id", ":userID"), (request: Request, response: Response, next: NextFunction) => quitContest(request, response, next))
  app.put(joinContestURL(":id", ":userID"), (request: Request, response: Response, next: NextFunction) => joinContest(request, response, next))

  // collections of contests
  app.get(contestSearchURL(), (request: Request, response: Response, next: NextFunction) => contestSearch(request, response, next))
  app.get(findContestRankingsURL(":id"), (request: Request, response: Response, next: NextFunction) => findContestRankings(request, response, next))

  // administrative routes
  app.get(getUpdateUserIconsURL, (request: Request, response: Response, next: NextFunction) => updateUserIcons(request, response, next))

  //////////////////////////////////////////////////////////////////////////////////////
  //      API Methods
  //////////////////////////////////////////////////////////////////////////////////////

  /**
   * Searches for contest via a [[ContestSearchOptions contest search form]]
   */
  def contestSearch(request: Request, response: Response, next: NextFunction): Unit = {
    val form = request.bodyAs[ContestSearchOptions]
    form.validate match {
      case messages if messages.isEmpty =>
        contestDAO.contestSearch(form) onComplete {
          case Success(contests) =>
            response.setContentType("application/json")
            response.send(contests)
            next()
          case Failure(e) => response.showException(e).internalServerError(e); next()
        }
      case messages =>
        response.badRequest(new ValidationErrors(messages)); next()
    }
  }

  /**
   * Creates a new contest
   */
  def createContest(request: Request, response: Response, next: NextFunction): Unit = {
    val form = request.bodyAs[ContestCreationRequest].copy(contestID = UUID.randomUUID().toString)
    form.validate match {
      case messages if messages.nonEmpty =>
        response.badRequest(new ValidationErrors(messages)); next()
      case _ =>
        vm.invoke(CreateContest(form)) onComplete {
          case Success(result) => response.send(result); next()
          case Failure(e) => response.showException(e).internalServerError(e); next()
        }
    }
  }

  def findChatMessages(request: Request, response: Response, next: NextFunction): Unit = {
    val contestID = request.params("id")
    contestDAO.findChatMessages(contestID) onComplete {
      case Success(messages) => response.setContentType("application/json"); response.send(messages); next()
      case Failure(e) => response.showException(e).internalServerError(e); next()
    }
  }

  /**
   * Retrieves contests by portfolio
   */
  def findContestByID(request: Request, response: Response, next: NextFunction): Unit = {
    val contestID = request.params("id")
    contestDAO.findOneByID(contestID) onComplete {
      case Success(Some(contest)) => response.send(contest); next()
      case Success(None) => response.notFound(contestID); next()
      case Failure(e) => response.showException(e).internalServerError(e); next()
    }
  }

  /**
   * Retrieves a collection of rankings by contest
   */
  def findContestRankings(request: Request, response: Response, next: NextFunction): Unit = {
    val contestID = request.params("id")
    contestDAO.findRankings(contestID).map(ContestRanking.computeRankings(_)) onComplete {
      case Success(rankings) => response.setContentType("application/json"); response.send(rankings.toJSArray); next()
      case Failure(e) => response.showException(e).internalServerError(e); next()
    }
  }

  def joinContest(request: Request, response: Response, next: NextFunction): Unit = {
    val (contestID, userID) = (request.params("id"), request.params("userID"))
    vm.invoke(JoinContest(contestID, userID)) onComplete {
      case Success(result) => response.send(result); next()
      case Failure(e) => response.showException(e).internalServerError(e); next()
    }
  }

  def sendChatMessage(request: Request, response: Response, next: NextFunction): Unit = {
    // get the arguments
    val form = for {
      contestID <- request.params.get("id")
      chatMessage = request.bodyAs[ChatMessage]
      userID <- chatMessage.userID.toOption
      message <- chatMessage.message.toOption
    } yield (contestID, userID, message, chatMessage)

    // handle the request
    form match {
      case Some((contestID, userID, message, chatMessage)) =>
        // asynchronously create the message
        vm.invoke(SendChatMessage(contestID, userID, message)) onComplete {
          // HTTP/200 OK
          case Success(result) =>
            response.send(result)
            //WebSocketHandler.emit(RemoteEvent.createMessageEvent(chatMessage))
            next()
          // HTTP/500 ERROR
          case Failure(e) =>
            response.showException(e).internalServerError(e); next()
        }
      // HTTP/404 NOT FOUND
      case None =>
        response.notFound(request.params); next()
    }
  }

  def quitContest(request: Request, response: Response, next: NextFunction): Unit = {
    val (contestID, userID) = (request.params("id"), request.params("userID"))
    vm.invoke(QuitContest(contestID, userID)) onComplete {
      case Success(result) => response.send(result); next()
      case Failure(e) => response.showException(e).internalServerError(e); next()
    }
  }

  def updateUserIcons(request: Request, response: Response, next: NextFunction): Unit = {
    uploadUserIcons onComplete {
      case Success(values) => response.send(s"${values.length} user icons loaded"); next()
      case Failure(e) => response.showException(e).internalServerError(e); next()
    }
  }

  private def uploadUserIcons: Future[Seq[Int]] = {
    import routes.dao._
    Future.sequence {
      Seq(
        ("ldaniels", "./public/images/avatars/gears.jpg"),
        ("natech", "./public/images/avatars/dcu.png"),
        ("gunst4rhero", "./public/images/avatars/gunstar-heroes.jpg"),
        ("gadget", "./public/images/avatars/sickday.jpg"),
        ("daisy", "./public/images/avatars/daisy.jpg"),
        ("teddy", "./public/images/avatars/teddy.jpg"),
        ("joey", "./public/images/avatars/joey.jpg"),
        ("dizorganizer", "./public/images/avatars/bkjk.jpg"),
        ("naughtymonkey", "./public/images/avatars/naughtymonkey.jpg"),
        ("seralovett", "./public/images/avatars/hearts.jpg"),
        ("fugitive528", "./public/images/avatars/fugitive528.jpg")) map { case (name, path) if Fs.existsSync(path) =>
        val outcome = UserRoutes.writeImage(name = name, path = path)
        outcome onComplete {
          case Success(value) => println(s"$name ~> $path: count = $value")
          case Failure(e) =>
            println(s"Failed to set icon for user '$name':")
            e.printStackTrace()
        }
        outcome
      }
    }
  }

}
