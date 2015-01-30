(function($){

	var Templates = Templates || {
		load: function () {
			//this.imageTemplate= Handlebars.compile($("#image-template").html());
			this.imageListTemplate= Handlebars.compile($("#image-list-template").html());
		}
	};

	// Represents one image
	var Image = Backbone.Model.extend({});

	// Represents a list of images
	var List = Backbone.Collection.extend({
		url: '/image',
		model: Image
	});

	// Because the new features (swap and delete) are intrinsic to each `Item`, there is no need to modify `ListView`.
	var ImageListView = Backbone.View.extend({
		el: '.images', // el attaches to existing element
		tagName: 'ul',

		initialize: function(){
			this.collection = new List();
			//this.listenTo(this.collection, "add", this.appendImage);
			var that = this;
			this.collection.fetch({success: function(){that.render()}});
		},

		render: function(){
			this.$el.html(Templates.imageListTemplate({images:this.collection.toJSON()}));
			return this;
		}
	});

	$(document).ready(function () {
		Templates.load();
		var imageListView = new ImageListView();
	});

	
})(jQuery);



// Represents An image
/*	var ImageView = Backbone.View.extend({
 render: function(){
 this.$el.html(Templates.imageTemplate(this.model.toJSON()));
 return this; // for chainable calls, like .render().el
 }
 });*/
