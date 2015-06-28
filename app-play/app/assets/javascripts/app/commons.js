/**
 * Commonly-used JavaScript functions
 * @author lawrence.daniels@gmail.com
 */
(function () {

    Object.prototype.OID = function () {
        var self = this;
        return self._id ? self._id.$oid : null;
    };

})();
