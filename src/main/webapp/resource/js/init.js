var Templates = Templates || {
	load: function () {
		this.truckListTemplate = Handlebars.compile($("#truck-list-template").html());
		this.stopListTemplate = Handlebars.compile($("#stop-list-template").html());
		this.depotListTemplate = Handlebars.compile($("#depot-list-template").html());

		this.solutionPageTemplate = Handlebars.compile($("#solution-page-template").html());
		this.solutionListTemplate = Handlebars.compile($("#solution-list-template").html());
		//this.solutionMapTemplate = Handlebars.compile($("#solution-map-template").html());
	}
};

var BaseCollection = Backbone.Collection.extend({});

var BaseView = Backbone.View.extend({

	events: {'submit': 'save'},

	initialize: function(options) {
	},

	removeItem: function(id) {
		this.collection.get(id).destroy();
		this.render();
	},

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

		this.collection.create(data);
		return false;
	}

});


// ******************************* ROUTER **************************************
var AppRouter = Backbone.Router.extend({
	routes: {
		trucks: "truckListView",
		depots: "depotListView",
		stops: "stopListView",
		solution: "solutionPageView",
		"*actions": "defaultRoute" // Backbone will try to match the route above first
	},

	defaultRoute: function() {

	},

	truckListView: function() {
		this.view && this.view.close();
		this.view = new TruckListView();
	},

	depotListView: function() {
		this.view && this.view.close();
		this.view = new DepotListView();
	},

	stopListView: function() {
		this.view && this.view.close();
		this.view = new StopListView();
	},

	solutionPageView: function() {
		this.view && this.view.close();
		this.view = new SolutionPageView();
	}
});


$(document).ready(function () {
	Templates.load();
	var app_router = new AppRouter;
	Backbone.history.start();
});
