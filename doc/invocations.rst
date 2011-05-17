Invocations
===========

Each time a method on an object is invoked, a new *invocation* is created.
This invocation represents the new stack frame that is created, and holds
the local variables of the call.

Because an unlimited number of invocations can be made, it is always
necessary to aggregate sets of invocations into a single one.

The :func:`showInvocation` predicate allows configuration of whether to
show invocations as separate objects or (the default) to aggregate each
with its parent object.

Aggregation
-----------

.. function:: newObject(?Object, ?Invocation, ?ChildType, ?NewObject)

   Aggregate all new objects of type `Type` created by `Object` the context
   `Invocation` into a single object `NewObject`. For example::

     newObject("factory", "clientA", "Proxy", "newProxiesForA").
     newObject("factory", "otherClients", "Proxy", "newProxiesForOthers").


.. function:: invocationObject(?Caller, ?CallerInvocation, ?CallSite, ?TargetInvocation)

   Whenever the object `Caller` executes the call at `CallSite` in the context
   `CallerInvocation`, all the resulting invocations should be aggregated into
   the `TargetInvocation` context. If not specified, the default is to use
   `CallerInvocation` for `TargetInvocation`.
   
   For example, to group all invocations by clientA's "other" context of
   the first call in the "ClientA.run" method into the "a" group::

     invocationObject("clientA", "other", "ClientA.run-1", "a").

.. function:: methodMatches(?CallSite, ?Target, ?Method)

   When `CallSite` invokes `Target`, `Method` is a method that could be invoked. Normally this
   is true when the method name in `CallSite` equals the name of `Method`, but there are extra
   cases to support unknown caller and target types.

Generated predicates
--------------------

.. function:: realNewObject(?Object, ?Invocation, ?ChildType, ?NewChild)

   The `realNewObject` relation is copied from `newObject`, but has a suitable
   default whenever `newObject` wasn't defined.

.. function:: realInvocationObject(?Caller, ?CallerInvocation, ?CallSite, ?TargetInvocation)

   The `realInvocationObject` relation is copied from `invocationObject`, but
   has a suitable default whenever `invocationObject` wasn't defined.

.. function:: realInitialInvocation(?Object, ?Invocation)

   Usually the same as :func:`initialInvocation`, but if `Object` is of type `Unknown` and has
   no initialInvocation defined, then it gets an "unknown" context assigned to it.
