var db = (function() {
	return {
		getTable: function(table, callback) {
			var url = "http://localhost:3000/phpmyadmin/sql.php?printview=1&db=vorbit2&table=" + table;
			$.get(url, function(data) {
				var results = $("#table_results", data);
				callback(results);
			});
		}
	}
})();

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
		setup: function(callback) {
			var count = 0;

			$navItems.each(function() {
				var $this = $(this);
				var name = $("a", this).attr("href").substring(1);
				if (name.length > 0) {
					$.get("pages/" + name + ".html", function(data) {
						var $page = newPage(name);
						$page.append(data);
						if ($this.hasClass(selClass)) { $page.addClass(selClass); }
						$content.append($page);
						if (++count == $navItems.length) { callback(); }
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
				if ($(window).width() > minWidth) {
					window.resizeTo(minWidth, $(window).height());
				}
				$navItems.removeClass(selClass);
				$("a[href=#"+name+"]", $navItems).parent().addClass(selClass);
				$(".page").removeClass(selClass);
				page.addClass(selClass);
				if(name === "data") {
					checkForTableResize();
				}
			}
		}
	}
})();

var minWidth = $(window).width();

function checkForTableResize() {
	var w = $("#table-wrap table").width();
	if (w + 200 > minWidth) {
		window.resizeTo(w + 200, $(window).height());
		// console.log(chrome.app.window);
	} else if ($(window).width() > minWidth) {
		window.resizeTo(minWidth, $(window).height());
	}
}

$(document).ready(function() {

	nav.setup(function() {
		$("#tables").on("change", function() {
			db.getTable($(this).val(), function(table) {
				$("#table-wrap").empty();
				$("#table-wrap").append(table);
				checkForTableResize();
			});
		});
	});

});