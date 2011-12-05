Base predicates
===============

Types
-----

.. function:: isType(String type)

   True if `Type` is the type of some object.

.. function:: definedType(String type)

   A type whose behaviour was defined in the model file. This is used to give
   undefined types the Unknown behaviour, rather than no behaviour::

     isA(?Object, "Unknown") :- isA(?Object, ?Type), !definedType(?Type).


Objects
-------

.. function:: live(Ref object)

   The object exists. This is true for all the initial objects and for any
   objects that may be created. This will be false for potential child
   objects that can never be created in the current configuration.

.. function:: isRef(Ref ref)

   This object is a reference type (it is passed by reference).

.. function:: isConstant(Object object)

   This object is a constant type (it is passed by value).

.. function:: isA(Ref object, String type)

   The object has the given type. Note than an object may have multiple
   types, either because of inheritance or because of aggregation.

.. function:: field(Ref object, String fieldName, Object value)

   The object has a field called `FieldName` which may contain `Value`.

.. function:: isPublic(Ref object)

   Marking an object as public means that references to it are not shown on the
   access graph. This is useful for objects that are widely accessible, to avoid
   cluttering up the graph.

   All objects of type Unknown automatically get a reference
   to all public objects (and will therefore also invoke them, unless
   :ref:`access` rules prevent this).

Invocations
-----------

.. function:: liveMethod(Ref object, String invocation, String method)

   It is possible for `Object.Method` to be invoked in the context `Invocation`.

.. function:: isInvocation(String invocation)

   There is some invocation with the context `Invocation`.

.. function:: local(Ref object, String invocation, String varName, Object value)

   In some invocation of `Object` in context `Invocation`, local variable
   `VarName` has value `Value`.

Results
-------
These predicates indicate behaviour that may be possible given the behaviour and configuration of the
system. They are named "did" to indicate that they are the result of applying the system rules - in the *model*
everything that is possible "did" happen, even though in the real system this only represents things that
might happen.

.. function:: hasRef(Ref object, Ref target)

   `Object` has a local variable or field with the given value.

.. function:: didAccept(Ref target, String targetInvocation, String paramVar, Object argValue)

   `Target` was invoked with the given value passed as an argument.

.. function:: didCall(Ref caller, String callerInvocation, String callSite, Ref target, String method)

   `Caller`'s `CallSite` called `Target`'s `Method`.

.. function:: didCall(Ref caller, String callerInvocation, String callSite, Ref target, String targetInvocation, String method)

   `Caller`'s `CallSite` called `Target`'s `Method`, switching to the `TargetInvocation` context.

.. function:: didCall(Ref caller, Ref target, String method)

   Simpler version of `didCall/6` with just the caller, target and method.

.. function:: didCreate(Ref caller, String invocation, String callSite, Ref newChild)

   The code at `CallSite` created `NewChild` as the result of a constructor call made
   by object `Caller` in context `CallerInvocation`.

.. function:: didCreate(Ref factory, Ref object)

   Simplified view of :func:`didCreate`/4.

.. function:: didGetException(Ref caller, String callerInvocation, String callSite, Object exception)

   `Exception` was thrown by `Caller`'s `CallSite`'s target.

.. function:: didGet(Ref caller, String callerInvocation, String callSite, Object resultValue)

   The code at `CallSite` got `ResultValue` back as the result of a call made
   by object `Caller` in context `CallerInvocation`.

.. function:: getsAccess(Ref sourceObject, Ref targetObject)

   Some invocation of `SourceObject` may have access to `TargetObject` (through a field or local variable).

.. function:: didReceive(Ref target, String targetInvocation, String method, int pos, Object argValue)

   Target.method may get called with `ArgValue` as parameter number `Pos` (or as any
   parameter if `Pos` is `_`). ?Pos will be a position in `Method`'s :func:`hasParam`.

Functions
---------
These are not relations, so you can't enumerate all their values, but you can use them in rules.

.. function:: IS_REF(Ref ref)

   Checks that ref is a Ref.

.. function:: IS_STRING(String string)

   Checks that string is a String. Note: this method does not consider `any(String)` to be a string. It is usually better
   to use `ASSIGN("String", ?StringIn, ?StringOut)`. For example, if `StringIn` is `any(Value)` then `StringOut` will be
   `any(String)`.

.. function:: TO_STRING(Object object, String string)

   Converts `any` to a String.

.. function:: ASSIGN(String Type, Object value, Type result)

   Checks that `value` can be assigned to a field of the given type. The `result` parameter is needed to handle `any` types. Some
   examples should make this clear::

	ASSIGN("String", "hi") -> "hi"
	ASSIGN("String", 4) -> nothing
	ASSIGN("String", any(Value)) -> any(String)
	ASSIGN("String", any(int)) -> nothing

.. function:: MATCH(Object a, Object b, Object result)

   Tests whether `a = b`, taking account of the fact that either may be an `any` value. The `result` is the intersection
   of the possible values. e.g.::

       MATCH("foo", "foo") -> "foo"
       MATCH("foo", "bar") -> nothing
       MATCH(any(String), any(Value)) -> any(String)
