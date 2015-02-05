	var Stop = Backbone.Model.extend({});

	var StopList = BaseCollection.extend({
		url: '/stop',
		model: Stop
	});

	var StopListView = BaseView.extend({
		el: '.content',

		initialize: function(){
			this.collection = new StopList();
			var that = this;
			this.collection.fetch({success: function(){that.render()}});
		},

		render: function(){
			this.$el.html(Templates.stopListTemplate({stops:this.collection.toJSON()}));
			return this;
		}
	});

