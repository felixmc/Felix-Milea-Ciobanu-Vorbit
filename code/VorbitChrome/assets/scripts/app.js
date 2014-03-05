// var dbManager = {
// 	token: null,
// 	loadToken: function(doneCallback) {
// 		if (this.token === null) {
// 			$.get("http://localhost:3000/phpmyadmin/main.php", function(data) {
// 				this.token = $("a:eq(2)", data).attr("href").split("&token=")[1];
// 				console.log("load token: " + this.token);
// 				doneCallback(this.token);
// 			});
// 		} else {
// 			doneCallback(this.token);
// 		}
// 	},
// 	getTable: function(query, callback) {
// 		this.loadToken(function(token) {
// 			var encodedQuery = encodeURIComponent(query).replace(/%20/g, "+");
// 			var url = "http://localhost:3000/phpmyadmin/sql.php?db=vorbit2&printview=1&sql_query=" + encodedQuery + "&token=" + token;
// 			console.log("url: " + url);

// 			$.get(url, function(data) {
// 				var results = $("#table_results", data);
// 				console.log(data);
// 				callback(results, url);
// 			});
// 		});
// 	}
// }

var nav = (function() {
	var $navItems = $("#main-nav li:not(.logo)");
	var $content = $("#content");
	var selClass = "selected";
	var newPage = function(name) {
		var $page = $('<div class="page"></div>');
		$page.attr("id", "page-" + name);
		return $page;
	}

	return {
		setup: function() {
			$navItems.each(function() {
				var $this = $(this);
				var name = $("a", this).attr("href").substring(1);
				if (name.length > 0) {
					$.get("pages/" + name + ".html", function(data) {
						var $page = newPage(name);
						$page.append(data);
						if ($this.hasClass(selClass)) { $page.addClass(selClass); }
						$content.append($page);
					});
				}
			});

			var self = this;

			$("a", $navItems).on("click", function() {
				self.changePage($(this).attr("href").substring(1));
			});
		},
		changePage: function(name) {
			var page = $("#page-" + name);
			if (page.length > 0 && !page.hasClass(selClass)) {
				$navItems.removeClass(selClass);
				$("a[href=#"+name+"]", $navItems).parent().addClass(selClass);
				$(".page").removeClass(selClass);
				page.addClass(selClass);
			}
		}
	}
})();

$(document).ready(function() {

	nav.setup();

});