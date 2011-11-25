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

  isPublic(?X) :- isObject(?X).

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

.. sam-output:: confused

The debug example shows that:

  * <compiler>.exec:output.write called <billing>.write()
      * <compiler>.exec()'s output = <billing>
          * <compiler>.exec() received argument <billing>

So, if Alice tells the compiler that the output file is the compiler's billing file, the compiler will
overwrite the billing file (and Alice won't have to pay for using the service).

Comparison with Authodox
------------------------

We have actually changed the scenario slightly from the Authodox original (`<http://www.cs.ox.ac.uk/people/toby.murray/tools/authodox/doc/index.html#authodox-tutorial>`_). The Authodox tutorial says that:

* The compiler will **Write** to the output file
* The compiler will **Append** to the billing file

The original problem statement (`<http://cap-lore.com/CapTheory/ConfusedDeputy.html>`_) does not mention "appending", and talks of "writing" in both cases; and while the paper does not mention the exact operating system used, under POSIX systems appending and writing would both be done using the "write" system call.

This change was necessary because otherwise Authodox would not be able to detect the problem, since the compiler writes to the billing file in the baseline case and in the attack case. However, the change makes
the example unconvincing, since it is highly unlikely that someone doing the modelling would make
the change, unless they already knew about the problem.

In SAM, this modification is not necessary. When SAM records a baseline it also records the particular
call-site used to make each call. So, SAM doesn't just record that `compiler` called `billing.write`, it
records that `compiler` called `billing.write` from the call-site `myLog.write()`.

In the attack scenario, SAM is therefore able to detect the unexpected access. The compiler is now writing
to the billing file from the `output.write()` call-site.

Another change is that the SAM model includes the input and output files. These were omitted from the
Authodox model for simplicity, but in SAM it's easy to add them so we do.
