var serverManager = (function() {
	var address = "http://localhost:5000/";
	var nodes = ["miners", "posters", "subsets", "editions"];

	return {
		loadData: function(callback) {
			var count = 0;
			var self = this;
			nodes.forEach(function(node, i) {
				$.get(self.uri(node), function(data) {
					self.data[node] = data;
					if (++count == nodes.length) { callback(); }
				});
			});
		},
		reloadData: function() {
			var self = this;
			nodes.forEach(function(node) {
				$.get(self.uri(node), function(data) {
					self.data[node] = data;
					self.updateSelect(node);
				});
			});	
		},
		updateSelects: function(name) {
			var self = this;
			nodes.forEach(function(node) {
				self.updateSelect(node);
			});
		},
		updateSelect: function(name) {
			var $list = $("." + name + "-list");
			$("option:not([disabled])", $list).remove();
			this.data[name].forEach(function(item) {
				$list.append('<option value="'+item.id+'">' + item.name + '</option>');
			});
		},
		uri: function(path) { return address + path; },
		data: {},
		getId: function(type, name) {
			var result = 0;
			this.data[type].forEach(function(val) {
				if(val.name == name) {
					console.log(val.id);
					result = val.id;
				}
			});
			return result;
		}
	}
})();

var dbManager = (function() {
	var base = "http://localhost:3000/phpmyadmin/sql.php?printview=1&db=vorbit2";
	return {
		getTable: function(table, callback) {
			var url = base + "&table=" + table;
			$.get(url, function(data) {
				var results = $("#table_results", data);
				callback(results);
			});
		},
		getQueryTable: function(query, callback) {
			var escapedQuery = encodeURIComponent(query);
			$.get(base + "&sql_query="+escapedQuery, function(data) {
				var results = $("#table_results", data);
				callback(results);
			});
		}
	}
})();

var pageManager = (function() {
	var $navItems = $("#main-nav li:not(.logo)");
	var $content = $("#content");
	var selClass = "selected";
	var newPage = function(name) {
		var $page = $('<div class="page"></div>');
		$page.attr("id", "page-" + name);
		return $page;
	}

	var minPageWidth = $(window).width();

	var mgr = {
		setup: function(callback) {
			var count = 0;
			$navItems.each(function() {
				var $this = $(this);
				var name = $("a", this).attr("href").substring(1);
				if (name.length > 0) {
					$.get("pages/" + name + ".html", function(data) {
						var $page = newPage(name);

						// load page
						$page.append(data);
						if ($this.hasClass(selClass)) { $page.addClass(selClass); }
						$content.append($page);

						// load scripts
						var script   = document.createElement("script");
						script.src   = "assets/scripts/"+name+".js";    // use this for linked script
						document.body.appendChild(script);

						// check if done loading
						if (++count == $navItems.length) { callback(); }
					});
				}
			});

			var self = this;

			$("a", $navItems).on("click", function() {
				self.changePage($(this).attr("href").substring(1));
				return false;
			});
		},
		changePage: function(name) {
			var page = $("#page-" + name);
			if (page.length > 0 && !page.hasClass(selClass)) {
				$navItems.removeClass(selClass);
				$("a[href=#"+name+"]", $navItems).parent().addClass(selClass);
				$(".page").removeClass(selClass);
				page.addClass(selClass);
				this.checkForTableResize();
			}
		},
		checkForTableResize: function(isDataPage) {
			var w = $("#table-wrap table").width();
			var offset = isDataPage ? 500 : 200;
			if (w + offset > minPageWidth) {
				window.resizeTo(w + offset, $(window).height());
				// console.log(chrome.app.window);
			} else if ($(window).width() > minPageWidth) {
				window.resizeTo(minPageWidth, $(window).height());
			}
		}
	}

	return mgr;
})();


var app = (function() {
	function setupStaticListeners() {
		$("#titleBar .close").on("click", function() {
			window.close();
		});
	}

	function setupDynamicListeners() {

	}

	var loaded = false;
	var loadQueue = [];

	return {
		main: function() {
			setupStaticListeners();

			serverManager.loadData(function() {
				pageManager.setup(function() {
					loaded = true;
					loadQueue.forEach(function(callback) {
						callback();
					});

					serverManager.updateSelects();
					setupDynamicListeners();
				});
			});
		},
		onLoad: function(callback) {
			if (loaded) {
				callback();
			} else {
				loadQueue.push(callback);
			}
		}
	}
})();

$(document).ready(app.main);