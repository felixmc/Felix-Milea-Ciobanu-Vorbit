var miningManager = (function() {
	var $minerSeed = $('<div class="miner item seed" data-miner-id="0"><span class="name"></span></div>');
	var $list = $(".miner-list");
	var $details = $(".miner-details");
	var $content = $("#content");
	var $form = $("#page-mining #config-form");
	var $mineCount = $("#mine-count");

	function loadMiners() {

		$.each(serverManager.data.miners, function(index, val) {
			var $miner = $minerSeed.clone();

			$miner.removeClass("seed");
			$miner.attr("data-miner-id", val.id);
			$(".name", $miner).text(val.name);

			$list.append($miner);
		});		
	}

	var updateHeader = function (forced) {
		var query = "SELECT COUNT(*) FROM `reddit_corpus`";
		if ($details.hasClass("running") || forced){
			dbManager.getQueryTable(query, function(data) {
				var count = parseInt($("td", data).text());
				$mineCount.text(count.toLocaleString());
				setTimeout(function () { updateHeader() }, 1000);
			});
		}
	};

	app.onLoad(function() {
		updateHeader(true);
		loadMiners();	
	});

	$list.on("click", ".miner", function() {
		var $this = $(this);

		if(!$list.hasClass("inactive") && !$this.hasClass("selected")) {
			$(".miner").removeClass("selected");
			$this.addClass("selected");

			if($this.hasClass("new")) {
				$details.addClass("new");
				configManager.switchTo();
				$("[name=dataset]", $form).prop("disabled", false);
			} else {
				$details.removeClass("new");
				var name = $(".name", $this).text();
				configManager.switchTo(name, $this.attr("data-miner-id"));
				$("[name=dataset]", $form).prop("disabled", true);
			}
		}
	});

	$content.on("click",  ".miner-details:not(.running) .icon-save", function () {	
		var $this = $(this);
		$this.addClass("inactive");
		$list.addClass("inactive");
		$("input, select, form button", $details).prop("disabled", true);

		configManager.persist(configManager.serialize(), function(data) {
			$list.removeClass("inactive");
			$this.removeClass("inactive");
			$(".tasks input, select, form button", $details).prop("disabled", false);

			if($details.hasClass("new")) {
				var $miner = $minerSeed.clone();

				$miner.removeClass("seed");
				$miner.attr("data-miner-id", data.id);
				$(".name", $miner).text($("[name=dataset]", $form).val());

				$list.append($miner);

				configManager.deserialize();
				$("[name=dataset]", $form).prop("disabled", false);
				serverManager.reloadData();
			}
		});

		return false;
	});

	$content.on("click", ".miner-details:not(.new, .running) .icon-delete", function() {
		var id = $(".miner.selected").attr("data-miner-id");
		$(".miner.selected").remove();

		$.get(serverManager.uri("miners/delete"), { miner: id }, function(data) {
			console.log(data);
			$(".miner.new", $list).click();
			configManager.deserialize();
			$("[name=dataset]", $form).prop("disabled", false);
			serverManager.reloadData();
		});

		return false;
	});

	$content.on("click", ".miner-details:not(.new, .running) .icon-start", function() {
		$details.addClass("running");
		$list.addClass("inactive");
		var id = $(".miner.selected").attr("data-miner-id");

		updateHeader();

		$.get(serverManager.uri("miners/start"), { miner: id }, function(data) {
			console.log(data);
		});

		$("input, select, form button", $details).prop("disabled", true);

		return false;
	});

	$content.on("click", ".miner-details.running .icon-stop", function() {
		$details.removeClass("running");
		$list.removeClass("inactive");

		$.get(serverManager.uri("miners/stop"), function(data) {
			console.log(data);
		});

		$(".tasks input, select, form button", $details).prop("disabled", false);

		return false;
	});


	var configManager = (function() {
		var configCache = {
			newConfig: {"dataset":"","active":true,"tasks":[{"name":"","recurrence":0,"targetType":"subreddit","postType":"both","parsePostContent":false,"postSort":"top","time":"all","postListings":5,"postLimit":25,"commentSort":"top","commentNesting":0,"targets":[{"units":[],"commentConstraints":[{"minKarma":0,"minGild":0,"maxAge":0}],"postConstraints":[{"minKarma":0,"maxAge":0}]}]}]},

		};
		var views = {};

		var names = ["mining-task", "mining-target", "post-constraint", "comment-constraint"]
		names.forEach(function(val, i) {
			$.get("partials/" + val + ".html", function(data) {
				views[val] = $(data);
				if (Object.keys(views).length == names.length) {
					result.init();
				}
			});
		});

		function newCommentConstraint() {
			var $cc = views["comment-constraint"].clone();
			return $cc;
		}

		function newPostConstraint() {
			var $pc = views["post-constraint"].clone();
			return $pc;
		}

		function newTarget() {
			var $target = views["mining-target"].clone();
			$("#add-cs", $target).after(newCommentConstraint());
			$target.append(newPostConstraint());
			return $target;		
		}

		function newTask() {
			var $task = views["mining-task"].clone();
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

		$form.on("click", "#add-cs", function () {
			var $this = $(this);
			$this.after( newCommentConstraint() );
			return false;
		});

		$form.on("click", "#add-ps", function () {
			var $this = $(this);
			$this.after( newPostConstraint() );
			return false;
		});


		var result = {
			init: function() {
				this.deserialize();
			},
			switchTo: function(configName, id) {
				this.serialize();
				this.deserialize(configName, id);
			},
			loadConfig: function (id, callback) {
				$.get(serverManager.uri("miners/config"), {miner: id}, function(data) {
					configCache[data.dataset] = data;
					callback(data);
				});
			},
			uncache: function(configName) {
				configCache[configName] = undefined;
			},
			serialize: function () {
				var config = {};

				config.dataset = $("[name=dataset]", $form).val();
				config.active = $("[name=active]", $form).val();
				config.tasks = [];

				$(".task", $form).each(function(taskI, taskEl) {
					var $task = $(taskEl);
					var task = {};

					task.name = $("[name=name]", $task).val();
					task.recurrence = parseInt($("[name=recurrence]", $task).val());
					task.targetType = $("[name=targetType]", $task).val();
					task.postType = $("[name=postType]", $task).val();
					task.postSort = $("[name=postSort]", $task).val();
					task.parsePostContent = $("[name=parsePostContent]", $task).prop("checked");
					task.time = $("[name=time]", $task).val();
					task.postListings = parseInt($("[name=postListings]", $task).val());
					task.postLimit = parseInt($("[name=postLimit]", $task).val());
					task.commentSort = $("[name=commentSort]", $task).val();
					task.commentNesting = parseInt($("[name=commentNesting]", $task).val());
					task.targets = [];

					$(".target", $task).each(function(targetI, targetEl) {
						var $target = $(targetEl);
						var target = {};
						target.units = $("[name=units]", $target).val().split(/[\s,]+/);
						target.commentConstraints = [];
						target.postConstraints = [];

						$(".commentConstraint", $target).each(function(ccIm, ccEl) {
							var $cc = $(ccEl);
							var cc = {};

							cc.minKarma = parseInt($("[name=minKarma]", $cc).val());
							cc.minGild = parseInt($("[name=minGild]", $cc).val());
							cc.maxAge = parseInt($("[name=maxAge]", $cc).val());

							target.commentConstraints.push(cc);
						});

						$(".postConstraint", $target).each(function(pcIm, pcEl) {
							var $pc = $(pcEl);
							var pc = {};

							pc.minKarma = parseInt($("[name=minKarma]", $pc).val());
							pc.maxAge = parseInt($("[name=maxAge]", $pc).val());

							target.postConstraints.push(pc);
						});

						task.targets.push(target);
					});

					config.tasks.push(task);
				});

				configCache[config.dataset] = config;
				return config;
			},
			getConfig: function(configName, id, callback) {
				var config = undefined;

				if (configName === undefined) {
					callback(configCache.newConfig)
				} else {
					if (configCache[configName] === undefined) {
						this.loadConfig(id, callback);
					} else {
						callback(configCache[configName]);
					}
				}
			},
			deserialize: function (configName, id) {
				var self = this;
				this.getConfig(configName, id, function(config) {
					self.clear();

					$("[name=dataset]", $form).val(config.dataset);
					$("[name=active]", $form).val("true");

					config.tasks.forEach(function(task, taskIndex) {
						var $task = views["mining-task"].clone();

						$("[name=name]", $task).val(task.name);
						$("[name=recurrence]", $task).val(task.recurrence);
						$("[name=postType]", $task).val(task.postType);
						$("[name=targetType]", $task).val(task.targetType);
						$("[name=postSort]", $task).val(task.postSort);
						$("[name=parsePostContent]", $task).prop("checked", task.parsePostContent);
						$("[name=time]", $task).val(task.time);
						$("[name=postListings]", $task).val(task.postListings);
						$("[name=postLimit]", $task).val(task.postLimit);
						$("[name=commentSort]", $task).val(task.commentSort);
						$("[name=commentNesting]", $task).val(task.commentNesting);

						task.targets.forEach(function(target, targetIndex) {
							var $target = views["mining-target"].clone();
							$("[name=units]", $target).val(target.units.join());
						
							target.commentConstraints.forEach(function(cc, ccIndex) {
								$cc = views["comment-constraint"].clone();

								$("[name=minKarma]", $cc).val(cc.minKarma);
								$("[name=minGild]", $cc).val(cc.minGild);
								$("[name=maxAge]", $cc).val(cc.maxAge);

								$(".commentConstraints", $target).append($cc);
							});

							target.postConstraints.forEach(function(pc, pcIndex) {
								$pc = views["post-constraint"].clone();

								$("[name=minKarma]", $pc).val(pc.minKarma);
								$("[name=maxAge]", $pc).val(pc.maxAge);

								$(".postConstraints", $target).append($pc);
							});

							$(".targets", $task).append($target);
						});

						$(".tasks", $form).append($task);
					});
				});
			},
			persist: function (config, callback) {
				$.post(serverManager.uri("miners/update"), JSON.stringify(config), function(data) {
					callback(data);
				});
			},
			clear: function () {
				$("fieldset", $form).remove();
				$("[name=dataset]").val("");
				$("[name=active]").prop("checked", true);
			}
		}

		return result;
	})();

})();