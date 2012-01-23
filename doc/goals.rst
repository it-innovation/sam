Goals
=====

The general syntax for a goal is::

  assert GOAL.

For example, the ensure that `compiler` called `billing.append(...)` but not `billing.write(...)`::

  assert didCall("compiler", "billing", "File.append").
  assert !didCall("compiler", "billing", "File.write").

If an assertion fails, SAM will fail the model and display an error message
giving the location of the assertion that failed. It will also run the debugger
to examine the cause of the failure.

If an assertion includes variables, then the assertion succeeds if any match is
found. For example, to ensure that the compiler called some method of `billing` and
no methods of `precious`::

  assert didCall("compiler", "billing", ?AnyMethod).
  assert !didCall("compiler", "precious", ?AnyMethod).

If the predicate used in the goal includes two objects, then SAM will also add a
suitable red arrow to the diagram.


Comparing against a base-line
-----------------------------

It is often useful to look at changes to a model. A good approach is as follows:

1. Create a model containing just the minimal set of required components, all with
   trusted behaviour (and access control, if any, turned off).

2. Use `File -> Export calls` to dump all the possible invocations and objects.

3. Import the `mustCall.sam` file from your model.

4. Add objects for Unknown actors and re-enable any access control policies. SAM will
   verify that all the calls in the minimal safe model are still possible, and that
   no new calls can be made on the `checkCalls` objects.


Predicates
----------

.. function:: denyAccess(Ref object, Ref target)

   Verify that it is impossible for `Object` to ever get access to
   `Target`. i.e. `Object` must never be able to get a local variable or
   field with a reference to `Target`.

.. function:: requireAccess(Ref object, Ref target)

   Check that the model does not exclude the possibility of `Object`
   getting access to `Target`. Note that (unlike `denyAccess` above), this
   check cannot be exact: a real system could conform to the model but
   still not allow this. However, it is a useful sanity check that your
   model is not over-constrained.

.. function:: error(?Message, ?Args...)

   Fail the model checking and print the message and arguments as the
   error (up to four extra arguments are allowed). The base library
   creates errors automatically in many cases (e.g. if `denyAccess`
   fails), but you can also specify them manually, e.g.::

     error("Store contains non-Value item", ?Store, ?Item) :-
       isA(?Store, "Store"),
       field(?Store, "data", ?Item),
       !isA(?Item, "Value").

.. function:: haveBadAccess(Ref sourceObject, Ref targetObject)

   Fail the model and indicate the problem with a red arrow on the graph.

.. function:: missingGoodAccess(Ref sourceObject, Ref targetObject)

   Fail the model and indicate the problem with a dotted red arrow on the graph.

.. function:: expectFailure()

   Indicates that this scenario is expected to fail. Normally, SAM exits with a status
   code of 0 if the model passes, or 1 on failure. This reverses the test.

.. function:: failedAssertion(int number)

   This will be true if the body of the assertion is false. SAM gives each assertion a
   unique number.

.. function:: assertionMessage(int number, String msg)

   The message to display if an assertion fails. This gives the location and
   contents of the assertion.

.. function:: assertionArrow(int number, Ref source, Ref target, boolean positive)

   If assertion ?Number fails and it relates two objects, an assertionArrow fact will be
   recorded. This is used to add red arrows to the diagram.

.. function:: mustCall(Ref caller, String callerInvocation, String callSite, Ref target, String method)

   The :func:`didCall` relation must contain this call. Otherwise, fail the model.
   For `Unknown` callers, the call-site does not need to match.

.. function:: checkCalls(Ref object)

   Ensure that every call on `Object` is in `mayCall`.

.. function:: mayCall(Ref caller, String callerInvocation, String callSite, Ref target, String method)

   Calls that can be made on objects marked with :func:`checkCalls` without generating an error.
   Everything in :func:`mustCall` is automatically added to `mayCall` too.

.. function:: mayCall(Ref caller, String callerInvocation, Ref target, String method)

   Like :func:`mayCall`/5, but allow calls from any call-site.

.. function:: mayCall(Ref caller, Ref target, String method)

   Like :func:`mayCall`/4, but allow calls in any context.

.. function:: mayCall(Ref target)

   Like :func:`mayCall`/3, but allow calls of any method and by any caller.

.. function:: AnyoneMayCall(String method)

   This annotation indicates that all calls to the given method are OK. This is useful to annotate harmless methods.


Debugging
---------

.. function:: debug()

    If true, SAM will find a small proof explaining why and display it. It will
    also add :func:`debugEdge` facts for calls involved in this proof.

.. function:: debugEdge(Ref source, String sourceInvocation, String callSite, Ref target, String targetInvocation)

    This call from `Source` to `Target` was involved in the proof produced by :func:`debug`.
