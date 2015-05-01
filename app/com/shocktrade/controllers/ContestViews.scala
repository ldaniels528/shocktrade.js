package com.shocktrade.controllers

import com.shocktrade.models.contest.Contest
import play.api.mvc.{Action, Controller}

/**
 * Contest Views and JavaScript
 * @author lawrence.daniels@gmail.com
 */
object ContestViews extends Controller {

  def lobby = Action {
    Ok(assets.views.html.game.lobby())
  }

  def playCtrl = Action {
    Ok(assets.javascripts.js.playCtrl())
  }

  def playPerksCtrl = Action {
    Ok(assets.javascripts.js.playPerksCtrl())
  }

  def playStatisticsCtrl = Action {
    Ok(assets.javascripts.js.playStatisticsCtrl())
  }

  def search = Action {
    Ok(assets.views.html.game.search())
  }

}
