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

Grouping by argument value
--------------------------

When an object makes a call in some context, the default is that the invoked method runs in the
same context. Sometimes it's useful to group calls into different contexts based on the value
passed. This can be done by adding `SwitchPos` and `SwitchCase` annotations. For example::

	class Tester {
	    @SwitchPos(0)
	    @SwitchCase("x", "ForX")
	    @SwitchCase("y", "ForY")
	    public void test(Object a) {
		Box box = new Box();
		box.put(a);

		Object b = box.get(a);
		b.run(a);
	    }
	}

If the `test` method is called from the same context with different values
(e.g. `x` and `y`) then SAM will aggregate them into a single invocation, which
creates a single `Box` object, which holds all the values. SAM will report that
every value may get access to every other value.

Adding switch annotations allows us to separate the cases where `Tester` is called with the
objects "x" and "y" into separate contexts. SAM will model this as two invocations, creating
two boxes (`boxForX` containing `x` and `boxForY` containing `y`)::

	class Tester {
	    @SwitchPos(0)
	    @SwitchCase("x", "ForX")
	    @SwitchCase("y", "ForY")
	    public void test(Object a) {
		Box box = new Box();
		box.put(a);

		Object b = box.get(a);
		b.run(a);
	    }
	}


.. function:: SwitchPos(?Method, ?Pos)

   Invocations of this function should be aggregated in groups based on the values
   passed in argument number ?Pos (starting from zero).

.. function:: SwitchCase(?Method, ?Value, ?Context)

   Defines which values map to which contexts.

Low-level control
-----------------

.. function:: newObject(?Object, ?Invocation, ?ChildType, ?NewObject)

   Aggregate all new objects of type `Type` created by `Object` the context
   `Invocation` into a single object `NewObject`. For example::

     newObject("factory", "clientA", "Proxy", "newProxiesForA").
     newObject("factory", "otherClients", "Proxy", "newProxiesForOthers").

.. function:: methodDoesContextMapping(?Method)

   Normally, when a caller calls a method in a particular context, the target method
   becomes active in the same context and receives all the values as arguments.
   This disables both behaviours, allowing it to be replaced by custom rules. If you
   enable this for a method, you will need to set :func:`didCall`/6 based on `didCall`/5
   somehow and arrange for :func:`mayReceive` to get set based on :func:`maySend` (for
   both cases; with and without the context).

.. function:: methodMatches(?CallSite, ?Target, ?Method)

   When `CallSite` invokes `Target`, `Method` is a method that could be invoked. Normally this
   is true when the method name in `CallSite` equals the name of `Method`, but there are extra
   cases to support unknown caller and target types.

Generated predicates
--------------------

.. function:: realNewObject(?Object, ?Invocation, ?ChildType, ?NewChild)

   The `realNewObject` relation is copied from `newObject`, but has a suitable
   default whenever `newObject` wasn't defined.

.. function:: realInitialInvocation(?Object, ?Method, ?Invocation)

   Usually based on :func:`initialInvocation`, but if `Object` is of type `Unknown` and has
   no initialInvocation defined, then it gets an "unknown" context assigned to it. Note that
   the third parameter is a `Method` here, not a `MethodName`.


