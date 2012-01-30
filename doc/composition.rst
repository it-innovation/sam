.. highlight:: java

Composition problem
===================

In this example, we detect a problem with combining two access control
techniques. The situation is:

1. The `File` interface provides a `readOnly` method, which returns a read-only proxy to the file.
2. A reference can be wrapped by a `Logger`, which logs all operations performed.

Full access
-----------
With no access controls, we have a file and a delegate. We give the delegate full access to the file, and
the delegate reads from it::

  class File {
      public void write() { }
      public void read() { }
  }

  class Delegate {
      public void test(Object file) {
          file.read();
      }
  }

  config {
      Object file;

      setup {
          file = new File();
      }

      test {
          Object delegate = new Delegate();
          delegate.test(file);
      }
  }

Note that we have split the test case into separate `setup` and
`test` blocks. SAM evaluates the model in two "phases": first it activates the
`setup` blocks and adds all created objects to the model. Then it activates the
`test` blocks. The advantage of this is that the graph isn't cluttered with arrows
due to the setup phase, and the assertions don't fail because of actions that happened
during setup.

The read-only proxy
-------------------
If we follow the usual SAM approach of saving a base-line (`File` -> `Export calls`), importing the
resulting `mustCall.sam` file, and changing the behaviour of the delegate to `Unknown`, we discover
that `File.write` may be called.

To prevent `delegate` from calling `write`, we create a new `ReadOnly` class. Calling `readOnly` on
a `File` returns a read-only proxy to it::

  class File {
      public void write() { }
      public void read() { }

      public Object readOnly() {
          Object readOnly = new ReadOnly(this);
          return readOnly;
      }
  }

We define the `ReadOnly` class to proxy only the methods we desire::

  class ReadOnly {
      private Object myUnderlying;

      public ReadOnly(Object underlying) {
          myUnderlying = underlying;
      }

      public Object read() {
          Object value = myUnderlying.read();
          return value;
      }

      public Object readOnly() {
          return this;    // Already read-only
      }
  }

.. sam-output:: compose-1-ro-only-baseline

Testing this in SAM reveals that the delegate may call `readOnly`, but this is harmless so we allow it
(by adding an :func:`AnyoneMayCall` annotation to the `readOnly` methods).

The delegate cannot cause the `write` method to be called, so we have achieved our goal.


The logging proxy
-----------------

The logger introduces some new syntax. Using `$method` in place of a method name matches any method (and stores the result in the local variable `method`).
So a `Logger` provides every possible method and may call the same method on the underlying object. `(arg*)` matches multiple arguments, so the logger
can also proxy calls which pass multiple arguments::

  class Logger {
      private Object myUnderlying;

      public Logger(Object underlying) {
          myUnderlying = underlying;
      }

      @GroupAs("Logged")
      public Object $method(Object arg*) {
          Object value = myUnderlying.$method(arg*);
          return value;
      }
  }

We need some way to detect that logging has been performed. Here, we use the :func:`GroupAs` annotation,
which causes all calls to this method to be aggregated into the `Logged` context. All calls made from here
will be in the same context.

The test case is similar to before: we wrap the file with a `Logger` and pass that to the delegate.
We have also set the default context to "NotLogged" for clarity::

  config {
      Object file;
      Object logged;

      setup {
          file = new File();
          logged = new Logger(file);
      }

      test "NotLogged" {
          Object delegate = new Delegate();
          delegate.test(logged);
      }
  }

.. sam-output:: compose-2-logging-only-baseline

Testing this model shows that it is safe. Even with an `Unknown` delegate, the file object is only
ever called in the `Logged` context.

Combining the two mechanisms
----------------------------

Finally, we'll try using both of these mechanisms together. We first wrap the file in a `ReadOnly`,
and then wrap that in a `Logger`. In the base-line case, we see that the only invocation of `file` is
`file.read` in the `Logged` context.

However, changing the type of `delegate` to `Unknown` reveals that the design is not
safe: `delegate` can bypass the logger:

.. sam-output:: compose-3-both-baseline

The errors reported are:

.. code-block:: none

   <readOnly>.read:value=myUnderlying.read called <file>.read() [NotLogged]
   <delegate>.*:ref=ref.* called <readOnly>.read() [NotLogged]

The debug example for the second case (simplified) shows:

.. code-block:: none

   * <delegate>.*:ref=ref.* called <readOnly>.read() [NotLogged]
       * <delegate>.ref = <readOnly>
           * <logged>.$method returned <readOnly>
               * <readOnly>.readOnly returned <readOnly>

So `delegate` was able to call `file.read` without logging because it had direct access to
`readOnly` (not just indirect access via `logged`). It got that because `logged` returned it,
which it did because `readOnly.readOnly()` returned it.

The problem here is the implementation of `ReadOnly.readOnly`::

  class ReadOnly {
      ...
      public Object readOnly() {
          return this;    // Already read-only
      }
  }

If someone has direct access to a `ReadOnly` object then this is reasonable. But
if someone has access to a proxy to a `ReadOnly` object, this method lets them turn
this indirect access into direct access.

This is a realistic example: the E `File` interface provides many methods like this
(e.g. `File.deepReadOnly`).

One solution to this problem would be to change `Logger` to wrap the return values
(and arguments) with their own loggers::

  class Logger {
      private Object myUnderlying;

      public Logger(Object underlying) {
          myUnderlying = underlying;
      }

      @GroupAs("Logged")
      public Object $method(Object arg*) {
          Object result = myUnderlying.$method(arg*);
          Logger loggedResult = new Logger(result);
          return loggedResult;
      }
  }

Another would be to remove the `readOnly` method from the `File` interface,
forcing people to use `new ReadOnly` explicitly.
