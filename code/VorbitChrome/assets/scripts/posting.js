var postingManager = (function() {
	var $posterSeed = $('<div class="poster item seed"><span class="name"></span></div>');
	var $list = $(".poster-list");
	var $details = $(".poster-details");
	var $content = $("#content");
	var $form = $("#page-posting #config-form");
	var $postCount = $("#post-count");

	function loadPosters() {
		$.each(serverManager.data.posters, function(index, val) {
			var $poster = $posterSeed.clone();

			$poster.removeClass("seed");
			$(".name", $poster).text(val.name);

			$list.append($poster);
		});		
	}

	var updateHeader = function (forced) {
		var query = "SELECT COUNT(*) FROM `posted_content`";
		if ($details.hasClass("running") || forced){
			dbManager.getQueryTable(query, function(data) {
				var count = parseInt($("td", data).text());
				$postCount.text(count.toLocaleString());
				setTimeout(function () { updateHeader() }, 1000);
			});
		}
	};

	app.onLoad(function() {
		updateHeader(true);
		loadPosters();	
	});

	$list.on("click", ".poster", function() {
		var $this = $(this);

		if(!$list.hasClass("inactive") && !$this.hasClass("selected")) {
			$(".poster").removeClass("selected");
			$this.addClass("selected");

			if($this.hasClass("new")) {
				$details.addClass("new");
				configManager.switchTo();
				$("[name=pname]", $form).prop("disabled", false);
			} else {
				$details.removeClass("new");
				var name = $(".name", $this).text();
				configManager.switchTo(name);
				$("[name=pname]", $form).prop("disabled", true);
			}
		}
	});

	$content.on("click",  ".poster-details:not(.running) .icon-save", function () {	
		var $this = $(this);
		$this.addClass("inactive");
		$list.addClass("inactive");
		$("input, select, form button", $details).prop("disabled", true);

		configManager.persist(configManager.serialize(), function(data) {
			$list.removeClass("inactive");
			$this.removeClass("inactive");
			$(".tasks input, select, form button", $details).prop("disabled", false);

			if($details.hasClass("new")) {
				var $poster = $posterSeed.clone();

				$poster.removeClass("seed");
				$(".name", $poster).text($("[name=pname]", $form).val());

				$list.append($poster);

				configManager.deserialize();
				$("[name=dataset]", $form).prop("disabled", false);
				serverManager.reloadData();
			}
		});

		return false;
	});

	$content.on("click", ".poster-details:not(.new, .running) .icon-delete", function() {
		var id = $(".poster.selected .name").text();
		$(".poster.selected").remove();

		$.get(serverManager.uri("posters/delete"), { poster: id }, function(data) {
			console.log(data);
			$(".poster.new", $list).click();
			configManager.deserialize();
			$("[name=pname]", $form).prop("disabled", false);
			serverManager.reloadData();
		});

		return false;
	});

	$content.on("click", ".poster-details:not(.new, .running) .icon-start", function() {
		$details.addClass("running");
		$list.addClass("inactive");
		var id = $(".poster.selected .name").text();

		updateHeader();

		$.get(serverManager.uri("posters/start"), { poster: id }, function(data) {
			console.log(data);
		});

		$("input, select, form button", $details).prop("disabled", true);

		return false;
	});

	$content.on("click", ".poster-details.running .icon-stop", function() {
		$details.removeClass("running");
		$list.removeClass("inactive");

		$.get(serverManager.uri("posters/stop"), function(data) {
			console.log(data);
		});

		$(".tasks input, select, form button", $details).prop("disabled", false);

		return false;
	});


	var configManager = (function() {
		var configCache = {
			newConfig: {"name":"","active":true,"corpus":{"dataset":"","subset":"","edition":"","n":4},"tasks":[{"name":"","recurrence":0,"postType":"both","postSort":"new","time":"all","postListings":5,"postLimit":25,"targets":[{"subreddits":[""],"constraints":[{"minKarma":0,"maxAge":0}]}]}]},

		};
		var views = {};

		var names = ["posting-task", "posting-target", "constraint"]
		names.forEach(function(val, i) {
			$.get("partials/" + val + ".html", function(data) {
				views[val] = $(data);
				if (Object.keys(views).length == names.length) {
					result.init();
				}
			});
		});

		function newConstraint() {
			var $cc = views["constraint"].clone();
			return $cc;
		}

		function newTarget() {
			var $target = views["posting-target"].clone();
			$target.append(newConstraint());
			return $target;		
		}

		function newTask() {
			var $task = views["posting-task"].clone();
			$task.append(newTarget());
			return $task;
		}

		$form.on("click", ".remove", function () {
			$(this).parent().remove();
			return false;
		});

		$form.on("click", "#add-task", function () {
			var $this = $(this);
			$this.after( newTask() );
			return false;
		});

		$form.on("click", "#add-target", function () {
			var $this = $(this);
			$this.after( newTarget() );
			return false;
		});

		$form.on("click", "#add-constraint", function () {
			var $this = $(this);
			$this.after( newConstraint() );
			return false;
		});


		var result = {
			init: function() {
				this.deserialize();
			},
			switchTo: function(configName) {
				this.serialize();
				this.deserialize(configName);
			},
			loadConfig: function (id, callback) {
				$.get(serverManager.uri("posters/config"), {poster: id}, function(data) {
					configCache[data.dataset] = data;
					callback(data);
				});
			},
			uncache: function(configName) {
				configCache[configName] = undefined;
			},
			serialize: function () {
				var config = {};

				config.name = $("[name=pname]", $form).val();
				config.active = $("[name=active]", $form).val();
				config.corpus = {};
				config.corpus.dataset = $("[name=dataset]", $form).find(":selected").text();
				config.corpus.subset = $("[name=subset]", $form).find(":selected").text();
				config.corpus.edition = $("[name=edition]", $form).find(":selected").text();
				config.corpus.n = parseInt($("[name=n]", $form).val());

				config.tasks = [];

				$(".task", $form).each(function(taskI, taskEl) {
					var $task = $(taskEl);
					var task = {};

					task.name = $("[name=name]", $task).val();
					task.recurrence = parseInt($("[name=recurrence]", $task).val());
					task.postType = $("[name=postType]", $task).val();
					task.postSort = $("[name=postSort]", $task).val();
					task.time = $("[name=time]", $task).val();
					task.postListings = parseInt($("[name=postListings]", $task).val());
					task.postLimit = parseInt($("[name=postLimit]", $task).val());
					task.targets = [];

					$(".target", $task).each(function(targetI, targetEl) {
						var $target = $(targetEl);
						var target = {};
						target.subreddits = $("[name=subreddits]", $target).val().split(/[\s,]+/);
						target.constraints = [];

						$(".constraint", $target).each(function(ccIm, ccEl) {
							var $cc = $(ccEl);
							var cc = {};

							cc.minKarma = parseInt($("[name=minKarma]", $cc).val());
							cc.maxAge = parseInt($("[name=maxAge]", $cc).val());

							target.constraints.push(cc);
						});

						task.targets.push(target);
					});

					config.tasks.push(task);
				});

				configCache[config.name] = config;
				return config;
			},
			getConfig: function(configName, callback) {
				var config = undefined;

				if (configName === undefined) {
					callback(configCache.newConfig);
				} else {
					if (configCache[configName] === undefined) {
						this.loadConfig(configName, callback);
					} else {
						callback(configCache[configName]);
					}
				}
			},
			deserialize: function (configName) {
				var self = this;
				this.getConfig(configName, function(config) {
					self.clear();

					$("[name=pname]", $form).val(config.name);
					$("[name=active]", $form).val("true");
					$("[name=dataset]", $form).val(serverManager.getId("miners", config.corpus.dataset));
					$("[name=subset]", $form).val(serverManager.getId("subsets", config.corpus.subset));
					$("[name=edition]", $form).val(serverManager.getId("editions", config.corpus.edition));
					$("[name=n]", $form).val(config.corpus.n);


					config.tasks.forEach(function(task, taskIndex) {
						var $task = views["posting-task"].clone();

						$("[name=name]", $task).val(task.name);
						$("[name=recurrence]", $task).val(task.recurrence);
						$("[name=postType]", $task).val(task.postType);
						$("[name=postSort]", $task).val(task.postSort);
						$("[name=time]", $task).val(task.time);
						$("[name=postListings]", $task).val(task.postListings);
						$("[name=postLimit]", $task).val(task.postLimit);

						task.targets.forEach(function(target, targetIndex) {
							var $target = views["posting-target"].clone();
							$("[name=subreddits]", $target).val(target.subreddits.join());
						
							target.constraints.forEach(function(cc, ccIndex) {
								$cc = views["constraint"].clone();

								$("[name=minKarma]", $cc).val(cc.minKarma);
								$("[name=maxAge]", $cc).val(cc.maxAge);

								$(".constraints", $target).append($cc);
							});

							$(".targets", $task).append($target);
						});

						$(".tasks", $form).append($task);
					});
				});
			},
			persist: function (config, callback) {
				$.post(serverManager.uri("posters/update"), JSON.stringify(config), function(data) {
					callback(data);
				});
			},
			clear: function () {
				$("fieldset", $form).remove();
				$("[name=pname]").val("");
				$("[name=active]").prop("checked", true);
				$("[name=dataset]").val("");
				$("[name=subset]").val("");
				$("[name=edition]").val("");
				$("[name=n]").val("");
			}
		}

		return result;
	})();

})();