

// Represents an image
var ImageModel = Backbone.Model.extend({});


// Represents a view of the image
var ImageView = Backbone.View.extend({
	el : $("#images"), // Specifies the DOM element which this view handles

	events : {
		// Call the event handler "updateVal" when slider value changes.
		// 'slidechange' is the jquery slider widget's event type.
		// Refer http://jqueryui.com/demos/slider/#default for information about 'slidechange'.
		"slidechange" : "updateVal"
	},

	updateVal : function() {
		// Get slider value and set it in model using model's 'set' method.
		console.log('SliderView.updateVal');
		var val = this.el.slider("option", "value");
		this.model.set({slidervalue : val});
	},

	render: function(){
		$(this.el).append("<ul> <li>hello world</li> </ul>");
	}
});

// The listener "View" for the model.
// Whenever the model's slidervalue attribute changes, this view receives the updated value.
var ValueView = Backbone.View.extend({
	initialize: function(args) {
		// _.bindAll is provided by underscore.js.
		// Due to some javascript quirks, event handlers which are bound receive a 'this'
		// value which is "useless" (according to underscore's docs).
		// _.bindAll is a hack that ensures that 'this' in event handler refers to this view.
		_.bindAll(this, 'updateValue');


		console.log('ValueView.initialize');

		// Bind an event handler to receive updates from the model whenever its
		// 'slidervalue' attribute changes.
		this.model.bind('change:slidervalue', this.updateValue);
		console.log(this);
	},

	updateValue: function() {

		// Get the slider value from model, and display it in textbox.
		console.log('ValueView.updateValue');

		// this.model won't work unless "_.bindAll(this, ...)" has been called in initialize
		var value = this.model.get('slidervalue');
		console.log(value);
		$("#sliderVal").val(value);
	}
});

// Create the instances
var imageModel = new ImageModel;
var imageView = new ImageView({model : imageModel});
var valView = new ValueView({model : imageModel});