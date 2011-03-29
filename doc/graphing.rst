.. _Graphing:

Graphing
========

Two graphs are created: "initial" is produced after all the "initial"
imports are processed and then "access" is produced once the "final"
imports have also been processed.

.. function:: graphNode(?NodeId, ?Attributes)

   Specify the GraphViz attributes for a node. For example::

     graphNode("store", "color=red").

.. function:: graphEdge(?Source, ?Target, ?Attributes)

   Specify that there is an edge from `Source` to `Target` with the given
   attributes. For example::

     graphEdge("user", "store", "style=dashed").

.. function:: showInvocation(?Object, ?InvocationContext)

   Configuration setting that causes the access graph to show these
   invocations of Object explicitly on the graph. If false, these
   invocations are aggregated with their object. To graph all invocations,
   use::

     showInvocation(?Object, ?Invocation) :- live(?Object, ?Invocation).

Notation
--------

.. graphviz::
   digraph notation {
     node [shape=plaintext];
     A -> B [label="field"];
     C [label="C\n(unknown behaviour)",fontcolor=red];

     A -> D [color=red,fontcolor=red,label="safety violation"];
     B -> A [color=red,fontcolor=red,style=dotted,label="liveness violation"];

     node [color=green,fontcolor=green];
     invocationOfA -> invocationOfB [label="calls",fontcolor=green,color=green];
     invocationOfA -> A [color=blue,style=bold,label="this",fontcolor=blue];
     invocationOfB -> B [color=blue,style=bold,label="this",fontcolor=blue];
     invocationOfB -> C [color=blue,label="local",fontcolor=blue];
   }

This diagram shows:

* Object `A` has a reference to `B` (stored in a field of `A`).
* During invocation of a method on `A`, the code might call `B`. Such calls are aggregated as `invocationB`.
* Each invocation has a local variable called `this` to its parent object.
* Some of the calls aggregated as `invocationB` may get a reference to an object `C` with unknown
  behaviour.
* `A` has access to `D`, but shouldn't.
* `B` should be able to get access to `A`, but can't.
