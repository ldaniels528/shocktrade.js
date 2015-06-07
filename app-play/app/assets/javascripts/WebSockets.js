(function () {
    var app = angular.module('shocktrade');

    /**
     * Web Socket Singleton Service
     * @author Lawrence Daniels <lawrence.daniels@gmail.com>
     */
    app.factory('WebSockets', function ($rootScope, $http, $location, $log, $timeout, toaster, MySession) {
        var service = {};

        // establish the web socket connection
        if (!window.WebSocket) {
            window.WebSocket = window.MozWebSocket;
        }

        var socket;
        var connected = false;
        if (window.WebSocket) {
            connect();
        } else {
            toaster.pop("Your browser does not support Web Sockets.");
        }

        /**
         * Indicates whether a connection is established
         * @returns {boolean}
         */
        service.isConnected = function () {
            return connected;
        };

        /**
         * Transmits the message to the server via web-socket
         * @param message the given message
         */
        service.send = function (message) {
            if (!window.WebSocket) {
                toaster.pop('error', 'Online Status', 'Web socket closed');
                return false;
            }
            if (socket.readyState == WebSocket.OPEN) {
                socket.send(message);
                return true;
            } else {
                toaster.pop('error', 'Online Status', 'Web socket closed: readyState = ' + socket.readyState);
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

        function sendState(connected) {
            var userID = MySession.getFacebookID();
            if(userID) {
                $log.info("Sending connected status for user " + userID + "...");
                if(connected)
                    return $http.put('/api/online/' + userID);
                else
                    return $http.delete('/api/online/' + userID);
            }
            else {
                $log.info("User unknown, waiting 5 seconds...");
                $timeout(function() {
                    sendState(connected);
                }, 5000);
            }
        }

        /**
         * Establishes a web socket connection
         */
        function connect() {
            var endpoint = "ws://" + $location.host() + ":" + $location.port() + "/websocket";
            $log.info("Connecting to websocket endpoint '" + endpoint + "'...");
            socket = new WebSocket(endpoint);

            socket.onopen = function (event) {
                connected = true;
                sendState(connected);
                //toaster.pop('info', 'Online Status', 'You are connected to ShockTrade');
            };

            socket.onclose = function (event) {
                connected = false;
                sendState(connected);
                //toaster.pop('warning', 'Online Status', 'Lost connection to server');

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