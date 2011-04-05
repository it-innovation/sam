.. _Behaviour:

Behaviour
=========

Behaviour predicates indicate what objects of the given type are willing to do.
Behaviour can be specified using a Java-like syntax (which is translated into Datalog), or
in Datalog directly.

Java-like syntax
----------------
A type may be defined using the "class" keyword. This must occur at the start of a new line, and
continues until the next line starting with "}" (with no leading spaces). The syntax is::

  class NAME [extends SUPERCLASS] {
    // Zero or more fields, of the form:
    private TYPE NAME;

    // Zero or more methods, of the form:
    public TYPE NAME(PARAMS) {
      METHOD-BODY
    }
  }

Note: fields *must* be "private" and methods *must* be "public". Fields must come before methods.

Types are currently ignored (and treated safely as "Object"). Method names are currently ignored.

`PARAMS` is a comma-separated list of "TYPE NAME" pairs, as in Java.

`METHOD-BODY` is a list of statements, each of which is one of these::

  [TYPE] NAME = new TYPE(ARGS);
  [TYPE] NAME = NAME(ARGS);
  return NAME;

Where `NAME` is a variable name, `TYPE` is a class name, and `ARGS` is a comma-separated list of
variable names.


Predicates
----------

.. function:: mayAccept(?Type, ?ParamVar)

   Objects of this type accept an argument value and store it in a variable namd ParamVar.

.. function:: mayAccept(?Type, ?ParamVar, ?Value)

   Objects of this type accept these argument values and store them in the
   variable namd ParamVar.

.. function:: hasCallSite(?Type, ?CallSite)

   These objects may perform the call described in `CallSite` (see :ref:`CallSite`).

.. function:: mayReturn(?Type, ?TargetResultVar)

   These objects may return the contents of TargetResultVar to their callers.

.. _CallSite:

Call-sites
----------
.. function:: mayCall(?CallSite, ?TargetVar)

   This call invokes the object stored in TargetVar.

.. function:: mayPass(?CallSite, ?ArgVar)

   This call passes ArgVar as an argument.

.. function:: mayCreate(?Type, ?ChildType)

   This "call" (to the constructor) may create new objects of type ChildType.


Example
-------
For example, a Jave class that does::

     class Proxy {
       public Object invoke(Data msg) {
         Object result = myTarget.invoke(msg);	// callsite1
         return result;
       }
     }

     class ProxyFactory {
       public Proxy createProxy(Object target) {
         Proxy proxy = new Proxy(target);
         return proxy;
       }
     }

could be modelled with::

     hasField("Proxy", "myTarget").
     mayAccept("Proxy", "msg", msg) :- isData(msg).
     hasCallSite("Proxy", "callsite1").
     mayReturn("Proxy", "result").

     mayCall("callsite1", "myTarget").
     mayPass("callsite1", "msg").
     local(?Caller, ?Invocation, "result", ?Value) :- didGet(?Caller, ?Invocation, "callsite1", ?Value).

     mayAccept("ProxyFactory", "target").
     hasCallSite("ProxyFactory", "callsite2").
     mayReturn("ProxyFactory", "proxy").

     mayCreate("ProxyFactory", "Proxy").
     local(?Caller, ?Invocation, "proxy", ?Value) :- didCreate(?Caller, ?Invocation, "callsite2", ?Value).

The Unknown type
----------------
Objects of type "Unknown" are willing to accept any argument when invoked,
may invoke any object to which they have a reference, and may pass any argument
they are able to. They aggregate all fields into a single field named `ref`.
