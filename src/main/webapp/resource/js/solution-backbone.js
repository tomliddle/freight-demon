var Solution = Backbone.Model.extend({});

var SolutionList = Backbone.Collection.extend({
	url: '/solution',
	model: Solution
});

var SolutionPageView = Backbone.View.extend({
	el: '.content',

	initialize: function(){
		this.collection = new SolutionList();
		this.solutionMapView = new SolutionMapView();
		this.solutionListView = new SolutionListView({
			solutions:this.collection,
			mapView:this.solutionMapView
		});

		var that = this;
		this.collection.fetch({success: function(){that.render()}});

	},

	render: function(){
		this.$el.html(Templates.solutionPageTemplate());

		this.$el.find(".solution-list").html(this.solutionListView.render().el);
		this.solutionMapView.render();
		return this;
	}
});

var SolutionListView = BaseView.extend({

	initialize: function(options){
		this.collection = options.solutions;
		this.mapView = options.mapView;
		this.listenTo(this.collection, "add", this.render);
		var that = this;
		//this.collection.fetch({success: function(){that.render()}});
	},

	render: function(){
		this.$el.html(Templates.solutionListTemplate({solution:this.collection.toJSON()}));
		this.$el.find("solution-select").click(function() {
			this.mapView.drawSolution()
		});
		return this;
	}
});


var SolutionMapView = Backbone.View.extend({

	initialize: function(options){
		//this.listenTo()
	},

	render: function(){
		this.map = L.map('map').setView([51.505, -0.09], 13);
		L.tileLayer('http://{s}.tile.osm.org/{z}/{x}/{y}.png?{foo}', {foo: 'bar'}).addTo(this.map);
		return this;
	},

	drawSolution: function(solution) {

		for (var i = 0; i < trucks.size; i++) {

		}
	}



});




