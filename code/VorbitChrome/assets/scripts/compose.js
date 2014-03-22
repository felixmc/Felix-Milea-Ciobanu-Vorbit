(function() {
	var $page = $("#page-compose");
	var $results = $("#compose-result", $page);
	var $compose = $("#compose", $page);
	var $clear = $("#clear", $page);

	$compose.on("click", function() {
		if(!$compose.hasClass("inactive")) {
			$compose.addClass("inactive");
			$.get(serverManager.uri("compose"), { n: $("[name=n]", $page).val(), dataset: $("[name=dataset]", $page).val(),
				subset: $("[name=subset]", $page).val(), edition: $("[name=edition]", $page).val() }, function(data) {
				$results.prepend('<div class="comment">'+ data +'</div>');
				$compose.removeClass("inactive")
			});
		}
	});

	$clear.on("click", function() {
		$results.empty();
	});

})();