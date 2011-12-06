.. highlight:: java

Composition problem
===================

In this example, we detect a problem with combining two access control
techniques. The situation is:

1. The `File` interface provides a `readOnly` method, which returns a read-only proxy to the file.
2. A reference can be wrapped by a `Logger`, which logs all operations performed.

For the `File` class, we define `read` and `write` methods, but we don't bother adding the arguments. SAM is only concerned with propagation of
references and these methods only take and return values::

  class File {
      public void write() { }
      public void read() { }

      public Object readOnly() {
          ReadOnly readOnly = new ReadOnly(this);
          return readOnly;
      }
  }

The `ReadOnly` class simply forwards the `read` method and returns `this` for `readOnly`::

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

The logger introduces some new syntax. Using `$method` in place of a method name matches any method (and stores the result in the local variable `method`).
So a `Logger` provides every possible method and may call the same method on the underlying object. `(arg*)` matches multiple arguments, so the logger
can also proxy calls which pass multiple arguments::

  class Logger {
      private Object myUnderlying;

      public Logger(Object underlying) {
          myUnderlying = underlying;
      }

      public Object $method(Object arg*) {
          Object value = myUnderlying.$method(arg*);
          return value;
      }
  }

For the test scenario, we create a new File and a logging, read-only reference to it.
We pass this to `delegate`, which has `Unknown` behaviour::

  config {
      Logger loggedReadOnly;

      setup {
          File file = new File();
          ReadOnly readOnly = file.readOnly();
          loggedReadOnly = new Logger(readOnly);
      }

      test {
          Unknown delegate = new Unknown(loggedReadOnly);
      }
  }

This shows another new feature: we have split the test case into separate `setup` and
`test` blocks. SAM evaluates the model in two "phases": first it activates the
`setup` blocks and adds all created objects to the model. Then it activates the
`test` blocks. The advantage of this is that the graph isn't cluttered with arrows
due to the setup phase, and the assertions don't fail because of actions that happened
during setup.

Our goals are that during the test the delegate should not be able to get access to `file` or
to `readOnly`::

  assert !getsAccess("delegate", "file").
  assert !getsAccess("delegate", "readOnly").

As an extra check, we can also require that the `file` object's `File.read` method must be executed
by someone (even though `delegate` can't invoke it directly), but not the `File.write` method.
Note that you must use the fully-qualified method name (`Class.method`) here::

  assert didCall(?X, "file", "File.read").
  assert !didCall(?X, "file", "File.write").

The `?X` here will match with any object. So these assertions say:

* Someone invoked file.read() (i.e. this model doesn't prevent the delegate in the real system from causing `read` to be invoked)
* No-one invoked file.write() (i.e. it is impossible for the delegate in the real system to cause `write` to be invoked)

Analysing this model reveals that it is not safe: `delegate` can bypass the
logger (:example:`compose`):

.. sam-output:: compose

The debug example is:

.. code-block:: none

  debug()
     <= getsAccess('delegate', 'readOnly')
        <= delegate: got readOnly
           <= delegate: loggedReadOnly.*()
              <= config: new delegate()
              <= delegate: delegate.*()
                 <= config: delegate.<init>()
                    <= config: new delegate()
              <= delegate: received loggedReadOnly (arg to Unknown.*)
                 <= delegate: delegate.*()
                 <= delegate: received loggedReadOnly (arg to Unknown.<init>)
                    <= config: delegate.<init>()
           <= loggedReadOnly: got readOnly
              <= loggedReadOnly: readOnly.readOnly()

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

      public Object *(Object arg*) {
          Object result = myUnderlying.*(arg*);
          Logger loggedResult = new Logger(result);
          return loggedResult;
      }
  }

Another would be to remove the `readOnly` method from the `File` interface,
forcing people to use `new ReadOnly` explicitly.
