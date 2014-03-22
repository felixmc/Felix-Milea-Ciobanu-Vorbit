(function() {
	var $page = $("#page-data");

	var $update = $("#update", $update);

	var $table = $("[name=table]", $page);
	var $dataset = $("[name=dataset]", $page);
	var $subset = $("[name=subset]", $page);
	var $edition = $("[name=edition]", $page);
	var $sort = $("[name=sort]", $page);
	var $sortDir = $("[name=sortDir]", $page);
	var $limit = $("[name=limit]", $page);
	var $pageNr = $("[name=page]", $page);

	function hasValue($sel) {
		return !$sel.prop("disabled") && !$sel.find(":selected").prop("disabled");
	}

	function loadCols(table) {
		$sort.find(":not([disabled])").remove();
		dbManager.getTable(table, function(data) {
			var cols = $("thead", data).text().trim().split(/\s/);
			cols.forEach(function(item) {
				$sort.append('<option value="'+item+'">'+item+'</option>');
			});
		});
	}

	$update.on("click", function() {
		if(!hasValue($table)) return;

		var query = "SELECT * FROM `" + $table.val() + "` WHERE 1";

		if(hasValue($dataset))
			query += " AND `dataset`=" + $dataset.val();

		if(hasValue($subset))
			query += " AND `subset`=" + $subset.val();

		if(hasValue($edition))
			query += " AND `edition`=" + $edition.val();

		if(hasValue($sort))
			query += " ORDER BY `" + $sort.val() + "` " + $sortDir.val();
		else
			query += " ORDER BY `id` " + $sortDir.val();

		if(hasValue($limit))
			query += " LIMIT " + ((parseInt($pageNr.val()) - 1) * parseInt($limit.val())) + ", " + $limit.val();

		dbManager.getQueryTable(query, function(table) {
			$("#table-wrap").empty();
			$("#table-wrap").append(table);
			pageManager.checkForTableResize(true);
		});
	});


	$table.on("change", function() {
		var $this = $(this);
		if(!hasValue($this)) return;

		var val = $this.val();

		loadCols(val);

		// disable all params
		$dataset.prop("disabled", true);
		$subset.prop("disabled", true);
		$edition.prop("disabled", true);

		// load params for table
		var params = $this.find(":selected").attr("data-params");
		if (params != undefined) {
			params.split(",").forEach(function(param) {
				$("[name="+param+"]").prop("disabled", false);
			});
		}

		$pageNr.val(1);
	});




})();