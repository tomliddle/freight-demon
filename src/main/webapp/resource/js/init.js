	var Templates = Templates || {
		load: function () {
			this.truckListTemplate = Handlebars.compile($("#truck-list-template").html());
			this.stopListTemplate = Handlebars.compile($("#stop-list-template").html());
			this.depotListTemplate= Handlebars.compile($("#depot-list-template").html());
			this.solutionTemplate= Handlebars.compile($("#solution-template").html());
		}
	};

	var BaseView = Backbone.View.extend({

		events: {'submit': 'save'},

		close: function() {
			this.$el.empty();
			this.unbind();
		},

		save: function(e) {
			e.preventDefault();
			var arr = this.$el.find("form").serializeArray();
			var data = _(arr).reduce(function(acc, field) {
				acc[field.name] = field.value;
				return acc;
			}, {});
			this.model.save(data);
			return false;
		}

	});


	// ******************************* ROUTER **************************************
	var AppRouter = Backbone.Router.extend({
		routes: {
			trucks: "truckListView",
			depots: "depotListView",
			stops: "stopListView",
			solution: "solutionView",
			"*actions": "defaultRoute" // Backbone will try to match the route above first
		},

		defaultRoute: function() {

		},

		truckListView: function() {
			this.switchView(new TruckListView(), "truckListView")
		},

		depotListView: function() {
			this.switchView(new DepotListView(), "depotListView")
		},

		stopListView: function() {
			this.switchView(new StopListView(), "stopListView")
		},

		solutionView: function() {
			this.switchView(new SolutionView(), "solutionView")
		},

		switchView: function (view, name) {
			// Close the old view
			this.view && this.view.close();
			this.view = view;

			//$("ul.nav li").removeClass("active");
			//$("ul.nav a[href$='" + name + "']").closest("li").addClass("active");


		}
	});





	$(document).ready(function () {
		Templates.load();
		var app_router = new AppRouter;
		Backbone.history.start();
		//var truckListView = new TruckListView();
		//var stopListView = new StopListView();
		//var depotListView = new DepotListView();
	});
