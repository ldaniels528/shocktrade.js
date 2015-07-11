package com.shocktrade.javascript.dialogs

import com.ldaniels528.scalascript.core.Http
import com.ldaniels528.scalascript.extensions.{Modal, ModalOptions}
import com.ldaniels528.scalascript.{Service, injected}
import com.shocktrade.javascript.MySession
import com.shocktrade.javascript.models.Participant

import scala.scalajs.js

/**
 * Invite Player Dialog Service
 * @author lawrence.daniels@gmail.com
 */
class InvitePlayerDialogService($http: Http, $modal: Modal, @injected("MySession") mySession: MySession) extends Service {

  /**
   * Invite a player via pop-up dialog
   */
  def popup(participant: Participant) = {
    /*
    function(selectedFriends) {
      if (selectedFriends.length) {
        $log.info("selectedFriends = " + angular.toJson(selectedFriends));
        Facebook.send("http://www.nytimes.com/interactive/2015/04/15/travel/europe-favorite-streets.html");
      }

    }, function() {
      $log.info('Modal dismissed at: ' +new Date());
    }
     */

    val modalInstance = $modal.open[js.Dynamic](ModalOptions(
      templateUrl = "invite_player_dialog.htm",
      controller = classOf[InvitePlayerDialogController].getSimpleName,
      resolve = js.Dictionary[js.Any]("myFriends" -> (() => mySession.fbFriends))
    ))
    modalInstance.result
  }

}
