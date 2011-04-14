Composition problem
===================

This example detects a problem with combining two access control techniques. The
situation is:

1. The `File` interface provides a `readOnly` method, which returns a read-only proxy to the file.
2. A reference can be wrapped by a `Logger`, which logs all operations performed.

The `owner` creates a new Directory and gives a logging, read-only reference to
`delegate`::

        Directory dir = new Directory();
        Object readOnly = dir.readOnly();
        Object loggedReadOnly = new Logger(readOnly);

        delegate.read(loggedReadOnly);

Analysing this model reveals that it is not safe: `delegate` can bypass the logger:

.. image:: _images/compose.png

The debug example is::

  debug()
     <= getsAccess('delegate', 'aReadOnly')
        <= delegate: got aReadOnly
           ...
           <= aLogger: got aReadOnly
              <= aLogger: aReadOnly.readOnly()
              ...

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
(and arguments) with their own loggers. Another would be to remove the
`readOnly` method from the `File` interface, forcing people to use `new ReadOnly`
explicitly.
