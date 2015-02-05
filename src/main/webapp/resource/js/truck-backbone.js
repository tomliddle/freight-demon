	// Represents one
	var Truck = Backbone.Model.extend({});

	// Represents a list
	var TruckList = Backbone.Collection.extend({
		url: '/truck',
		model: Truck
	});

	// Because the new features (swap and delete) are intrinsic to each `Item`, there is no need to modify `ListView`.
	var TruckListView = BaseView.extend({
		el: '.content', // el attaches to existing element

		initialize: function(){
			this.collection = new TruckList();
			//this.listenTo(this.collection, "add", this.appendImage);
			var that = this;
			this.collection.fetch({success: function(){that.render()}});
		},

		render: function(){
			this.$el.html(Templates.truckListTemplate({trucks:this.collection.toJSON()}));
			return this;
		}
	});

