var Solution = Backbone.Model.extend({});

var SolutionList = Backbone.Collection.extend({
	url: '/solution',
	model: Solution
});

var SolutionListView = BaseView.extend({


	initialize: function(){
		this.collection = new SolutionList();
		this.listenTo(this.collection, "add", this.render);
		var that = this;
		this.collection.fetch({success: function(){that.render()}});
	},

	render: function(){
		this.$el.html(Templates.solutionTemplate({solution:this.collection.toJSON()}));
		return this;
	}
});





var SolutionPageView = Backbone.View.extend{{
	el: '.content',

	initialize: function(){
		var that = this;
		//this.collection.fetch({success: function(){that.render()}});
	},

	render: function(){
		this.$el.html(Templates.solutionPageTemplate({}));

		this.$el.find(".solutionlist").html(templage...)

		this.$el.html(Templates.eventViewTemplate());
		return this;
	}
}};

