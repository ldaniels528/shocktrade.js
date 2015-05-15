(function () {
    var app = angular.module('shocktrade');

    /**
     * Chat Controller
     * @author lawrence.daniels@gmail.com
     */
    app.controller('ChatController', ['$scope', '$location', '$log', '$timeout', 'toaster', 'MySession', 'ContestService',
        function ($scope, $location, $log, $timeout, toaster, MySession, ContestService) {

            $scope.chatMessage = "";

            $scope.addSmiley = function (emoticon) {
                $scope.chatMessage += emoticon.symbol;
            };

            $scope.getMessages = function(contest) {
                return contest.messages.sort(function(a,b) {
                    return b.sentTime - a.sentTime;
                });
            };

            $scope.sendChatMessage = function (messageText) {
                // build the message blob
                var message = {
                    text: messageText,
                    //"recipient": null,
                    "sender": {
                        "_id": MySession.userProfile._id,
                        "name": MySession.getUserName(),
                        "facebookID": MySession.fbUserID
                    }
                };

                // transmit the message
                $log.info("Transmitting message " + angular.toJson(message));
                ContestService
                    .sendChatMessage($scope.contest.OID(), message).then(
                        function (response) {
                            $scope.chatMessage = "";
                        },
                        function (response) {
                            toaster.pop('error', 'Error!', "Failed to send message (" + response.message + ")");
                        });
            };

            $scope.emoticons = [
                {"symbol": ":-)", "uri": "icon_smile.gif", "tooltip": "Smile"},
                {"symbol": ";-)", "uri": "icon_wink.gif", "tooltip": "Wink"},
                {"symbol": ":-D", "uri": "icon_biggrin.gif", "tooltip": "Big Smile"},
                {"symbol": ":->", "uri": "icon_razz.gif", "tooltip": "Razzed"},
                {"symbol": "B-)", "uri": "icon_cool.gif", "tooltip": "Cool"},
                {"symbol": "$-|", "uri": "icon_rolleyes.gif", "tooltip": "Roll Eyes"},
                {"symbol": "8-|", "uri": "icon_eek.gif", "tooltip": "Eek"},
                {"symbol": ":-/", "uri": "icon_confused.gif", "tooltip": "Confused"},
                {"symbol": "|-|", "uri": "icon_redface.gif", "tooltip": "Blush"},
                {"symbol": ":-(", "uri": "icon_sad.gif", "tooltip": "Sad"},
                {"symbol": ":'-(", "uri": "icon_cry.gif", "tooltip": "Cry"},
                {"symbol": ">:-(", "uri": "icon_evil.gif", "tooltip": "Enraged"},
                {"symbol": ":-))", "uri": "icon_mrgreen.gif", "tooltip": "Big Grin"},
                {"symbol": ":-|", "uri": "icon_neutral.gif", "tooltip": "Neutral"},
                {"symbol": ":-O", "uri": "icon_surprised.gif", "tooltip": "Surprised"},
                {"symbol": "(i)", "uri": "icon_idea.gif", "tooltip": "Idea"},
                {"symbol": "(!)", "uri": "icon_exclaim.gif", "tooltip": "Exclamation"},
                {"symbol": "(?)", "uri": "icon_question.gif", "tooltip": "Question"},
                {"symbol": "=>", "uri": "icon_arrow.gif", "tooltip": "Arrow"}];

            //////////////////////////////////////////////////////////////////////
            //              Broadcast Event Listeners
            //////////////////////////////////////////////////////////////////////

            /**
             * Listen for contest message update events
             */
            $scope.$on("messages_updated", function(event, contest) {
                if($scope.contest && ($scope.contest.OID() == contest.OID())) {
                    $log.info("[Chat] Messages for '" + contest.name + "' updated");
                    $scope.contest.messages = contest.messages;
                }
            });

        }]);

})();