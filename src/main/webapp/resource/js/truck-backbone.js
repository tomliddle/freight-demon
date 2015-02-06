// Represents one
var Truck = Backbone.Model.extend({});

// Represents a list
var TruckList = BaseCollection.extend({
	url: '/truck',
	model: Truck
});

var TruckListView = BaseView.extend({
	el: '.content', // el attaches to existing element

	initialize: function(){
		this.collection = new TruckList();
		this.listenTo(this.collection, "add", this.render);
		var that = this;
		this.collection.fetch({success: function(){that.render()}});
	},

	render: function(){
		this.$el.html(Templates.truckListTemplate({trucks:this.collection.toJSON()}));
		return this;
	}
});

