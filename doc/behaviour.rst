.. highlight:: java

.. _Behaviour:

Behaviour
=========

Behaviour predicates indicate what objects of the given type are willing to do.
Behaviour can be specified using a Java-like syntax (which is translated into Datalog), or
in Datalog directly.

Java-like syntax
----------------
A type may be defined using the "class" keyword. The syntax is::

  class NAME [extends SUPERCLASS] {
    // Zero or more fields, of the form:
    private TYPE NAME;

    // Zero or more constructors, of the form:
    public TYPE(PARAMS) {
      CODE
    }

    // Zero or more methods, of the form:
    ANNOTATION*
    public TYPE NAME(PARAMS) {
      CODE
    }
  }

Note: fields *must* be "private" and methods *must* be "public". Fields must
come before constructors, which come before methods.

Types are currently ignored (and treated safely as "Object").

`PARAMS` is a comma-separated list of "TYPE NAME" pairs, as in Java.

`CODE` is a list of statements, each of which is one of these::

  [TYPE] NAME = new TYPE(ARGS);
  [TYPE] NAME = NAME.METHOD(ARGS);
  [TYPE] NAME = NAME;
  return NAME;
  throw NAME;
  try { CODE } catch (TYPE NAME) { CODE }

Where `NAME` is a variable name, `TYPE` is a class name, `METHOD` is a method name,
and `ARGS` is a comma-separated list of variable names.

Each `ANNOTATION` is of the form "@NAME(ARGS)", where NAME is the name of a Datalog predicate, and
asserts a fact for this method. For example, the annotations in this code::

  class Foo {
    @restricted
    @permittedRole("admin")
    public void destroy() {}
  }

have the same effect as::

  restricted("Foo.destroy").
  permittedRole("Foo.destroy", "admin").


Classes
-------
.. function:: hasField(String type, String fieldName)

   There is a field on `Type` named `VarName`.

.. function:: hasConstructor(String type, String method)

   `Method` is a constructor for `Type`. Constructors work like other
   methods, but can only be called on an object if the called might have
   created it.

.. function:: hasMethod(String type, String method)

   `Method` is a method on `Type`. This is a fully-qualified name,
   usually "Type.method".

Methods
-------
(and also constructors)

.. function:: methodName(String method, String methodName)

   The name of the method. Usually, `Method` is fully-qualified (e.g. "Type.invoke") whereas `MethodName`
   is just the name ("invoke").

.. function:: mayAccept(String method, String paramVar, int pos)

   Objects of this type accept an argument value passes in the given position
   and store it in a variable named ParamVar. The first argument has position 0.
   If `Pos` is -1, then the parameter may accept values passed in any position.

.. function:: hasCallSite(String method, String callSite)

   This method may perform the call described in `CallSite` (see :ref:`CallSite`).

.. function:: mayReturn(Ref object, String invocation, String method, Object value)

   This method invocation may return `Value` to its callers.

.. function:: mayThrow(Ref object, String invocation, String method, Object exception)

   This method invocation may throw `Exception` to its callers.

.. function:: hasParam(String method, String paramVar, int pos)

   This method has a parameter with the given name and position (starting from 0).
   If the method accepts parameters sent at any position, `Pos = -1`.

.. _CallSite:

Call-sites
----------
.. function:: mayCallObject(Ref caller, String callerInvocation, String callSite, Object target)

   This call invokes `Target`.

.. function:: callsMethod(String callSite, String methodName)

   This call-site may call methods named `MethodName`.

.. function:: callsAnyMethod(String callSite)

   This call-site may call methods with any name.

.. function:: maySend(Ref caller, String callerInvocation, String callSite, int pos, Object argValue)

   CallSite may send the value `ArgValue` as parameter number `Pos` (or as any
   parameter if `Pos` is `-1`). Includes all values from :func:`maySendFromAnyContext`
   for invocations that were active.

.. function:: maySendFromAnyContext(Ref caller, String callSite, int pos, Object argValue)

   As for :func:`maySend`, but the value may be sent in any context (e.g. it is a field
   or a literal).

.. function:: mayCreate(String callSite, String childType, String nameHint)

   This "call" (to the constructor) may create new objects of type ChildType.
   There is no need for a `callsMethod` here; `mayCreate` implies that it may
   call the constructor(s). `NameHint` is used to create a suitable name for the
   new child object. Usually, this is the name of the variable it will be assigned
   to.

.. function:: catchesAllExceptions(String callSite)

   This call-site handles all exceptions (i.e. it is inside a try block that
   catches "Throwable"). Without this, it is assumed that all exceptions may
   propagate.

The Unknown type
----------------
Objects of type "Unknown" are willing to accept any argument when invoked,
may invoke any object to which they have a reference, and may pass any argument
they are able to. They aggregate all fields into a single field named `ref`.

There is also a BaseUnknown type, which has the same behaviour definition as Unknown. However, `Unknown`
objects have some useful extra properties by default:

- `Unknown` objects are active by default (`BaseUnknown` objects can't act unless invoked)
- `Unknown` objects have an access control policy that allows access by anyone
- `Unknown` objects have references to all :func:`isPublic` objects

You should use `Unknown` in most cases. Use `BaseUnknown` if you need to avoid these defaults
(e.g. because you have some untrusted code that is still controlled by an access policy).


The Value type
--------------
Objects of type "Value" represent pure values (e.g. strings and numbers). It is not usually
necessary to model these in SAM, but if you do need to pass them around then mark them as
`isA("myValue", "Value")` to avoid errors about them not being objects. Values are not shown
on the graph. They have no behaviour and cannot hold references to other objects.


Embedding Datalog
-----------------
In addition to the standard Java syntax, it is possible to assign variables using Datalog
rules. The syntax is::

  [TYPE] NAME = VAR :- QUERY;

For example, an object that only stores value types (int, string, etc) rather than references
can be modelled as::

  class ValueStore {
      private Object myValue;

      public void store(Object value) {
          myValue = value :- isA(value, "Value");
      }
  }

You can use any Datalog query as the test and you can mix Java variables, Datalog variables and "special"
variables freely. The special variables recognised are:

* `$Context` -- the context in which the variable is being assigned
* `$Caller` -- the object (or objects) which called this method (in `$Context`)

Note on "private"
-----------------
There is a subtle difference between SAM and Java in the meaning of "private":

* In SAM, a private member can only be accessed by the object itself.
* In Java, a private member can be accessed by any other instance of the same class.

For example, in Java you can do this::

  class WebStore {
    private Database customerInformation;

    public void comparePricesWithCompetition(WebStore competitor) {
      competitor.getPublicPrices();
      ...
      competitor.customerInformation.download();
      ...
    }
  }

In SAM, replacing any behaviour definition with `Unknown` should only allow more access to occur, not less. Therefore, if we took this
interpretation of `private` then `Unknown` would need access to all private fields and methods of all objects, which would clearly not be
useful.

However, we also want to avoid reporting that a SAM model is safe when the identical Java code would not be. SAM's solution is that:

* all methods must be public, and
* there is no syntax for accessing fields on another object.

Therefore:

* If you define a behaviour (class) in SAM then the definition automatically says that the real system can't call fields on another object directly, since
  there is no way to express this in SAM syntax.
* If you leave the behaviour undefined then the real system would still be safe even if all members were public.
