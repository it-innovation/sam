.. highlight:: java

Factories and contexts
======================

Earlier in the tutorial, we saw that giving two unconfined `Unknown` objects access to a factory
could result in them accessing each other's tasks:

.. image:: _images/factory2.png

To fix this, we must restrict clientA's behaviour. For example, we can model
clientA as having three separate fields: "myTask", "myFactory" and
"myOtherRefs". "myTask" will be the task(s) clientA created explicitly using
the factory, "myFactory" is the factory, and "myOtherRefs" will represent all
other fields (aggregated)::

  class ClientA {
    private Object myFactory;
    private Object myTask;
    private Object myOtherRefs;

    public ClientA(Object factory, Object otherRefs) {
      myFactory = factory;
      myOtherRefs = otherRefs;
    }

    public void run() {
      myTask = myFactory.newInstance();
      myTask = myTask.invoke(myTask);
    }
  }

This model (:example:`factory3`) is safe, though it puts rather strict limits
on what clientA can do:

.. image:: _images/factory3.png

The black arrow shows that, though `clientA` has a reference to `otherClients`, it never calls
it. If we later want to modify clientA, we can update the model to check whether all our previous
safety properties are still satisfied by the updated code.

Explicit aggregation
--------------------
Sometimes the default aggregation rules are not sufficient. For example, if we
try to check whether it's safe for clientA to call `myOtherRefs.invoke(myOtherRefs)`,
we find that the required properties can't be verified::

  class ClientA {
    private Object factory;
    private Object myTask;
    private Object ref;
  
    public void run() {
      myTask = factory.newInstance();
      myTask = myTask.invoke(myTask);
      myOtherRefs.invoke(myOtherRefs);
    }
  }

Turning on display of invocations shows the reason (:example:`factory4`)::

  showAllInvocations("factory").

.. image:: _images/factory4.png

The example reported (simplified) is:

.. code-block:: none

  debug()
     <= getsAccess('otherClients', 'taskA')
	<= otherClients: got taskA
	   <= otherClients: factory.newInstance()
	      <= clientA: otherClients.*()
	   <= factory: new taskA()

* `otherClients` got `taskA` because:
  
  * it called `factory.newInstance()`, which it did because:

    * `clientA` invoked `otherClients`; and

  * the factory created `taskA`.

The problem here is that the default aggregation strategy groups all calls resulting from
actions by `clientA` under the "A" context. Because `clientA` invoked `otherClients`, tasks
created directly by `clientA` are grouped with tasks created by `otherClients`. Often this is
what you want (for example, if `otherClients` was instead some kind of proxy), but in this case
we want to treat them separately.

In fact, clientA may end up with references to two different groups of Tasks: those
`clientA` created directly using the factory, and those received from calls to other
objects.

We will therefore put `clientA`'s initial invocation into the "other" group, and
tell SAM to put only the `factory.invoke()` invocation under "A"::

  config {
    Factory factory;

    setup {
      factory = new Factory();
    }

    test "Other" {
      Object otherClients = new Unknown(factory);
      Object clientA = new ClientA(factory, otherClients);
      clientA.run();
    }
  }

  invocationObject("clientA", "Other", "ClientA.run-1", "A").

The third argument to :func:`invocationObject` identifies the call: the first call in the `ClientA.run` method.

With this division, the desired propery can be proved. `clientA` can now get access to tasks created
by other parties, but others still can't get access to the tasks created by `clientA`  (:example:`factory5`):

.. image:: _images/factory5.png

We need to be careful here. While playing around with aggregation
strategies always leads to a correct over-approximation of the behaviour of the
system, note that our goal refers to `taskA`. We have proved that `otherClients` never
gets access to `taskA`, but which real tasks are in `taskA` now, and which are in `taskOther`?

We can state our goal more explicitly by saying that it is an error if
`otherClients` gets access to any reference that `clientA` may store in
`myTask`::

  error("otherClient may access some clientA.myTask") :-
          getsAccess("otherClients", ?Ref),
          field("clientA", "myTask", ?Ref).

This means that if there is some way that `clientA` could create a new task, aggregated under
`taskOther`, and store it in `myTask` then we would still detect the problem.
