.. highlight:: java

Datalog
=======

SAM uses a `Datalog <http://en.wikipedia.org/wiki/Datalog>`_ engine to evaluate the models. We will give a brief
introduction to Datalog here, along with some extensions added by SAM.

A Datalog program consists of a set of `facts` and a set of `rules`. The rules are applied to the facts to generate new
facts. This process continues until no new facts can be produced.

For example::

  // a rule: ?Y is an admin if ?X is an admin and ?X says ?Y is an admin
  isAdmin(?Y) :- isAdmin(?X), saysIsAdmin(?X, ?Y).

  // some facts...
  
  // Alice is an admin
  isAdmin("Alice").

  // Alice says Bob is an admin
  saysIsAdmin("Alice", "Bob").

Running this program, SAM would use the rule to deduce the new fact::

  isAdmin("Bob").

Queries
=======
A like starting with `?-` is a query. When the model has been evaluated, SAM will write all matching values
to the console. For example, to list all the admins::

  ?- isAdmin(?X).

.. _Types:

Types
=====

Predicates (such as `isAdmin`) must be declared in SAM before they can be used. Each term is given a
type and a name (the name is used in the GUI)::

  declare isAdmin(String name).
  declare saysIsAdmin(String delegator, String delegate).

The SAM type-hierarchy looks like this:

 * Object (abstract)
     * Ref (e.g. `<Bob>`)
     * Value (abstract)
	 * String (e.g. `"Bob"`)
	 * int (e.g. `3`)
	 * boolean (`true`/`false`)

References behave much like strings, but have their own namespace (so `"Bob" != <Bob>`). In SAM, references are used
to identify objects in the model (representing instances of Java classes), while strings are used to represent
literal strings.

The `any` values
================

There is a special `any` value for each type (`any(String)`, `any(Value)`, etc). The idea is that `Unknown` objects
may pass `any(Value)` as an argument, rather than having to enumerate every possible constant value in the system::

  // Bob decides to make everyone an admin
  saysIsAdmin("Bob", any(String)).

The `any` values can be narrowed by various type constraints. For example, if a sender may send `any(Value)` and a
receiver declares its parameter type as `String`, then it receives `any(String)`. The `any` mechanism is not built in
to the Datalog engine (which sees these as distinct values), so rules that handle these values need to use
:func:`ASSIGN` or :func:`MATCH` to apply the appropriate matching rules.
