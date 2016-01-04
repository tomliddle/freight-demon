
Freight Demon
===========================

This is TruckScheduling webapp which is more of a demonstration at present.

It uses immutable classes for the entities to allow easy persistence using a document database.

The design also helps with reducing of bugs and simplification of code.

Any modifications to a particular solution can be returned as a new object and compared
against other solutions for cost, time and distance etc.


Note:
You need to install node and babel to get the js6 to compile using the jetty task.