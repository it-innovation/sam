.. highlight:: java

The Confused Deputy
===================

This is the classic example of a problem that is hard to solve without capabilities (see
http://en.wikipedia.org/wiki/Confused_deputy_problem for more details). This
model is based on the example in the `Authodox <http://web.comlab.ox.ac.uk/people/toby.murray/tools/authodox>`_ tutorial.

1. Alice (an untrusted user) has access to a compiler.
2. The compiler has access to a billing log.
3. When invoked by Alice, the compiler should write to the output file and to its billing log.

The objects are configured like this::

  config {
      test {
          File billing = new File();
          Compiler compiler = new Compiler(billing);
          File input = new File();
          File output = new File();
          Object alice = new Unknown();

          alice.test(compiler, input, output);
      }
  }

In a non-capability system, references aren't secret. We want to see whether we can rely on just
the access control rules to protect the billing file. We therefore mark all objects as
:func:`isPublic`. Unknown objects get access to these automatically::

  isPublic(?X) :- isRef(?X).

In this system, instead of using only capabilities we also use traditional access control. This must be enabled using :func:`accessControlOn`, after which
the allowed interactions can be defined using :func:`accessAllowed`::

  accessControlOn.
  accessAllowed("alice", "compiler").
  accessAllowed("alice", "input").
  accessAllowed("alice", "output").
  accessAllowed("compiler", "billing", "File.write").
  accessAllowed("compiler", "output", "File.write").
  accessAllowed("compiler", "input", "File.read").

As usual, we first run the scenario with defined behaviour for `alice` to get
the baseline (:example:`includes/confusedMustCall`), remove the
:func:`checkCalls` for objects we don't care about, and then try again after
giving `alice` the `Unknown` behaviour.

Running this example (:example:`confused`) shows that it is not safe:

.. sam-output:: confused-attack

The debug example shows that:

  * <compiler>.exec:output.write called <billing>.write()
      * <compiler>.exec()'s output = <billing>
          * <compiler>.exec() received argument <billing>

So, if Alice tells the compiler that the output file is the compiler's billing file, the compiler will
overwrite the billing file (and Alice won't have to pay for using the service).
