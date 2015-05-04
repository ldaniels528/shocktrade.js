(function () {
    var app = angular.module('shocktrade');

    /**
     * Web Socket Singleton Service
     * @author Lawrence Daniels <lawrence.daniels@gmail.com>
     */
    app.factory('WebSockets', function ($rootScope, $location, $log, $timeout) {
        var service = {};

        // establish the web socket connection
        if (!window.WebSocket) {
            window.WebSocket = window.MozWebSocket;
        }

        var socket;
        if (window.WebSocket) {
            connect();
        } else {
            alert("Your browser does not support Web Sockets.");
        }

        /**
         * Transmits the message to the server via web-socket
         * @param message the given message
         * @param scope the given scope
         */
        service.send = function (message, scope) {
            if (!window.WebSocket) {
                scope.addErrorMessage("Web socket closed");
                return false;
            }
            if (socket.readyState == WebSocket.OPEN) {
                socket.send(message);
                return true;
            } else {
                scope.addErrorMessage("Web socket closed: readyState = " + socket.readyState);
                return false;
            }
        };

        /**
         * Handles the incoming web socket message event
         * @param event the given web socket message event
         */
        function handleMessage(event) {
            if (event.data) {
                var message = angular.fromJson(event.data);
                if (message.action) {
                    $log.info("Broadcasting action '" + message.action + "'");
                    $rootScope.$broadcast(message.action, message.data);
                }
                else {
                    $log.warn("Message does not contain an action; message = " + angular.toJson(message));
                }
            }
            else {
                $log.warn("Unhandled event received - " + angular.toJson(event))
            }
        }

        /**
         * Establishes a web socket connection
         */
        function connect() {
            var endpoint = "ws://" + $location.host() + ":" + $location.port() + "/websocket";
            $log.info("Connecting to websocket endpoint '" + endpoint + "'...");
            socket = new WebSocket(endpoint);
            //$log.info("socket = " + angular.toJson(socket));

            socket.onopen = function (event) {
                $log.debug("onOpen: event = " + angular.toJson(event));
            };

            socket.onclose = function (event) {
                $log.error("onClose: event = " + angular.toJson(event));

                $timeout(function () {
                    connect();
                }, 15000);
            };

            socket.onmessage = function (event) {
                handleMessage(event);
            };
        }

        return service;
    });

})();