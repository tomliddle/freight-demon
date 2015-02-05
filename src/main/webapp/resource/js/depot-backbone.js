	var Depot = Backbone.Model.extend({});

	var DepotList = BaseCollection.extend({
		url: '/depot',
		model: Depot
	});

	var DepotListView = Backbone.View.extend({
		el: '#depots',

		initialize: function(){
			this.collection = new DepotList();
			var that = this;
			this.collection.fetch({success: function(){that.render()}});
		},

		render: function(){
			this.$el.html(Templates.depotListTemplate({depots:this.collection.toJSON()}));
			return this;
		}
	});

