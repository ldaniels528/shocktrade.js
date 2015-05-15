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
         * @returns {HttpPromise}
         */
        service.getPerks = function () {
            return $http.get("/api/perks");
        };

        /**
         * Attempts to purchase the given perk codes
         * @param perkCodes the given perk codes to purchase
         * @returns {*}
         */
        service.purchasePerks = function (perkCodes) {
            return $http({
                method: 'PUT',
                url: "/api/profile/" + MySession.getUserID() + "/perks/purchase",
                data: angular.toJson(perkCodes)
            });
        };

        return service;
    });

})();