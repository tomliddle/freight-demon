var Stop = Backbone.Model.extend({});

var StopList = BaseCollection.extend({
	url: '/stop',
	model: Stop
});

var StopListView = BaseView.extend({
	el: '.content',

	initialize: function(){
		this.collection = new StopList();
		this.listenTo(this.collection, "add", this.render);
		var that = this;
		this.collection.fetch({success: function(){that.render()}});
	},

	render: function(){
		this.$el.html(Templates.stopListTemplate({stops:this.collection.toJSON()}));
		var that = this;
		this.$el.find("input.remove").click(function (e) {
			e.preventDefault();
			var id = $(e.target).data("id");
			that.remove(id);
		});
		return this;
	}
});

