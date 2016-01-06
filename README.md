
Freight Demon
===========================

A TruckScheduling webapp which allows the user to add trucks, stops (deliveries), depots and see an optimised
delivery route displayed on a map.

Technologies include Scalatra, Slick (probably to be removed in favour of...), MongoDB, Backbone, Handlebars,
JQuery, JS6 (using babel to transpile) and others.

The project is in its infancy at present and there are a number of steps taken to get a working demonstration
that need to be filled out later when I have more time.

The project uses extensive use of case classes to get the full benefits of immutability and the copy feature
that comes 'for free'. Logic is kept with the object in question, so a Truck knows how to optimise (or more
specifically the TruckOptimiser trait adds this functionality to Truck for example). Other benefits include
the simplification of finding a better (cheaper) solution. If you call optimise on a solution object, a new
solution object is returned which is the lowest cost found. This object can be persisted and returned and
will remain constant.

There are some issues with this that still need to be resolved.

Firstly the Truck object needs to have a copy of the LocationMatrix to be able to optimise. Although this is
another immutable object and there is only one copy for any set of solutions it should probably only belong in
the solution object. It could be that the Truck object doesn't hold any routing information and a new set of
objects called Routes take the place of the Trucks and have access to the LocationMatrix and also contain a
reference to the Truck that is assigned to that route. The set of Routes could then become the output of the
solution calculation in another object that is persisted.

Secondly the initial persistance mechanism was Slick, which although a good solution provided a bit challenging
to store large complex objects such as the Solution with lists of Trucks and Stops. This task is in the middle of
being moved over to MongoDB but currently is using both.

How the user interacts with the site also needs to be determined. Currently a solution is made from all Trucks and
Stops the user adds to the app.

A solution might be that the user can persist named sets of Trucks and Stops. The user will then be able to create
a new Solution from these sets. This would also aid those wishing to use just the API rather than the web interface.


Note:

There are a number of steps to get this up and running. I will add a brief list here

Install MongoDB

Install Node & Babel plugin

Transpile the JS6 to JS5 using the Jetty task (deployment isn't fully automated yet).