.. _Behaviour:

Behaviour
=========

Behaviour predicates indicate what objects of the given type are willing to do.

.. function:: mayAccept(?Type, ?ParamVar)

   Objects of this type accept an argument value and store it in a variable namd ParamVar.

.. function:: mayAccept(?Type, ?ParamVar, ?Value)

   Objects of this type accept these argument values and store them in the
   variable namd ParamVar.

.. function:: hasCallSite(?Type, ?CallSite)

   These objects may perform the call described in :ref:`CallSite`.

.. function:: mayReturn(?Type, ?TargetResultVar)

   These objects may return the contents of TargetResultVar to their callers.

.. function:: mayCreate(?Type, ?ChildType, ?Var)

   These objects may create new objects of type ChildType and store the new instance in Var.

.. _CallSite:

Call-sites
----------
.. function:: mayCall(?CallSite, ?TargetVar)

   This call invokes the object stored in TargetVar.

.. function:: mayPass(?CallSite, ?ArgVar)

   This call passes ArgVar as an argument.

.. function:: mayStore(?CallSite, ?ResultVar)

   This result of this call is stored in ResultVar.

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
     hasLocal("Proxy", "result").
     mayAccept("Proxy", "msg", msg) :- isData(msg).
     hasCallSite("Proxy", "callsite1").
     mayReturn("Proxy", "result").

     mayCall("callsite1", "myTarget").
     mayPass("callsite1", "msg").
     mayStore("callsite1", "result").

     hasLocal("ProxyFactory", "proxy").
     mayAccept("ProxyFactory", "target").
     mayCreate("ProxyFactory", "Proxy", "proxy").
     mayReturn("ProxyFactory", "proxy").

The Unknown type
----------------
Objects of type "Unknown" are willing to accept any argument when invoked,
may invoke any object to which they have a reference, and may pass any argument
they are able to. They aggregate all fields into a single field named `ref`.
