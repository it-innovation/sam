.. highlight:: java

The Confused Deputy
===================

This is the classic example of a problem that is hard to solve without capabilities. This
model is based on the example in the `Authodox <http://web.comlab.ox.ac.uk/people/toby.murray/tools/authodox>`_ tutorial.

1. Alice (an untrusted user) has access to a compiler.
2. The compiler has access to a billing log.
3. Alice should be able to cause her output file to be written to and the compiler's log to be appended to (and nothing else).

The four objects are::

  initialObject("alice", "Unknown").
  initialObject("billing", "File").
  initialObject("compiler", "Compiler").
  initialObject("output", "File").

We give Alice a reference to `billing` because in a non-capability system references aren't secret. We want to see whether we can rely on
the access control rules to protect the billing file::

  field("alice", "ref", "compiler").
  field("alice", "ref", "output").
  field("alice", "ref", "billing").
  field("compiler", "myLog", "billing").

In this system, instead of using only capabilities we also use traditional access control. This must be enabled using :func:`accessControlOn`, after which
the allowed interactions can be defined using :func:`accessAllowed`::

  accessControlOn.
  accessAllowed("alice", "compiler").
  accessAllowed("compiler", "billing").
  accessAllowed("compiler", "output").

Following Authodox, we define the actions we think should be possible as a result of Alice's actions::

  // Flag an error if an interaction caused by alice (but not involving alice)
  // happens that isn't in desiredAuthority.
  declare desiredAuthority(?Source, ?Target, ?Method).
  haveBadAccess(?Caller, ?Target) :-
      didCall(?Caller, ?CallerInvocation, ?CallSite, ?Target, ?TargetInvocation, ?Method),
      ?Caller != "alice", ?Target != "alice",
      !desiredAuthority(?Caller, ?Target, ?Method).

  // The only things that should be able to happen which don't involve Alice are:
  // 1. compiler invokes output.write()
  // 2. compiler invokes billing.append()
  desiredAuthority("compiler", "output", "File.write").
  desiredAuthority("compiler", "billing", "File.append").

Running this example (:example:`confused`) shows that it is not safe:

.. image:: _images/confused.png

The debug example is:

.. code-block:: none

  debug()
     <= compiler: billing.write()
        <= alice: compiler.exec()
        <= compiler: received billing (arg to Compiler.exec)
           <= alice: compiler.exec()

.. tip::
  By defining the error using :func:`haveBadAccess` (rather than using :func:`debug` or :func:`error`), we get
  a nice red arrow on the diagram automatically.
