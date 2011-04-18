.. _tutorial:

Tutorial
========

A model contains several sections:

* A model of the behaviour of the objects in the system. This behaviour corresponds to the design or source code.

* The scenerio being modelled, which consists of:

  * The objects initially present and their connections.
  * The goals (security properties to be verified).
  * Aggregation rules to control the level of detail modelled.

All of these can be placed in a single file.

Behaviour
---------
We'll start by modelling the factory scenario descibed in the :ref:`Concepts` page.

A "factory" object creates new objects of type "Task", corresponding to the following Java
code::

  class Factory {
    public Task newInstance() {
      Task task = new Task();
      return task;
    }
  }

  class Task extends Unknown {
  }

.. note::
  SAM uses a small Java-like syntax to define behaviour. The above Java code can be
  used as-is, but more complex code must be simplified.

The `Unknown` class is used for code whose behaviour is unknown or untrusted. Making
`Task` extend `Unknown` here means that the safety properties we verify for the model will
be true no matter how `Task` is actually imlemented.

See :ref:`Behaviour` for more information about defining behaviour.

Configuration
-------------
We can now define the initial configuration. We'll model two client objects: clientA, representing a single arbitrary client, and otherClients, aggregating all the others::

  initialObject("clientA", "Unknown").
  initialObject("otherClients", "Unknown").
  initialObject("factory", "Factory").

  initialInvocation("clientA", "a").
  initialInvocation("otherClients", "other").

The :func:`initialObject` lines define the three objects and their types.

The :func:`initialInvocation` lines say that we assume both clients may be active by default (they
don't wait for someone to invoke them). The second argument is the *modelling context*. By giving them
different contexts we tell the modeller to consider these two cases separately when aggregating
invocations.

We also give each object a reference to the factory using :func:`field`. The
"Unknown" type is modelled as an object which may make any call it is able to
make. All its fields are aggregated into a single field called "ref"::

  field("clientA", "ref", "factory").
  field("otherClients", "ref", "factory").

See :ref:`Configuration` for more information.

Running the scenario
--------------------
Putting these together gives this complete model file (examples/factory1.sam)::

  /* Behaviour */
  class Factory {
    public Task newInstance() {
      Task task = new Task();
      return task;
    }
  }
  
  class Task extends Unknown {
  }
  
  /* Config */
  
  initialObject("clientA", "Unknown").
  initialObject("otherClients", "Unknown").
  initialObject("factory", "Factory").
  
  field("clientA", "ref", "factory").
  field("otherClients", "ref", "factory").
  
  initialInvocation("clientA", "a").
  initialInvocation("otherClients", "other").

You can run the model like this::

  $ sam factory1.sam

See :ref:`install` for more information about running SAM.

You should find you now have an output file called "access.dot.png":

.. image:: _images/factory1.png

This shows that, given the behaviour and initial configuration:

* Some new Task objects will be created. SAM aggregates all those that may be created in context "a" as `aTask` and those created in "other" as `otherTask`.
* clientA may get access to the `aTask` tasks.
* otherClients may get access to the `otherTask` tasks.
* The factory gets a reference to all tasks but doesn't store the reference (the
  dashed arrows indicate references held in local variables rather than in fields).

See :ref:`Graphing` for more information about the graphs produced.

Goals
-----
We can now decide what security properties to test. Two kinds of property are possible:

* *Safety properties*, which assert that something can never happen in the real system.
* *Liveness possibilties*, which assert that something isn't prevented by the model.

Because our model is an over-approximation of the real system, safety properties provide
a much stronger guarantee than liveness properties. Liveness properties are mainly useful
as a sanity check that the model isn't too restrictive.

For example, we can require that no other clients can get access to a's tasks::

  denyAccess("otherClients", "aTask").
  requireAccess("clientA", "aTask").

Unconfined clients
------------------

So far, we have assumed that the clients are *confined*. That is, we do not know their
behaviour but we know they don't start with access to anything except the factory. If
the clients are objects in a capability-based programming language then this may be
a reasonable assumption. If they are objects hosted by other parties then we should assume
that they have access to the Internet too.

We could add an explicit `internet` object to our model, but since there's no point having
two Unknown objects connected together (they'll share everything anyway), we'll just give
`clientA` a direct reference to `otherClients` and treat `otherClients` as including the
rest of the Internet too::

  field("clientA", "ref", "otherClients").

When we model this, SAM will detect that our safety goal is not met, and prints a simple
example of how the problem can occur::

  debug()
    <= getsAccess('otherClients', 'aTask')
      <= otherClients: received aTask (arg to Unknown.*)
         <= clientA: otherClients.*()
         <= clientA: got aTask
            <= clientA: factory.newInstance()
            <= factory: new aTask()

  === Errors detected after applying propagation rules ===

  ('unsafe access may be possible', 'otherClients', 'aTask')

You can read this as:

* The debugger was triggered because `otherClients` got access to `aTask`, which happened because:

  * `otherClients` got passed `aTask` as a method argument, which happened because:

    * `clientA` invoked `otherClients`, and
    * `clientA` had got `aTask`, because:

      * `clientA` had called `factory.newInstance` and
      * `factory` had created `aTask`.

.. note:: There is another problem with this model, which we will cover in the next section.
          SAM may report this (less obvious) problem instead of the example above.

The red arrow in the diagram corresponds to this problem, and the orange arrows show the
calls in the debugger's example:

.. image:: _images/factory2.png

This says that if we can't rely on clientA's behaviour then we can't be sure that
other client's won't get access to its tasks. To fix this, we must restrict clientA's
behaviour. For example, we can model clientA as having three separate fields:
"myTask", "ref" and "factory". "myTask" will be the task(s) clientA created explicitly using
factory, "factory" is the factory, and "ref" will represent all other fields (aggregated)::

  class ClientA {
    private Object factory;
    private Object myTask;
    private Object ref;
  
    public void run() {
      myTask = factory.newInstance();
      myTask = myTask.invoke(myTask);
    }
  }
  initialObject("clientA", "ClientA").
  field("clientA", "factory", "factory").

This model is safe, though it puts rather strict limits on what clientA can do:

.. image:: _images/factory3.png

The black arrow shows that, though `clientA` has a reference to `otherClients`, it never calls
it. If we later want to modify clientA, we can update the model to check whether all our previous
safety properties are still satisfied by the updated code.

Explicit aggregation
--------------------
Sometimes the default aggregation rules are not sufficient. For example, if we
try to check whether it's safe for clientA to call `ref = ref.invoke(ref)`,
we find that the required properties can't be verified::

  class ClientA {
    private Object factory;
    private Object myTask;
    private Object ref;
  
    public void run() {
      myTask = factory.newInstance();
      myTask = myTask.invoke(myTask);
      ref.invoke(ref);
    }
  }

Turning on display of invocations shows the reason::

  showInvocation("factory", ?Invocation) :- isInvocation(?Invocation).

.. image:: _images/factory4.png

The example reported is::

  debug()
     <= getsAccess('otherClients', 'aTask')
        <= otherClients: got aTask
           <= otherClients: factory.newInstance()
              <= clientA: otherClients.*()
           <= clientA: factory.newInstance()
           <= factory: new aTask()

* `otherClients` got `aTask` because:
  
  * it called `factory.newInstance()`, which it did because:

    * `clientA` invoked `otherClients`; and

  * the factory created `aTask`.

The problem here is that the default aggregation strategy groups all calls resulting from
actions by `clientA` under the "a" context. Because `clientA` invoked `otherClients`, tasks
created directly by `clientA` are grouped with tasks created by `otherClients`. Often this is
what you want (for example, if `otherClients` was instead some kind of proxy), but in this case
we want to treat them separately.

In fact, clientA may end up with references to two different groups of Tasks: those
`clientA` created directly using the factory, and those received from calls to other
objects.

We will therefore put `clientA`'s initial invocation into the "other" group, and
tell SAM to put only the `factory.invoke()` invocation under "a"::

  initialInvocation("clientA", "other").
  invocationObject("clientA", "other", "ClientA.run-1", "a").

The third argument to `invocationObject` identifies the call: the first call in the `ClientA.run` method.

With this division, the desired propery can be proved. `clientA` can now get access to tasks created
by other parties, but others still can't get access to the tasks by `clientA`.

.. image:: _images/factory5.png

We need to be careful here. While playing around with aggregation
strategies always leads to a correct over-approximation of the behaviour of the
system, note that our goal refers to `aTask`. We have proved that `otherClients` never
gets access to `aTask`, but which real tasks are in `aTask` now, and which are in `otherTask`?

We can state our goal more explicitly by saying that `otherClients` must not get access to any
reference that `clientA` may store in `myTask`::

  denyAccess("otherClients", ?Value) :- field("clientA", "myTask", ?Value).

This means that if there is some way that `clientA` could create a new task, aggregated under
`otherTask`, and store it in `myTask` then we would still detect the problem.
