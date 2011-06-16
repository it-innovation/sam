Goals
=====

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

Assertions
----------

.. function:: error(?Message, ?Args...)

   Fail the model checking and print the message and arguments as the
   error (up to four extra arguments are allowed). The base library
   creates errors automatically in many cases (e.g. if `denyAccess`
   fails), but you can also specify them manually, e.g.::

     error('Store contains non-data item', ?Store, ?Item) :-
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
