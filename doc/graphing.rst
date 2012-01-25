.. _Graphing:

Graphing
========

.. function:: graphNode(String node, String attrs)

   Specify the GraphViz attributes for a node. For example::

     graphNode("store", "color=red").

.. function:: graphEdge(String source, String target, String attrs)

   Specify that there is an edge from `Source` to `Target` with the given
   attributes. For example::

     graphEdge("user", "store", "style=dashed").

.. function:: showAllInvocations(Ref object)

   Configuration setting that causes the access graph to show all
   invocations of Object explicitly on the graph. If false, these
   invocations are aggregated with their object.

.. function:: showInvocation(Ref object, String invocation)

   Configuration setting that causes the access graph to show these
   specific invocations of Object explicitly on the graph. If false, these
   invocations are aggregated with their object.

.. function:: graphHideObject(Ref ref)
.. function:: graphHideRef(Ref source, Ref target)

   Don't show this node or edge on the graph. Used to hide the `_testDriver` object.

.. function:: showOnlyProblemNodes()

   If on, the graph is limited to showing "important" nodes. A node is important if:

   - it is one of the initial objects
   - it is at one end of a debug arrow (an orange or red one), or
   - it :func:`didCreate` an important object

   If there is no problem, this is ignored and all nodes are shown.

.. function:: hideUncalledReferences()

    Especially when using access control, the graph can get cluttered with references that
    are held but not used. Setting this hides them.

.. function:: ignoreEdgeForRanking(Object source, Object target)

   Prevents edges from source to target from affecting the ranking. Unknown objects often
   cause many extra edges to be added which distort the shape of the graph. Ignoring these
   edges in the ranking calculation allows the graph to be structured according to the
   intended model.

Clusters
--------

You can group nodes into clusters using :func:`graphCluster`. For, example, to create clusters of objects with the
same identity (labelled with that identity)::

  graphCluster(?Identity, ?Object) :- hasIdentity(?Object, ?Identity).
  graphClusterLabel(?Identity, ?Identity) :- hasIdentity(?Object, ?Identity).

.. function:: graphCluster(String clusterID, Object graphNode)

   Put `GraphNode` in cluster `ClusterID`.

.. function:: graphClusterColour(String clusterID, String colour)

   Set the border and label colour for `ClusterID`.

.. function:: graphClusterLabel(String clusterID, String label)

   Set label text for `ClusterID`.

Low-level control
-----------------

.. function:: visibleGraphEdge(String sourceGraphNode, String targetGraphNode, String attrs)
.. function:: visibleGraphNode(String graphNode, String attrs)

   These are copies of :func:`graphEdge` and :func:`graphNode`, but filtered to remove hidden nodes.

Notation
--------

.. graphviz::

   digraph notation {
     P [shape=record,label="P\n(public)"];
     node [shape=plaintext];
     A -> B [label="field"];
     C [label="C\n(unknown behaviour)",fontcolor=blue];

     A -> D [color=red,fontcolor=red,label="safety violation"];

     A -> C [label="local",style=dashed];
     A -> E [label="called field",color=green,fontcolor=green];

     P -> A [color=orange,label="cause",fontcolor=orange];
   }

This diagram shows:

* P is flagged as Public (it has a border). All unknown objects know its address.
* A has a field which may point to B.
* Some method of A has a local variable which may point to C.
* C has unknown behaviour (it will try to do anything it is able to do).
* A has access to D, but shouldn't. This was caused by P calling A (the orange arrow).
* A has a field which may point to E, and may invoke E.

When invocations are being displayed, they are shown as additional green nodes:

.. graphviz::

   digraph notation {
     node [shape=plaintext];
     A -> E [label="field"];
     C;

     node [color=green,fontcolor=green];
     invocationOfA -> invocationOfB [label="calls",fontcolor=green,color=green];
     invocationOfA -> A [style=bold,label="this",style=dashed];
     invocationOfB -> E [style=bold,label="this",style=dashed];
     invocationOfB -> C [label="local",style=dashed];
   }

This diagram shows:

* Object `A` has a reference to `E` (stored in a field of `A`).
* During invocation of a method on `A`, the code might call `E`. Such calls are aggregated as `invocationB`.
* Each invocation has a local variable called `this` to its parent object.
* Some of the calls aggregated as `invocationB` may get a reference to an object `C`.
