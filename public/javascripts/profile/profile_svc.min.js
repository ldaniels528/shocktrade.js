(function () {

    /**
     * Profile Service
     * @author lawrence.daniels@gmail.com
     */
    angular
        .module('shocktrade')
        .factory('ProfileService', function ($http, $log, $q) {
            var service = {}

            /**
             * Retrieves the current user's profile by FaceBook ID
             * @param fbId the given FaceBook ID
             * @returns {*}
             */
            service.getProfileByFacebookID = function (fbId) {
                return $http.get("/api/profile/facebook/" + fbId).then(function (response) {
                    var profile = response.data;
                    profile.id = profile._id.$oid;
                    return profile;
                });
            }

            service.getExchanges = function (id) {
                return $http.get("/api/profile/" + id + "/exchanges");
            }

            service.updateExchanges = function (id, exchanges) {
                return $http({url: "/api/exchanges", method: 'POST', data: {id: id, exchanges: exchanges}});
            }

            return service;
        });
})();
