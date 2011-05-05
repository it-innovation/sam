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

.. function:: isA(?Object, ?Type)

   The object has the given type. Note than an object may have multiple
   types, either because of inheritance or because of aggregation.

.. function:: field(?Object, ?FieldName, ?Value)

   The object has a field called `FieldName` which may contain `Value`.

Invocations
-----------

.. function:: liveMethod(?Object, ?Invocation, ?Method)

   It is possible for `Object.Method` to be invoked in the context `Invocation`.

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
These predicates indicate behaviour that may be possible given the behaviour and configuration of the
system. They are named "did" to indicate that they are the result of applying the system rules - in the *model*
everything that is possible "did" happen, even though in the real system this only represents things that
might happen.

.. function:: didAccept(?Target, ?TargetInvocation, ?ParamVar, ?ArgValue)

   `Target` was invoked with the given value passed as an argument.

.. function:: didCall(?Caller, ?CallerInvocation, ?CallSite, ?Target, ?TargetInvocation, ?Method)

   `Caller`'s `CallSite` called `Target`'s `Method`.

.. function:: didCreate(?Caller, ?Invocation, ?CallSite, ?NewChild)

   The code at `CallSite` created `NewChild` as the result of a constructor call made
   by object `Caller` in context `CallerInvocation`.

.. function:: didGetException(?Caller, ?CallerInvocation, ?CallSite, ?Exception)

   `Exception` was thrown by `Caller`'s `CallSite`'s target.

.. function:: didGet(?Caller, ?CallerInvocation, ?CallSite, ?ResultValue)

   The code at `CallSite` got `ResultValue` back as the result of a call made
   by object `Caller` in context `CallerInvocation`.

.. function:: getsAccess(?SourceObject, ?TargetObject)

   Some invocation of `SourceObject` may have access to `TargetObject` (through a field or local variable).
