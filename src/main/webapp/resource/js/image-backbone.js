(function($){

	var Image = Backbone.Model.extend({
		defaults: {
			url: '/resource/images/notfound.jpg',
			name: ''
		}
	});

	var List = Backbone.Collection.extend({
		model: Image
	});

	var ImageView = Backbone.View.extend({
		tagName: 'li', // name of tag to be created

		initialize: function(){
		},

		render: function(){
			$(this.el).html("<image src='" + this.model.get("url") + "'/>");
			return this; // for chainable calls, like .render().el
		},

		unrender: function(){
			$(this.el).remove();
		}
	});

	// Because the new features (swap and delete) are intrinsic to each `Item`, there is no need to modify `ListView`.
	var ImageListView = Backbone.View.extend({
		el: $('#images'), // el attaches to existing element

		initialize: function(){
			this.collection = new List();
			this.collection.bind('add', this.appendImage); // collection event binder
		},

		render: function(){
			var self = this;

			_(this.collection.models).each(function(image){ // in case collection is not empty
				self.appendImage(image);
			}, this);
		},

		appendImage: function(image){
			var imageView = new ImageView({
				model: image
			});
			$('ul', this.el).append(imageView.render().el);
		}
	});

	var imageListView = new ImageListView();
})(jQuery);