Base predicates
===============

Types
-----

.. function:: isType(?Type)

   True if `Type` is the type of some object.

.. function:: hasLocal(?Type, ?VarName)

   There is a method on `Type` which has a local variable named `VarName`.

.. function:: hasField(?Type, ?VarName)

   There is a field on `Type` named `VarName`.

.. function:: hasVar(?Type, ?VarName)

   Has a local variable or field of this name.


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

