var exec = require("cordova/exec");

module.exports = {
    
    many_play: function (paths, number, successCallback, errorCallback) {
        exec(successCallback, errorCallback, "VideoPlayer", "many_play", [paths, number]);
    },
    
    custom_play: function (paths, number, successCallback, errorCallback) {
        exec(successCallback, errorCallback, "VideoPlayer", "custom_play", [paths, number]);
    },

    merge: function () {
        var obj = {};
        Array.prototype.slice.call(arguments).forEach(function(source) {
            for (var prop in source) {
                obj[prop] = source[prop];
            }
        });
        return obj;
    }
};
