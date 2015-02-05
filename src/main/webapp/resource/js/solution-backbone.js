	// Represents one
	var Solution = Backbone.Model.extend({});

	// Represents a list
	var SolutionList = Backbone.Collection.extend({
		url: '/solution',
		model: Solution
	});

	// Because the new features (swap and delete) are intrinsic to each `Item`, there is no need to modify `ListView`.
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

