Concepts
========

Objects
-------
We assume that the system being modelled is an object-capabilities system. The system contains a set of objects, which hold references to each other.
Objects may create other objects. The rules for access propagation are as follows:

* An object can only invoke (send a message to) another object if it holds a reference to it.
* When an object is created, the parent holds the only reference to it. The new object only has references given to it by its parent.
* If object A has references to B and to C, then A may give its reference to C to B.

These are the only ways to get references.


Modelling
---------
We model a safe over-approximation of the real system. Everything that can happen in the real system must also be able to happen in the model, but some things may be
possible in the model that are not possible in the real system. This makes the modelling tractable and also allows modelling of unknown behaviour.


Aggregation
-----------
We can always safely aggregate any subset of the objects into a single object.
The aggregated object has access to everything that any of the original objects
did, and may do anything that they may have done.

Aggregation is always "safe" in the sense that any safety property (e.g. "A can
never get access to B") that we can prove for the aggregated system will also
be true of the original system.

In the limit, you could aggregate all objects in the real system into a single object
in the model (in which case you would not be able to prove any safety properties).

Aggregation makes it possible to model systems where the number of real objects is
unbounded (e.g. because any number of new objects may be created). For example,
we can model every object that could ever be created by a factory as a single
"newObjects" object.


Invocations
-----------
When a method on an object is invoked, a new stack frame is created for that
call. This stack frame contains the arguments and local variables of the method.
Just as we can aggregate objects, we can (and must) also aggregate invocations.

For example, consider a factory with two clients ("ClientA" and "OtherClients"):
