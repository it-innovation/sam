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

.. function:: newObject(?Object, ?Invocation, ?Type, ?NewObject).

   Aggregate all new objects of type `Type` created by `Object` the context
   `Invocation` into a single object `NewObject`. For example::

     newObject("factory", "clientA", "Proxy", "newProxiesForA").
     newObject("factory", "otherClients", "Proxy", "newProxiesForOthers").

.. function:: invocationObject(?Caller, ?CallerInvocation, ?Target, ?Arg, ?VarName, ?TargetInvocation)

   Whenever the object `Caller` invokes a method on `Target` in the context
   `CallerInvocation`, passing ?Arg and storing the result in ?VarName, all the
   resulting invocations should be aggregated into the `TargetInvocation`
   context. If not specified, the default is to use `CallerInvocation` for
   `TargetInvocation`.
   
   For example, to group all invocations by clientA's "Other" context of
   `myTask = factory(arg)` into the "A" group::

     invocationObject("clientA", "Other", "factory", ?Arg, "myTask", "A") :- isObject(?Arg).

