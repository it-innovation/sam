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
  used as-is, but more complex code must be simplified. Note also that the final "}"
  defining a class must occur on a line of its own, and at the beginning of the line.

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

  initialInvocation("clientA", "A").
  initialInvocation("otherClients", "Other").

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
Adding a few standard imports to the top gives this complete model file (factory1.dl)::

  /* Imports */
  
  import("initial", "sam:base.dl").
  import("initial", "sam:graph.dl").
  import("final", "sam:system.dl").
  
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
  
  initialInvocation("clientA", "A").
  initialInvocation("otherClients", "Other").

You can run the model like this::

  $ sam factory1.dl

You should find you now have an output file called "access.dot.png":

.. image:: _images/factory1.png

This shows that, given the behaviour and initial configuration:

* Some new Task objects will be created. SAM aggregates all those that may be created in context "A" as `TaskA` and those created in "Other" as `TaskOther`.
* clientA may get access to the `TaskA` tasks.
* otherClients may get access to the `TaskOther` tasks.
* The factory gets a reference to all tasks but doesn't store the reference (the
  blue arrow indicates a local varibale rather than a field).

See :ref:`Graphing` for more information about the graphs produced.

Goals
-----
We can now decide what security properties to test. Two kinds of property are possible:

* *Safety properties*, which assert that something can never happen in the real system.
* *Liveness possibilties*, which assert that something isn't prevented by the model.

Because our model is an over-approximation of the real system, safety properties provide
a much stronger guarantee than liveness properties. Liveness properties are mainly useful
as a sanity check that the model isn't too restrictive.

For example, we can require that no other clients can get access to A's tasks::

  denyAccess('otherClients', 'TaskA').
  requireAccess('clientA', 'TaskA').

Unconfined clients
------------------

So far, we have assumed that the clients are *confined*. That is, we do not know their
behaviour but we know they don't start with access to anything except the factory. If
the clients are objects in a capability-based programming language then this may be
a reasonable assumption. If there are objects hosted by other parties then we should assume
that they have access to the Internet too.

We could add an explicit `internet` object to our model, but since there's no point having
two Unknown objects connected together (they'll share everything anyway), we'll just give
`clientA` a direct reference to `otherClients` and treat `otherClients` as including the
rest of the Internet too::

  field("clientA", "ref", "otherClients").

When we model this, SAM will detect that our safety goal is not met, and prints an example
of a sequence of steps that will cause the problem::

  Steps:
  1. clientA(A): ref = factory.newInstance(factory)
     factory(A): task = new Task()
     clientA(A): (ref = TaskA)
  2. clientA(A): ref = otherClients.invoke(TaskA)

  === Errors detected after applying propagation rules ===

  ('unsafe access may be possible', 'otherClients', 'TaskA')


The red arrow in the diagram corresponds to this problem, and the orange arrows show an
example sequence of steps that cause it:

.. image:: _images/factory2.png

.. note::
   Actually, there's another problem with this model, which means that SAM may find another,
   less obvious, sequence of steps when you try it. We'll look at this in the next section.

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
      myTask = factory();
      myTask = myTask(myTask);
    }
  }

This model is safe, though it puts rather strict limits on what clientA can do:

.. image:: _images/factory3.png

If we later want to modify clientA, we can update the model to check whether all our previous
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
      myTask = factory();
      ref = ref(ref);
    }
  }

Turning on display of invocations shows the reason:

.. image:: _images/factory4.png

The example reported is::

  Steps:
  1. clientA(A): ref = otherClients.invoke(otherClients)
  2. otherClients(A): ref = factory.newInstance(factory)
  3. clientA(A): myTask = factory.newInstance(otherClients)
     factory(A): task = new Task()
     otherClients(A): (ref = TaskA)

1. `clientA` calls `otherClients` (causing an invocation of it in context "A")
2. `otherClients` calls `factory` (creating a Task that is aggregated into `TaskA`)
3. (`clientA` calls `factory`). `factory` creates a Task. `otherClients` gets `TaskA` back from `factory`.

.. note::
  `clientA`'s call in step 3 isn't necessary to the proof (SAM doesn't always find the
  simplest example). In the verbose debug output, you can see that it used this call to prove
  that `factory` could be invoked in context "A"; something it could equally have deduced from
  `otherClients`'s call.

The problem here is that the default aggregation strategy groups all calls resulting from
actions by `clientA` under the "A" context. Because `clientA` invoked `otherClients`, tasks
created directly by `clientA` are grouped with tasks created by `otherClients`. Often this is
what you want (for example, if `otherClients` was instead some kind of proxy), but in this case
we want to treat them separately.

In fact, clientA may end up with references to two different groups of Tasks: those
`clientA` created directly using the factory, and those received from calls to other
objects.

We will therefore put `clientA`'s initial invocation into the "Other" group, and
tell SAM to put only the `myTask = factory.invoke()` invocation under "A"::

  initialInvocation("clientA", "Other").
  invocationObject("clientA", "Other", ?CallSite, "A") :- mayStore(?CallSite, "myTask").

With this division, the desired propery can be proved. `clientA` can now get access to tasks created
by other parties, but others still can't get access to the tasks by `clientA`.

.. image:: _images/factory5.png

We need to be careful here. While playing around with aggregation
strategies always leads to a correct over-approximation of the behaviour of the
system, note that our goal refers to `TaskA`. We have proved that `otherClients` never
gets access to `TaskA`, but which real tasks are in `TaskA` now, and which are in `TaskOther`?

We can state our goal more explicitly by saying that `otherClients` must not get access to any
reference that `clientA` may store in `myTask`::

  denyAccess('otherClients', ?Value) :- field('clientA', 'myTask', ?Value).

This means that if there is some way that `clientA` could create a new task, aggregated under
`TaskOther`, and store it in `myTask` then we would still detect the problem.
