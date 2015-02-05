
var Solution = Backbone.Model.extend({});

var SolutionList = Backbone.Collection.extend({
	url: '/solution',
	model: Solution
});

var SolutionView = BaseView.extend({
	el: '.content', // el attaches to existing element

	initialize: function(){
		this.collection = new SolutionList();
		var that = this;
		this.collection.fetch({success: function(){that.render()}});
	},

	render: function(){
		this.$el.html(Templates.solutionTemplate({solution:this.collection.toJSON()}));
		return this;
	}
});

