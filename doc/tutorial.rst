.. highlight:: java

.. _tutorial:

Tutorial
========

A model contains several sections:

* A model of the behaviour of the objects in the system. This behaviour corresponds to the design or source code.

* The scenerio being modelled, which consists of:

  * The objects initially present and their connections.
  * The test cases to be tried.

* Goals (security properties to be verified).
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

.. note::
  SAM uses a small Java-like syntax to define behaviour. The above Java code can be
  used as-is, but more complex code must be simplified.

Any class which we don't explicity define (e.g. `Task` here) will be given the `Unknown`
behaviour; we assume that such objects may do anything they are able to do given the references
they hold. This means that the safety properties we verify for the model will
be true no matter how `Task` is actually imlemented.

See :ref:`Behaviour` for more information about defining behaviour.

Configuration
-------------
We can now define the initial configuration. We'll model two client objects: `clientA`, representing a single arbitrary client, and `otherClients`, aggregating all the others::

  config {
      Factory factory;
  
      setup {
          factory = new Factory();
      }
  
      test "A" {
          Object clientA = new Unknown(factory);
      }
  
      test "Other" {
          Object otherClients = new Unknown(factory);
      }
  }

The model is evaluated in two phases. First, all the `setup` blocks are run. Then, all the `test`
blocks. In this case using separate phases doesn't make any difference, but it can be useful to
avoid cluttering up the output with effects of the setup phase.

The labels on the test blocks (`"A"` and `"Other"`) are the the *modelling
context*. By giving them different contexts we tell the modeller to consider
these two cases separately when aggregating invocations.

See :ref:`Configuration` for more information.

Running the scenario
--------------------
Putting these together gives this complete model file (:example:`factory1`)::

  class Factory {
    public Task newInstance() {
      Task task = new Task();
      return task;
    }
  }
  
  config {
      Factory factory;
  
      setup {
          factory = new Factory();
      }
  
      test "A" {
          Object clientA = new Unknown(factory);
      }
  
      test "Other" {
          Object otherClients = new Unknown(factory);
      }
  }

You can run the model like this:

.. code-block:: sh

  $ sam factory1.sam

See :ref:`install` for more information about running SAM.

You should find you now have an output file called "factory1.png":

.. image:: _images/factory1.png

This shows that, given the behaviour and initial configuration:

* Some new Task objects will be created. SAM aggregates all those that may be created in context "A" as `taskA` and those created in "Other" as `taskOther`.
* clientA may get access to the `taskA` tasks.
* otherClients may get access to the `taskOther` tasks.
* The tasks may get references to their clients and to the factory.
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

For example, we can require that no other clients can get access to clientA's tasks::

  assert !getsAccess("otherClients", "taskA").
  assert getsAccess("clientA", "taskA").

.. note::
	SAM uses `Datalog <http://en.wikipedia.org/wiki/Datalog>`_ syntax to
	state facts and rules. Literal strings must be in double-quotes.
	Variable names (not used yet) are preceded by "?".


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
rest of the Internet too (:example:`factory2`)::

    test "A" {
        Object clientA = new Unknown(factory, otherClients);
    }

When we model this, SAM will detect that our safety goal is not met, and prints a simple
example of how the problem can occur:

.. code-block:: none

  debug()
     <= getsAccess('otherClients', 'taskA')
	<= otherClients: received taskA (arg to Unknown.*)
	   <= clientA: otherClients.*()
	      ...
	   <= clientA: got taskA
	      <= clientA: factory.newInstance()
	         ...
	      <= factory: new taskA()

  === Errors detected after applying propagation rules ===

  Assertion failed (factory2.sam:28): !getsAccess('otherClients', 'taskA')

You can read this as:

* The debugger was triggered because `otherClients` got access to `taskA`, which happened because:

  * `otherClients` got passed `taskA` as a method argument, which happened because:

    * `clientA` invoked `otherClients`, and
    * `clientA` had got `taskA`, because:

      * `clientA` had called `factory.newInstance` and
      * `factory` had created `taskA`.

.. note:: There is another problem with this model, which we will cover in the next section.
          SAM may report this (less obvious) problem instead of the example above.

The red arrow in the diagram corresponds to this problem, and the orange arrows show the
calls in the debugger's example:

.. image:: _images/factory2.png

This says that if we can't rely on clientA's behaviour then we can't be sure that
other clients won't get access to its tasks. To fix this, we must restrict clientA's
behaviour. For example, we can model clientA as having three separate fields:
"myTask", "myFactory" and "myOtherRefs". "myTask" will be the task(s) clientA created explicitly using
the factory, "myFactory" is the factory, and "myOtherRefs" will represent all other fields (aggregated)::

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

  showInvocation("factory", ?Invocation) :- isInvocation(?Invocation).

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
