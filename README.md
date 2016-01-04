
Freight Demon
===========================

This is TruckScheduling webapp which is more of a demonstration at present.

It uses immutable classes for the entities to allow these to be easily persisted using a document database
and recalled later.

This also helps with reducing of bugs and simplification of code.

For example anything done to change a particular solution can be returned as a new object and compared
against other solutions for cost, time and distance etc.


Note:
You need to install node and babel to get the js6 to compile using the jetty task.