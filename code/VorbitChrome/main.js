chrome.app.runtime.onLaunched.addListener(function() {
	var screenWidth = screen.availWidth;
	var screenHeight = screen.availHeight;
	var width = 900;
	var height = 512;

	chrome.app.window.create('main.html', {
		bounds: {
			width: width,
			height: height,
			left: Math.round((screenWidth-width)/2),
			top: Math.round((screenHeight-height)/2)
		},
		resizable: false
	});
});