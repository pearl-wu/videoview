# Video TEST

###Instakkation
        cordova plugin add https://github.com/pearl-wu/videoview3.git

### many_play video (多影片輪播)

        var objs = new Array(
		"http://xxx.xxx.xxx.xxx/xxx.mp4",
		"http://xxx.xxx.xxx.xxx/xxx.mp4",
		"http://xxx.xxx.xxx.xxx/xxx.mp4",
		"http://xxx.xxx.xxx.xxx/xxx.mp4"
	);
		
		VideoPlayer.many_play(
			objs,	//影片列表
			2	// 2:輪播
		);
### many_play video (單一影片一次播)
	var objs = new Array(
		"http://xx.xx.xx.xx/xxx.mp4"
	);
	VideoPlayer.many_play(
		objs,
		1	// 1:只播一次
	);
		
### custom_play video (自訂控制列)
        var objs = new Array(
		"http://xxx.xxx.xxx.xxx/xxx.mp4",
		"http://xxx.xxx.xxx.xxx/xxx.mp4",
		"http://xxx.xxx.xxx.xxx/xxx.mp4",
		"http://xxx.xxx.xxx.xxx/xxx.mp4"
	);
        VideoPlayer.custom_play(objs,2);
