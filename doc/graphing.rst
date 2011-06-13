.. _Graphing:

Graphing
========

Two graphs are created: "initial" is produced after all the "initial"
imports are processed and then "access" is produced once the "final"
imports have also been processed.

.. function:: graphNode(?Node, ?Attrs)

   Specify the GraphViz attributes for a node. For example::

     graphNode("store", "color=red").

.. function:: graphEdge(?Source, ?Target, ?Attrs)

   Specify that there is an edge from `Source` to `Target` with the given
   attributes. For example::

     graphEdge("user", "store", "style=dashed").

.. function:: showAllInvocations(?Object)

   Configuration setting that causes the access graph to show all
   invocations of Object explicitly on the graph. If false, these
   invocations are aggregated with their object.

.. function:: showInvocation(?Object, ?Invocation)

   Configuration setting that causes the access graph to show these
   specific invocations of Object explicitly on the graph. If false, these
   invocations are aggregated with their object.

Notation
--------

.. graphviz::

   digraph notation {
     Public [shape=record];
     node [shape=plaintext];
     A -> B [label="field"];
     C [label="C\n(unknown behaviour)",fontcolor=red];

     A -> D [color=red,fontcolor=red,label="safety violation"];
     B -> A [color=red,fontcolor=red,style=dotted,label="liveness violation"];

     node [color=green,fontcolor=green];
     invocationOfA -> invocationOfB [label="calls",fontcolor=green,color=green];
     invocationOfA -> A [style=bold,label="this",style=dashed];
     invocationOfB -> B [style=bold,label="this",style=dashed];
     invocationOfB -> C [label="local",style=dashed];

     Public -> A;
   }

This diagram shows:

* Object `A` has a reference to `B` (stored in a field of `A`).
* During invocation of a method on `A`, the code might call `B`. Such calls are aggregated as `invocationB`.
* Each invocation has a local variable called `this` to its parent object.
* Some of the calls aggregated as `invocationB` may get a reference to an object `C` with unknown
  behaviour.
* `A` has access to `D`, but shouldn't.
* `B` should be able to get access to `A`, but can't.
* `Public` has been marked as public; see :func:`isPublic`. References to it are not shown, to avoid
  cluttering up the graph.
