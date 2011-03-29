Concepts
========

Objects
-------
We assume that the system being modelled is an object-capabilities system. The system contains a set of objects, which hold references to each other.
Objects may create other objects. The rules for access propagation are as follows:

* An object A can only invoke (send a message to) another object B if it holds a reference to it:

    .. graphviz::
      digraph msg { rankdir=LR; A -> B [color=green,label="(calls)",fontcolor=green] }

* When an object is created, the parent holds the only reference to it. The new object only has references given to it by its parent:

    .. graphviz::
      digraph msg {
      	rankdir=LR;
        B[style=dashed];
      	A -> B [color=green,label="(creates)",fontcolor=green,style=dotted]
      }

* If object A has references to B and to C, then A may give its reference to C to B:

    .. graphviz::
       digraph msg { rankdir=LR; A -> B[color=green]; A -> C; B->C [style=dotted] }

* If object A has a reference to B and B has a reference to C, then B may return its reference to C to A:

    .. graphviz::
       digraph msg { rankdir=LR; A -> B[color=green]; B -> C; A->C [style=dotted] }

These are the only ways to get references.


Modelling
---------
We model a safe over-approximation of the real system. Everything that can happen in the real system must also be able to happen in the model, but some things may be
possible in the model that are not possible in the real system. This makes the modelling tractable and also allows modelling of unknown behaviour.

Because the model is an over-approximation, if something doesn't occur in the model then it also
cannot occur in the real system.

Aggregation
-----------
We can always safely aggregate any subset of the objects into a single object.
The aggregated object has access to everything that any of the original objects
did, and may do anything that they may have done. For example, this system:

    .. graphviz::
      digraph msg { rankdir=LR; A->B; A->C; B->D; C->E; }

Can be aggregated like this:

    .. graphviz::
      digraph msg { rankdir=LR; A->"B,C"; "B,C"->"D,E"; }

Aggregation is always "safe" in the sense that any safety property (e.g. "A can
never get access to D") that we can prove for the aggregated system will also
be true of the original system.

If you aggregate too far then you may be unable to prove some valid properties (for example,
that B doesn't have access to E in the system above).

In the limit, you could aggregate all objects in the real system into a single object
in the model (in which case you would not be able to prove any safety properties).

Aggregation makes it possible to model systems where the number of real objects is
unbounded (e.g. because any number of new objects may be created). For example,
we can model every object that could ever be created by a factory as a single
"newObjects" object:

  .. graphviz::
     digraph msg { rankdir=LR; edge[style=dotted];
     factory; node[style=dashed];
       factory->newObject1; factory->newObject2; factory->"...";
       "..."[shape=plaintext];
     }

Can be modelled as:

  .. graphviz::
     digraph msg { rankdir=LR; edge[style=dotted];
       factory; node[style=dashed];
       factory->newObjects;
     }

Invocations
-----------
When a method on an object is invoked, a new stack frame is created for that
call. This stack frame contains the arguments and local variables of the method.
Just as we can aggregate objects, we can (and must) also aggregate invocations.

For example, consider a factory with some clients ("clientA" and "otherClients"):

  .. graphviz::
     digraph msg {
       node[shape=plaintext];
       factory;
       node[fontcolor=red];
       clientA->factory;
       otherClients->factory;
     }

We want to prove that the other clients (aggregated into a single "otherClients" object)
cannot get access to the new objects created by "clientA":

  .. image:: images/factory.png

.. note::
   These diagrams use the SAM colour scheme:

   * An object in red text indicates an object with unknown behaviour.
   * A black arrow represents a reference stored in a field on the object.
   * A blue arrow represents a reference held in a local variable of an invocation.

Without modelling invocations we could only say that the factory creates newTasksForA
and newTasksForOthers and that it may return both to its callers. The behaviour of an object
cannot depend on who calls it, because an object being invoked does not know this and the
behaviour of an object corresponds to its implementation in the real system.

Instead, we specify the behaviour of the factory as being that it creates new "task" objects,
stores the reference in a local variable, and returns that reference to its caller.

Then, we separately tell SAM to aggregate all invocations from clientA into one invocation object
and all invocations from otherClients in another. Like all aggregations, this is a safe
over-approximation of the actual behaviour. Here is the same diagram but with the invocations
of the factory shown in green, rather than aggregated with the factory object as before:

  .. image:: images/factory-showing-invocations.png

.. tip:: Use :func:`showInvocation` to control which invocations are shown explicitly in the graph.

Here we can see that none of otherClient's invocations can get access to newTasksForA, and so
otherClients itself cannot either.

The bold blue arrow from each invocation to the factory represents the "this" variable, giving
the invocation access to its object's fields.
