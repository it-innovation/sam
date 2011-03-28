Goals
=====

.. function:: denyAccess(?Holder, ?Target)

   Verify that it is impossible for `Holder` to ever get access to
   `Target`. i.e. `Holder` must never be able to get a local variable or
   field with a reference to `Target`.

.. function:: requireAccess(?Holder, ?Target)

   Check that the model does not exclude the possiblilty of `Holder`
   getting access to `Target`. Note that (unlike `denyAccess` above), this
   check cannot be exact: a real system could conform to the model but
   still not allow this. However, it is a useful sanity check that your
   model is not over-constrained.

.. function:: error(?Message, ?Args...)

   Fail the model checking and print the message and arguments as the
   error (up to four extra arguments are allowed). The base library
   creates errors automatically in many cases (e.g. if `denyAccess`
   fails), but you can also specify them manually, e.g.::

     error('Store contains non-data item', ?Store, ?Item) :-
       isA(?Store, 'Store'),
       field(?Store, 'data', ?Item),
       !isA(?Item, 'Data').
