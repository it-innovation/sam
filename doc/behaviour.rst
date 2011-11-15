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

Each `ANNOTATION` is of the form "@NAME(ARGS)", where NAME is the name of a datalog predicate, and
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
.. function:: hasField(?Type, ?FieldName)

   There is a field on `Type` named `VarName`.

.. function:: hasConstructor(?Type, ?Method)

   `Method` is a constructor for `Type`. Constructors work like other
   methods, but can only be called on an object if the called might have
   created it.

.. function:: hasMethod(?Type, ?Method)

   `Method` is a method on `Type`. This is a fully-qualified name,
   usually "Type.method".

Methods
-------
(and also constructors)

.. function:: methodName(?Method, ?MethodName)

   The name of the method. Usually, `Method` is fully-qualified (e.g. "Type.invoke") whereas `MethodName`
   is just the name ("invoke").

.. function:: mayAccept(?Method, ?ParamVar, ?Pos)

   Objects of this type accept an argument value passes in the given position
   and store it in a variable namd ParamVar. The first argument has position 0.
   If `Pos` is -1, then the parameter may accept values passed in any position.

.. function:: mayAccept(?Method, ?ParamVar, ?Pos, ?Value)

   Objects of this type accept these argument values and store them in the
   local variable namd ParamVar.

.. function:: hasCallSite(?Method, ?CallSite)

   This method may perform the call described in `CallSite` (see :ref:`CallSite`).

.. function:: mayReturn(?Object, ?Invocation, ?Method, ?Value)

   This method invocation may return `Value` to its callers.

.. function:: mayThrow(?Object, ?Invocation, ?Method, ?Exception)

   This method invocation may throw `Exception` to its callers.

.. _CallSite:

Call-sites
----------
.. function:: mayCallObject(?Caller, ?CallerInvocation, ?CallSite, ?Target)

   This call invokes `Target`.

.. function:: callsMethod(?CallSite, ?MethodName)

   This call-site may call methods named `MethodName`.

.. function:: callsAnyMethod(?CallSite)

   This call-site may call methods with any name.

.. function:: mayReceive(?Target, ?TargetInvocation, ?Method, ?Pos, ?ArgValue)

   Target.method may get called with `ArgValue` as parameter number `Pos` (or as any
   parameter if `Pos` is `-1`).

.. function:: mayCreate(?CallSite, ?ChildType, ?NameHint)

   This "call" (to the constructor) may create new objects of type ChildType.
   There is no need for a `callsMethod` here; `mayCreate` implies that it may
   call the constructor(s). `NameHint` is used to create a suitable name for the
   new child object. Usually, this is the name of the variable it will be assigned
   to.


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
