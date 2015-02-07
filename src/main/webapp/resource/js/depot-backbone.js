var Depot = Backbone.Model.extend({});

var DepotList = BaseCollection.extend({
	url: '/depot',
	model: Depot
});

var DepotListView = BaseView.extend({
	el: '.content',

	initialize: function(){
		this.collection = new DepotList();
		this.listenTo(this.collection, "add", this.render);
		var that = this;
		this.collection.fetch({success: function(){that.render()}});
	},

	render: function(){
		this.$el.html(Templates.depotListTemplate({depots:this.collection.toJSON()}));
		return this;
	}
});

