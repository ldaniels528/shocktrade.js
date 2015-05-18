/**
 * Commonly-used JavaScript functions
 * @author lawrence.daniels@gmail.com
 */
(function () {
    var units = [ "sec", "min", "hour", "day", "month", "year" ];

    Number.prototype.toDuration = function () {
        var duration = this;
        var unit = 0;

        // compute the age
        var age = Math.abs(Date.now() - duration) / 1000;
        if(age >= 60) { age /= 60; unit++; } // seconds -> minutes
        if(age >= 60) { age /= 60; unit++; } // minutes -> hours
        if(age >= 24) { age /= 24; unit++; } // hours -> days
        if(age >= 30) { age /= 30; unit++; } // days -> months
        if(age >= 12) { age /= 12; unit++; } // months -> years
        age = age.toFixed(0);
        return age + " " + units[unit] + ( age != 1 ? "s" : "" ) + " ago";
    };

    Object.prototype.OID = function() {
        var self = this;
        return self._id ? self._id.$oid : null;
    };

    /**
     * Indicates whether the host string ends with the given suffix
     * @param suffix the given suffix string
     * @returns {boolean}
     */
    String.prototype.endsWith = function (suffix) {
        return this.indexOf(suffix, this.length - suffix.length) !== -1;
    };

    /**
     * Returns the capitalize representation of the host string
     * @returns {string}
     */
    String.prototype.capitalize = function () {
        return this.charAt(0).toUpperCase() + this.substring(1).toLocaleLowerCase();
    };

    /**
     * Indicates whether the email address is valid
     * @returns {boolean}
     */
    String.prototype.isValidEmail = function () {
        var email = this;
        var re = /^([\w-]+(?:\.[\w-]+)*)@((?:[\w-]+\.)*\w[\w-]{0,66})\.([a-z]{2,6}(?:\.[a-z]{2})?)$/i;
        return re.test(email);
    }

})();
