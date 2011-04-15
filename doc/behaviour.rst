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


Classes
-------
.. function:: hasField(?Type, ?VarName)

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

.. function:: mayAccept(?Method, ?ParamVar)

   Objects of this type accept an argument value and store it in a variable namd ParamVar.

.. function:: mayAccept(?Method, ?ParamVar, ?Value)

   Objects of this type accept these argument values and store them in the
   local variable namd ParamVar.

.. function:: hasCallSite(?Method, ?CallSite)

   This method may perform the call described in `CallSite` (see :ref:`CallSite`).

.. function:: mayReturn(?Object, ?Invocation, ?Method, ?Value)

   This method invocation may return `Value` to its callers.

.. _CallSite:

Call-sites
----------
.. function:: mayCallObject(?Caller, ?CallerInvocation, ?CallSite, ?Target)

   This call invokes `Target`.

.. function:: callsMethod(?CallSite, ?MethodName)

   This call-site may call methods named `MethodName`.

.. function:: callsAnyMethod(?CallSite)

   This call-site may call methods with any name.

.. function:: maySend(?Target, ?TargetInvocation, ?Method, [?Pos,] ?ArgValue),

   Target.method may get called with `ArgValue` as parameter number `Pos` (or as any
   parameter in the version without `Pos`.

.. function:: mayCreate(?CallSite, ?ChildType)

   This "call" (to the constructor) may create new objects of type ChildType.
   There is no need for a `callsMethod` here; `mayCreate` implies that it may
   call the constructor(s).


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
     hasMethod("Proxy", "Proxy.invoke").

     methodName("Proxy.invoke", "invoke").
     mayAccept("Proxy.invoke", "msg", msg) :- isData(msg).
     hasCallSite("Proxy.invoke", "callsite1").
     mayReturn(?Object, ?Invocation, "Proxy.invoke", ?Result) :-
       isA(?Object, "Proxy"),
       live(?Object, ?Invocation),
       local(?Object, ?Invocation, "result", ?Value).

     mayCall("callsite1", "myTarget").	// FIXME
     callsMethod("callsite1", "invoke").
     mayPass("callsite1", "msg").
     local(?Caller, ?Invocation, "result", ?Value) :- didGet(?Caller, ?Invocation, "callsite1", ?Value).

     mayAccept("ProxyFactory.createProxy", "target").
     hasCallSite("ProxyFactory.createProxy", "callsite2").
     mayReturn(?Object, ?Invocation, "ProxyFactory.createProxy", ?Result) :-
       isA(?Object, "ProxyFactory"),
       live(?Object, ?Invocation),
       local(?Object, ?Invocation, "proxy", ?Value).

     mayCreate("callsite2", "Proxy").
     mayPass("callsite2", "target").
     local(?Caller, ?Invocation, "proxy", ?Value) :- didCreate(?Caller, ?Invocation, "callsite2", ?Value).

The Unknown type
----------------
Objects of type "Unknown" are willing to accept any argument when invoked,
may invoke any object to which they have a reference, and may pass any argument
they are able to. They aggregate all fields into a single field named `ref`.
