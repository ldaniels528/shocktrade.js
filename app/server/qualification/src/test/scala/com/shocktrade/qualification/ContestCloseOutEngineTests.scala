package com.shocktrade.qualification

import org.scalatest._

/**
  * Contest Close-Out Engine Tests
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class ContestCloseOutEngineTests extends FreeSpec with Matchers {
/*
  "Close-out a contest" - {

    val dbConnectionString = process.dbConnect getOrElse "mongodb://localhost:27017/shocktrade_test"
    implicit val mongo = MongoDB()
    implicit val dbFuture = mongo.MongoClient.connectFuture(dbConnectionString)
    implicit val contestDAO: ContestDAO = ContestDAO()
    implicit val contestCloseOutEngine = new ContestCloseOutEngine(dbFuture)

    //implicit val defaultPatience = PatienceConfig(timeout = Span(5, Seconds), interval = Span(500, Millis))

    "Liquidate portfolios" in {
      for {
        contest <- createContest().map(_ orDie "Could not be created")
        _ = console.log("contest => ", contest)

        w0 <- contestCloseOutEngine.closeOut(contest)
        _ = console.log("results => ", w0.toJSArray)

      } yield assert(w0.forall(_.result.isOk))
    }

    "Drop the database" in {
      for {
        db <- dbFuture
        op <- db.dropDatabaseFuture()
      } yield assert(op.asInstanceOf[Boolean])
    }

  }

  private def createContest()(implicit contestDAO: Future[ContestDAO]) = {
    val contest = new ContestData(
      _id = js.undefined,
      name = "Contest 1",
      creator = User(_id = "", name = "ldaniels", facebookID = ""),
      startTime = new js.Date() - 3.days,
      expirationTime = new js.Date(),
      startingBalance = 25000.00,
      status = ContestLike.StatusActive,
      messages = emptyArray[ChatMessage],
      // participants & rankings
      participants = emptyArray[Participant],
      // indicators
      friendsOnly = false,
      invitationOnly = false,
      levelCap = js.undefined,
      perksAllowed = true,
      robotsAllowed = true
    )
    contestDAO.create(contest) map {
      case w if w.isOk => w.opsAs[ContestData].headOption
      case w =>
        console.warn("Failed contest outcome => ", w)
        None
    }
  }*/

}
