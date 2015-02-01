(function($){

	var Image = Backbone.Model.extend({});

	var List = Backbone.Collection.extend({
		url: '/image/get',
		model: Image
	});

	var ImageView = Backbone.View.extend({
		tagName: 'li', // name of tag to be created

		initialize: function(){
		},

		render: function(){
			var url = this.model.url();
			$(this.el).html("<image width='100' height='100' src='" + url + "'/>");
			return this; // for chainable calls, like .render().el
		},

		unrender: function(){
			$(this.el).remove();
		}
	});


	// Because the new features (swap and delete) are intrinsic to each `Item`, there is no need to modify `ListView`.
	var ImageListView = Backbone.View.extend({
		el: '.images', // el attaches to existing element
		tagName: 'ul',

		initialize: function(){
			this.collection = new List();
			this.collection.bind('add', this.appendImage); // collection event binder
			this.collection.fetch();
		},

		render: function(){
			var self = this;

			_(this.collection.models).each(function(image){ // in case collection is not empty
				self.appendImage(image);
			}, this);
			return this;
		},

		appendImage: function(image){
			var self = this;
			var imageView = new ImageView({
				model: image
			});
			var x = $('.images');
			x.append(imageView.render().el);
		}
	});

	$(document).ready(function () {
		var imageListView = new ImageListView();
	});

	
})(jQuery);



