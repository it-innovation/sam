Base predicates
===============

Types
-----

.. function:: isType(?Type)

   True if `Type` is the type of some object.

Objects
-------

.. function:: isObject(?Object)
.. function:: live(?Object)

   The object exists. This is true for all the initial objects and for any
   objects that may be created. This will be false for potential child
   objects that can never be created in the current configuration.

.. function:: isA(?Object, Type)

   The object has the given type. Note than an object may have multiple
   types, either because of inheritance or because of aggregation.

.. function:: field(?Object, ?FieldName, ?Value)

   The object has a field called `FieldName` which may contain `Value`.

Invocations
-----------

.. function:: live(?Object, ?Invocation)

   It is possible for `Object` to be invoked in the context `Invocation`.

.. function:: isInvocation(?Invocation)

   There is some invocation with the context `Invocation`.

.. function:: local(?Object, ?Invocation, ?VarName, ?Value)

   In some invocation of `Object` in context `Invocation`, local variable
   `VarName` has value `Value`.

.. function:: value(?Object, ?Invocation, ?VarName, ?Value)

   In some invocation of `Object` in context `Invocation`, the variable
   `VarName` has value `Value`. It may be either a local variable or a field
   on `Object`.

Results
-------

.. function:: didGet(?Caller, ?CallerInvocation, ?CallSite, ?ResultValue)

   The code at `CallSite` got `ResultValue` back as the result of a call made
   by object `Caller` in context `CallerInvocation`.

.. function:: didCreate(?Caller, ?Invocation, ?CallSite, ?Child)

   The code at `CallSite` created `Child` as the result of a constructor call made
   by object `Caller` in context `CallerInvocation`.

.. function:: getsAccess(?Object, ?Value)

   After applying the propagation rules, some invocation of `Object` may have access to `Value`
   (through a field or local variable).
