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

Predicates
----------

.. function:: denyAccess(?Object, ?Target)

   Verify that it is impossible for `Object` to ever get access to
   `Target`. i.e. `Object` must never be able to get a local variable or
   field with a reference to `Target`.

.. function:: requireAccess(?Object, ?Target)

   Check that the model does not exclude the possiblilty of `Object`
   getting access to `Target`. Note that (unlike `denyAccess` above), this
   check cannot be exact: a real system could conform to the model but
   still not allow this. However, it is a useful sanity check that your
   model is not over-constrained.

.. function:: error(?Message, ?Args...)

   Fail the model checking and print the message and arguments as the
   error (up to four extra arguments are allowed). The base library
   creates errors automatically in many cases (e.g. if `denyAccess`
   fails), but you can also specify them manually, e.g.::

     error("Store contains non-data item", ?Store, ?Item) :-
       isA(?Store, "Store"),
       field(?Store, "data", ?Item),
       !isA(?Item, "Data").

.. function:: haveBadAccess(?SourceObject, ?TargetObject)

   Fail the model and indicate the problem with a red arrow on the graph.

.. function:: missingGoodAccess(?SourceObject, ?TargetObject)

   Fail the model and indicate the problem with a dotted red arrow on the graph.

.. function:: expectFailure

   Indicates that this scenario is expected to fail. Normally, SAM exits with a status
   code of 0 if the model passes, or 1 on failure. This reverses the test.

Debugging
---------

.. function:: debug

    If true, SAM will find a small proof explaining why and display it. It will
    also add :func:`debugEdge` facts for calls involved in this proof.

.. function:: debugEdge(?Source, ?SourceInvocation, ?CallSite, ?Target, ?TargetInvocation)

    This call from `Source` to `Target` was involved in the proof produced by :func:`debug`.
