/**
 * Commonly-used JavaScript functions
 * @author lawrence.daniels@gmail.com
 */
(function () {

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

})();
