chrome.app.runtime.onLaunched.addListener(function() {
	var screenWidth = screen.availWidth;
	var screenHeight = screen.availHeight;
	var w = 900;
	var h = 900;

	chrome.app.window.create('main.html', {
		bounds: {
			width: w,
			height: h,
			left: Math.round((screenWidth-w)/2),
			top: Math.round((screenHeight-h)/2)
		},
		frame: 'none'
	});
});