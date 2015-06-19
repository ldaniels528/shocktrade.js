/**
 * Commonly-used JavaScript functions
 * @author lawrence.daniels@gmail.com
 */
(function () {

    Object.prototype.OID = function () {
        var self = this;
        return self._id ? self._id.$oid : null;
    };

    window.makeClone = function (obj) {
        // Handle the 3 simple types, and null or undefined
        if (null == obj || "object" != typeof obj) return obj;

        // Handle Date
        else if (obj instanceof Date) {
            var copy = new Date();
            copy.setTime(obj.getTime());
            return copy;
        }

        // Handle Array
        else if (obj instanceof Array) {
            var copy = [];
            for (var i = 0, len = obj.length; i < len; i++) {
                copy[i] = makeClone(obj[i]);
            }
            return copy;
        }

        // Handle Object
        else if (obj instanceof Object) {
            var copy = {};
            for (var attr in obj) {
                if (obj.hasOwnProperty(attr)) copy[attr] = makeClone(obj[attr]);
            }
            return copy;
        }

        else {
            throw new Error("Unable to copy object! Its type isn't supported.");
        }
    };

})();
