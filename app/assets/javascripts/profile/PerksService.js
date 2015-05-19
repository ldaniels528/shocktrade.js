(function () {
    var app = angular.module('shocktrade');

    /**
     * Perks Service
     * @author lawrence.daniels@gmail.com
     */
    app.factory('PerksService', function ($http, MySession) {
        var service = {};

        /**
         * Retrieves the complete list of perks
         * @returns {*}
         */
        service.getPerks = function (contestId) {
            return $http.get("/api/contest/" + contestId + "/perks");
        };

        /**
         * Attempts to purchase the given perk codes
         * @param contestId the given contest ID
         * @param playerId the given player ID
         * @param perkCodes the given perk codes to purchase
         * @returns {*}
         */
        service.purchasePerks = function (contestId, playerId, perkCodes) {
            return $http({
                method: 'PUT',
                url: "/api/contest/" + contestId + "/perks/" + playerId,
                data: angular.toJson(perkCodes)
            });
        };

        return service;
    });

})();