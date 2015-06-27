package com.shocktrade.javascript.dialogs

import biz.enef.angulate.named
import com.ldaniels528.javascript.angularjs.core.{Http, Modal, ModalOptions, Service}
import com.shocktrade.javascript.MySession

import scala.scalajs.js

/**
 * Invite Player Dialog Service
 * @author lawrence.daniels@gmail.com
 */
class InvitePlayerDialogService($http: Http, $modal: Modal, @named("MySession") mySession: MySession) extends Service {

  /**
   * Invite a player via pop-up dialog
   */
  def popup(participant: js.Dynamic) = {
    // create an instance of the dialog
    val options = ModalOptions()
    options.templateUrl = "invite_player_dialog.htm"
    options.controller = classOf[InvitePlayerDialogController].getSimpleName
    options.resolve = js.Dictionary[js.Any]("myFriends" -> (() => mySession.fbFriends))

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

    val modalInstance = $modal.open[js.Dynamic](options)
    modalInstance.result
  }

}
