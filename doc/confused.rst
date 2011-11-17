.. highlight:: java

The Confused Deputy
===================

This is the classic example of a problem that is hard to solve without capabilities (see
http://en.wikipedia.org/wiki/Confused_deputy_problem for more details). This
model is based on the example in the `Authodox <http://web.comlab.ox.ac.uk/people/toby.murray/tools/authodox>`_ tutorial.

1. Alice (an untrusted user) has access to a compiler.
2. The compiler has access to a billing log.
3. Alice should be able to cause her output file to be written to and the compiler's log to be appended to (and nothing else).

The four objects are configured like this::

  config {
      File billing;
      Compiler compiler;
      File output;
      Unknown alice;

      setup {
          billing = new File();
          compiler = new Compiler(billing);
          output = new File();
          alice = new Unknown(compiler, output);
      }

      test {
          alice.test();
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
  accessAllowed("alice", "output").
  accessAllowed("compiler", "billing").
  accessAllowed("compiler", "output").

Following Authodox, we define the actions we think should be possible as a result of Alice's actions
(using :func:`mayCall`)::

  mayCall("compiler", "output", "File.write").
  mayCall("compiler", "billing", "File.append").
  checkCalls("billing").

Running this example (:example:`confused`) shows that it is not safe:

.. sam-output:: confused

The debug example is:

.. code-block:: none

  debug()
     <= compiler: billing.write()
        <= alice: compiler.exec()
        <= compiler: received billing (arg to Compiler.exec)
           <= alice: compiler.exec()
