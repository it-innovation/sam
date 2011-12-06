Invocations
===========

Each time a method on an object is invoked, a new *invocation* is created.
This invocation represents the new stack frame that is created, and holds
the local variables of the call.

Because an unlimited number of invocations can be made, it is always
necessary to aggregate sets of invocations in the real system into a fixed number
of invocations in the model.

The :func:`showInvocation` predicate allows configuration of whether to
show invocations as separate objects or (the default) to aggregate each
with its parent object.

Each `setup` and `test` block in the :ref:`Configuration` block defines the 
initial modelling context for all calls made within that block. By default,
when a call-site makes a call from some context, the called method's activation
frame is grouped using the same context name.

For example, given this configuration, the calls to the constructor for `User`
and to `alice`'s `User.run` method will be grouped under the "A" context::

  config {
    test "A" {
      User alice = new User();
      alice.run();
    }
  }

Sometimes this is not sufficient. For example::

  class User {
      public void run(Object a) {
          a.invoke(a);
      }
  }

  config {
    test "A" {
      Object x = new Unknown();
      Object y = new Unknown();

      User alice = new User();

      alice.run(x);
      alice.run(y);
    }
  }

  assert !hasRef("x", "y").
  assert !hasRef("y", "x")

.. sam-output:: invocations-over-aggregation

Here, SAM groups both calls to `alice.run` together. It therefore only knows that:

* `alice.run`'s local variable `a` may be `x` or `y`.
* `alice.run` may invoke `x` and `y`.
* `alice.run` may pass `x` or `y` as the argument.

In the model, therefore, SAM thinks that `x.run(y)` may be called, and so cannot prove the required
safety properties for the real system.

In such cases, is it necessary to specify a different grouping strategy for invocations, as explained
below. This works as follows:

* We aggregate all invocations of `alice.run` where `a` is `x` under one context ("X").
* We aggregate all invocations of `alice.run` where `a` is `y` under another context ("Y").

Invocations in group "X" can therefore only ever call `x.run(x)`, while those in group "Y"
can only call `y.run(y)`, and we can now prove that `x` never gets access to `y`.

Safety of grouping
------------------

Changing the grouping strategy is always safe (it will not allow us to prove safety properties
that are not true of the real system). After all, in the real system all calls are separate, so any
grouping strategy is a valid over-approximation.

However, be careful about using names derived from contexts when specifying goals. An object created in 
context "X" will be called `<name>X`. If you write a goal such as::

  assert !hasRef("alice", "fooX").

then this assertion can be made to succeed by simply changing the aggregation strategy so that the objects
created are aggregated as "fooY" instead. You need to make sure that `fooX` really does aggregate
the objects you care about. A better solution may be to write the rule in a way
that doesn't depend on contexts. For example::

  haveBadAccess("alice", ?X) :- hasRef("bob", ?X).

(it is an error for `alice` to get a reference to any object to which `bob` has a reference).


Grouping by argument value
--------------------------

This can be done by adding `GroupByArgAt` and `GroupByArgCase` annotations. For example::

  class User {
      @GroupByArgAt(0)
      @GroupByArgCase("null", "Null")
      @GroupByArgCase("x", "X")
      @GroupByArgCase("y", "Y")
      public void run(Object a) {
          a.invoke(a);
      }
  }

If the method takes other arguments (not at position=0), then they will be available in
all contexts. SAM will report an error if `a` could have a value that you didn't handle.

Note that you need to handle the case where `a = null` too (a caller without any reference to pass
could still call the method with a null argument).

.. function:: GroupByArgAt(String method, int Pos)

   Invocations of this function should be aggregated in groups based on the values
   passed in argument number ?Pos (starting from zero).

.. function:: GroupByArgCase(String method, Object value, String context)

   Defines which values map to which contexts.


Group everything into a single context
--------------------------------------

.. function:: GroupAs(String method, String context)

   All calls to this method are grouped into `context`. This can be useful to reduce the number
   of contexts where it doesn't matter, or to tag certain calls (for example, grouping all calls
   on a logging proxy into a "Logged" context).

Low-level control
-----------------

.. function:: newObject(Ref object, String invocation, String childType, Ref newObject)

   Aggregate all new objects of type `Type` created by `Object` the context
   `Invocation` into a single object `NewObject`. For example::

     newObject("factory", "clientA", "Proxy", "newProxiesForA").
     newObject("factory", "otherClients", "Proxy", "newProxiesForOthers").

.. function:: methodDoesContextMapping(String method)

   Normally, when a caller calls a method in a particular context, the target method
   becomes active in the same context and receives all the values as arguments.
   This disables both behaviours, allowing it to be replaced by custom rules. If you
   enable this for a method, you will need to set :func:`didCall`/6 based on `didCall`/5
   somehow and arrange for :func:`didReceive` to get set based on :func:`maySend`/5.

Generated predicates
--------------------

.. function:: realNewObject(Ref object, String invocation, String childType, Ref newChild)

   The `realNewObject` relation is copied from `newObject`, but has a suitable
   default whenever `newObject` wasn't defined.

.. function:: realInitialInvocation(Ref object, String method, String invocation)

   Usually based on :func:`initialInvocation`, but if `Object` is of type `Unknown` and has
   no initialInvocation defined, then it gets an "unknown" context assigned to it. Note that
   the third parameter is a `Method` here, not a `MethodName`.


