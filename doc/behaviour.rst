.. _Behaviour:

Behaviour
=========

Behaviour predicates indicate what objects of the given type are willing to do.

.. function:: mayAccept(?Type, ?ParamVar)

   Objects of this type accept an argument value and store it in a variable namd ParamVar.

.. function:: mayAccept(?Type, ?ParamVar, ?Value)

   Objects of this type accept these argument values and store them in the
   variable namd ParamVar.

.. function:: mayCall(?Type, ?TargetVar, ?ArgVar, ?ResultVar)

   These objects may invoke the object in TargetVar, passing ArgVar as an argument and
   storing any result in ResultVar.

.. function:: mayReturn(?Type, ?TargetResultVar)

   These objects may return the contents of TargetResultVar to their callers.

.. function:: mayCreate(?Type, ?ChildType, ?Var)

   These objects may create new objects of type ChildType and store the new instance in Var.

For example, a Jave class that does::

     class Proxy {
       public Object invoke(Data msg) {
         Object result = myTarget.invoke(msg);
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
     mayCall("Proxy", "myTarget", "msg", "result").
     mayReturn("Proxy", "result").

     hasLocal("ProxyFactory", "proxy").
     mayAccept("ProxyFactory", "target").
     mayCreate("ProxyFactory", "Proxy", "proxy").
     mayReturn("ProxyFactory", "proxy").

The Unknown type
----------------
Objects of type "Unknown" are willing to accept any argument when invoked,
may invoke any object to which they have a reference, and may pass any argument
they are able to. They aggregate all fields into a single field named `ref`.
