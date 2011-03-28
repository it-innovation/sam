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
