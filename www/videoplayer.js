var androidPlay = function(url, showAds, isLive, adServer, callback) {
	adServer = typeof adServer !== 'undefined' ? adServer : "";
    cordova.exec(callback, function(err) {}, "videoplugin", "play", [url,showAds,isLive,adServer]);
}

module.exports = androidPlay;