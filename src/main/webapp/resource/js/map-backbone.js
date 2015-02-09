var Map = Backbone.Model.extend({});

var SolutionList = Backbone.Collection.extend({
	url: '/solution',
	model: Solution
});

var SolutionView = BaseView.extend({
	el: '.content',


	initialize: function(){
		this.collection = new SolutionList();
		this.listenTo(this.collection, "add", this.render);
		var that = this;
		this.collection.fetch({success: function(){that.render()}});
	},

	render: function(){
		this.$el.html(Templates.solutionTemplate({solution:this.collection.toJSON()}));

		var map = L.map('map').setView([51.505, -0.09], 13);
		L.tileLayer('http://{s}.tile.osm.org/{z}/{x}/{y}.png?{foo}', {foo: 'bar'}).addTo(map);

		var marker = L.marker([51.5, -0.09]).addTo(map);
		return this;
	}
});

