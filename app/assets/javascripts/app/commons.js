/**
 * Commonly-used JavaScript functions
 * @author lawrence.daniels@gmail.com
 */
(function () {
    var units = ["sec", "min", "hour", "day", "month", "year"];
    var factors = [60, 60, 24, 30, 12];

    window.toDuration = function (time) {
        var duration = time && time.$date ? time.$date : time;
        if(duration === null || duration === undefined) return null;

        // compute the time delta
        var delta = (Date.now() - duration) / 1000;

        // compute the age
        var unit = 0;
        var age = Math.abs(delta);
        while(age >= factors[unit]) {
            age /= factors[unit];
            unit++;
        }

        // make the age and unit names more readable
        age = age.toFixed(0);
        var unitName = units[unit] + ( age != 1 ? "s" : "" );

        return delta >= 0
            ? age + " " + unitName + " ago"
            : "in " + age + " " + unitName;
    };

    Object.prototype.OID = function () {
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
