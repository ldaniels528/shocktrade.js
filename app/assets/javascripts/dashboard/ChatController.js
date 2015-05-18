(function () {
    var app = angular.module('shocktrade');

    /**
     * Chat Controller
     * @author lawrence.daniels@gmail.com
     */
    app.controller('ChatController', ['$scope', '$location', '$log', '$sce', '$timeout', 'toaster', 'MySession', 'ContestService',
        function ($scope, $location, $log, $sce, $timeout, toaster, MySession, ContestService) {

            $scope.chatMessage = "";
            var lastUpdateTime = 0;
            var lastMessageCount = 0;
            var cachedHtml = "";

            /**
             * Adds a smiley (emoticon) to the text input component
             * @param emoticon the given emoticon
             */
            $scope.addSmiley = function (emoticon) {
                $scope.chatMessage += emoticon.symbol;
            };

            /**
             * Returns the array of defined emotions
             * @returns {*[]}
             */
            $scope.getEmoticons = function () {
                return emoticons;
            };

            /**
             * Returns chat messages sorted by time
             * @param contest the given contest
             * @returns {String}
             */
            $scope.getMessages = function (contest) {
                if((contest.messages.length === lastMessageCount) && (Date.now() - lastUpdateTime) < 15) return cachedHtml;
                else {
                    var startTime = Date.now();

                    // capture the new number of lines
                    lastMessageCount = contest.messages.length;

                    // sort the messages by time
                    var messages = contest.messages
                        .sort(function (a, b) {
                            return b.sentTime - a.sentTime;
                        });

                    // build an HTML string with emoticons
                    var html = "";
                    messages.forEach(function (msg) {
                        var text = msg.text;
                        angular.forEach(emoticons, function (emo) {
                            var index;
                            while ((index = text.indexOf(emo.symbol)) !== -1) {
                                text = text.substring(0, index) +
                                    "<img src='/assets/images/smilies/" + emo.uri + "'>" +
                                    text.substring(index + emo.symbol.length, text.length);
                            }
                        });
                        html += '<img src="http://graph.facebook.com/' + msg.sender.facebookID + '/picture" class="chat_icon">' +
                            '<span class="bold" style="color: ' + msg.color + '">' + msg.sender.name + '</span>&nbsp;' +
                            '[<span class="st_bkg_color">' + msg.sentTime.toDuration() + '</span>] &nbsp;' + text + "<br>";
                    });

                    $log.info("Generated HTML in " + (Date.now() - startTime) + " msec(s)");
                    lastUpdateTime = Date.now();
                    cachedHtml = $sce.trustAsHtml(html);
                    return cachedHtml;
                }
            };

            /**
             * Sends a chat message to the server
             * @param messageText the given chat message text
             */
            $scope.sendChatMessage = function (messageText) {
                if (messageText && messageText.trim().length) {
                    // build the message blob
                    var message = {
                        text: messageText,
                        //"recipient": null,
                        "sender": {
                            "_id": { "$oid" : MySession.getUserID() },
                            "name": MySession.getUserName(),
                            "facebookID": MySession.fbUserID
                        }
                    };

                    // transmit the message
                    ContestService.sendChatMessage($scope.contest.OID(), message)
                        .success(function (response) {
                            $scope.chatMessage = "";
                        })
                        .error(function (response) {
                            toaster.pop('error', 'Error!', "Failed to send message");
                        });
                }
            };

            var emoticons = [
                {"symbol": ":-))", "uri": "icon_mrgreen.gif", "tooltip": "Big Grin"},
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
            $scope.$on("messages_updated", function (event, contest) {
                if ($scope.contest && ($scope.contest.OID() === contest.OID())) {
                    $log.info("[Chat] Messages for '" + contest.name + "' updated");
                    $scope.contest.messages = contest.messages;
                }
            });

        }]);

})();