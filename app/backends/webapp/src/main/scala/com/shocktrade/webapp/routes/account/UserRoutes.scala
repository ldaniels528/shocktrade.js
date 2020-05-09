package com.shocktrade.webapp.routes.account

import com.shocktrade.common.Ok
import com.shocktrade.common.api.UserAPI
import com.shocktrade.common.auth.{AuthenticationCode, AuthenticationForm}
import com.shocktrade.common.events.RemoteEvent
import com.shocktrade.common.forms.SignUpForm
import com.shocktrade.common.models.user.OnlineStatus
import com.shocktrade.webapp.routes._
import com.shocktrade.webapp.routes.account.dao.{UserAccountData, UserDAO, UserIconData}
import com.shocktrade.webapp.vm.VirtualMachine
import com.shocktrade.webapp.vm.dao.VirtualMachineDAO
import com.shocktrade.webapp.vm.opcodes.{CreateUserAccount, CreateUserIcon}
import io.scalajs.nodejs.fs.Fs
import io.scalajs.npm.express.{Application, Request, Response}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.util.{Failure, Random, Success}

/**
 * User Routes
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class UserRoutes(app: Application)(implicit ec: ExecutionContext, userDAO: UserDAO, vmDAO: VirtualMachineDAO, vm: VirtualMachine)
  extends UserAPI with RemoteEventSupport {
  private val onlineStatuses = js.Dictionary[OnlineStatus]()

  // users API
  app.post(createAccountURL, (request: Request, response: Response, next: NextFunction) => createAccount(request, response, next))
  app.get(findUserByIDURL(":userID"), (request: Request, response: Response, next: NextFunction) => findUserByID(request, response, next))
  app.get(findUserByNameURL(":username"), (request: Request, response: Response, next: NextFunction) => findUserByName(request, response, next))
  app.get(findUserIconURL(":userID"), (request: Request, response: Response, next: NextFunction) => findUserIcon(request, response, next))
  app.get(findUsersURL(":ids"), (request: Request, response: Response, next: NextFunction) => findUsersByIDs(request, response, next))
  app.get(findMyAwardsURL(":userID"), (request: Request, response: Response, next: NextFunction) => findMyAwards(request, response, next))

  // authentication API
  app.get(getCodeURL, (request: Request, response: Response, next: NextFunction) => code(request, response, next))
  app.post(loginURL, (request: Request, response: Response, next: NextFunction) => login(request, response, next))
  app.post(logoutURL, (request: Request, response: Response, next: NextFunction) => logout(request, response, next))

  // session API
  app.get(getOnlineStatusURL(":userID"), (request: Request, response: Response, next: NextFunction) => getOnlineStatus(request, response, next))
  app.get(getOnlineStatusUpdatesURL(":since"), (request: Request, response: Response, next: NextFunction) => getOnlineStatusUpdates(request, response, next))
  app.put(setIsOnlineURL(":userID"), (request: Request, response: Response, next: NextFunction) => setIsOnline(request, response, next))
  app.delete(setIsOfflineURL(":userID"), (request: Request, response: Response, next: NextFunction) => setIsOffline(request, response, next))

  // administrative routes
  app.get(getUpdateUserIconsURL, (request: Request, response: Response, next: NextFunction) => updateUserIcons(request, response, next))

  //////////////////////////////////////////////////////////////////////////////////////
  //      API Methods
  //////////////////////////////////////////////////////////////////////////////////////

  def createAccount(request: Request, response: Response, next: NextFunction): Unit = {
    val form = request.bodyAs[SignUpForm]
    val args = (for {
      username <- form.username
      email <- form.email
      password <- form.password
      passwordConfirm <- form.passwordConfirm
    } yield (username, email, password, passwordConfirm)).toOption

    args match {
      case Some((username, email, password, passwordConfirm)) =>
        val outcome = for {
           _ <- vm.invoke(CreateUserAccount(new UserAccountData(
            username = username,
            email = email,
            password = password,
            wallet = 250e+3,
          )))
          account_? <- userDAO.findUserByName(username)
        } yield account_?

        outcome onComplete {
          case Success(Some(profileData)) => response.send(profileData); next()
          case Success(None) => response.internalServerError("User account could not be created"); next()
          case Failure(e) => response.internalServerError(e.getMessage); next()
        }
      case None =>
        response.badRequest("The username and password are required"); next()
    }
  }

  def findMyAwards(request: Request, response: Response, next: NextFunction): Unit = {
    val userID = request.params("userID")
    userDAO.findMyAwards(userID) onComplete {
      case Success(results) => response.send(results); next()
      case Failure(e) => response.showException(e).internalServerError(e); next()
    }
  }

  def findUserByID(request: Request, response: Response, next: NextFunction): Unit = {
    val userID = request.params("userID")
    userDAO.findUserByID(userID) onComplete {
      case Success(Some(user)) => response.send(user); next()
      case Success(None) => response.notFound(request.params); next()
      case Failure(e) => response.showException(e).internalServerError(e); next()
    }
  }

  def findUsersByIDs(request: Request, response: Response, next: NextFunction): Unit = {
    val userIDs = request.query.get("ids").toList.flatMap(_.split("[+]"))
    userDAO.findUsersByIDs(userIDs) onComplete {
      case Success(users) => response.send(users); next()
      case Failure(e) => response.showException(e).internalServerError(e); next()
    }
  }

  def findUserIcon(request: Request, response: Response, next: NextFunction): Unit = {
    val userID = request.params("userID")
    val outcome = for {
      icon_? <- userDAO.findUserIcon(userID)
      (mime, image) <- icon_? match {
        case Some(icon) => Future.successful((icon.mime, icon.image))
        case None => Fs.readFileFuture("./public/images/avatars/avatar101.png").map(image => ("image/png": js.UndefOr[String], image))
      }
    } yield (mime, image)

    outcome onComplete {
      case Success((mime, image)) =>
        mime.foreach(response.setContentType)
        response.send(image)
        next()
      case Failure(e) =>
        response.showException(e).internalServerError(e); next()
    }
  }

  /**
   * Retrieves a user by username
   */
  def findUserByName(request: Request, response: Response, next: NextFunction): Unit = {
    val username = request.params("username")
    userDAO.findUserByName(username) onComplete {
      case Success(Some(user)) => response.setContentType("application/json"); response.send(user); next()
      case Success(None) => response.notFound(request.params); next()
      case Failure(e) => response.showException(e).internalServerError(e); next()
    }
  }

  //////////////////////////////////////////////////////////////////////////////////////
  //      Authentication API Methods
  //////////////////////////////////////////////////////////////////////////////////////

  def code(request: Request, response: Response, next: NextFunction): Unit = {
    val charset = ('A' to 'Z') ++ ('a' to 'z') ++ ('0' to '9') ++ Seq('_', '-')
    val random = new Random()
    val code = new String((for (_ <- 1 to 16; c = charset(random.nextInt(charset.length))) yield c).toArray)
    response.send(new AuthenticationCode(code))
    next()
  }

  def login(request: Request, response: Response, next: NextFunction): Unit = {
    val form = request.bodyAs[AuthenticationForm]
    val args = (for {
      username <- form.username
      password <- form.password
      authCode <- form.authCode
    } yield (username, password, authCode)).toOption

    args match {
      case Some((username, password, authCode)) =>
        userDAO.findUserByName(username) onComplete {
          case Success(Some(accountData)) => response.send(accountData); next()
          case Success(None) => response.notFound(form); next()
          case Failure(e) => response.internalServerError(e.getMessage); next()
        }
      case None =>
        response.badRequest("The username and password are required"); next()
    }
  }

  def logout(request: Request, response: Response, next: NextFunction): Unit = {
    response.send(Ok(updateCount = 1));
    next()
  }

  //////////////////////////////////////////////////////////////////////////////////////
  //      Online Status API Methods
  //////////////////////////////////////////////////////////////////////////////////////

  def getOnlineStatusUpdates(request: Request, response: Response, next: NextFunction): Unit = {
    val since = request.params("since")
    val ts = since.toDouble
    response.send(onlineStatuses.values.filter(_.lastUpdatedTime >= ts).toJSArray)
    next()
  }

  def getOnlineStatus(request: Request, response: Response, next: NextFunction): Unit = {
    val userID = request.params("userID")
    val status = onlineStatuses.getOrElseUpdate(userID, new OnlineStatus(userID))
    response.send(status)
    next()
  }

  def setIsOnline(request: Request, response: Response, next: NextFunction): Unit = {
    val userID = request.params("userID")
    val status = onlineStatuses.getOrElseUpdate(userID, new OnlineStatus(userID))
    status.connected = true
    wsEmit(RemoteEvent.createUserStatusUpdateEvent(userID, connected = status.connected))
    response.send(status)
    next()
  }

  def setIsOffline(request: Request, response: Response, next: NextFunction): Unit = {
    val userID = request.params("userID")
    val status = onlineStatuses.getOrElseUpdate(userID, new OnlineStatus(userID))
    status.connected = false
    wsEmit(RemoteEvent.createUserStatusUpdateEvent(userID, connected = status.connected))
    response.send(status)
    next()
  }

  def updateUserIcons(request: Request, response: Response, next: NextFunction): Unit = {
    UserRoutes.uploadUserIcons() onComplete {
      case Success(values) => response.send(s"${values.length} user icons loaded"); next()
      case Failure(e) => response.showException(e).internalServerError(e); next()
    }
  }

}

/**
 * User Routes
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object UserRoutes {

  def writeImage(name: String, path: String)(implicit userDAO: UserDAO, vmDAO: VirtualMachineDAO, vm: VirtualMachine): Future[Int] = {
    val suffix = path.lastIndexOf(".") match {
      case -1 => None
      case index => Some(path.substring(index + 1).toLowerCase())
    }
    val mime = suffix match {
      case None => "image/png"
      case Some("jpg") => "image/jpeg"
      case Some(imageType) => s"image/$imageType"
    }
    for {
      data <- Fs.readFileFuture(path)
      Some(user) <- userDAO.findUserByName(name)
      _ <- vm.invoke(CreateUserIcon(new UserIconData(userID = user.userID, name = name, mime = mime, image = data)))
    } yield 1
  }

  private def uploadUserIcons()(implicit userDAO: UserDAO, vmDAO: VirtualMachineDAO, vm: VirtualMachine): Future[Seq[Int]] = {
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
        ("fugitive528", "./public/images/avatars/fugitive528.jpg"),
        ("dannywoo", "./public/images/avatars/dannywoo.jpg")) map { case (name, path) if Fs.existsSync(path) =>
        val outcome = writeImage(name = name, path = path)
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
