.. _access:

Access Control
==============

SAM normally models pure object-capability systems, but it can also be used to
model identity-based access control systems.

.. function:: accessControlOn

   This must be asserted to say that you are using the access control features.
   Otherwise, the default is to assume that all access is allowed.

.. function:: accessAllowed(?Caller, ?Target, ?Method)

   Indicates that the access control system should allow `Caller` to invoke `Target.Method`.

.. function:: accessAllowed(?Caller, ?Target)

   Indicates that the access control system should allow `Caller` to invoke any method on `Target`.

Objects are always permitted to call themselves.

Identity-Based Access Control
-----------------------------

Objects may be given an identity using :func:`hasIdentity`. Child objects inherit the identity of their parents. Objects can always call other objects with the same identity.

.. function:: hasIdentity(?Object, ?Identity)

   The given object may have this identity.

These identities are often used with role-based access control rules (see below).

To check that all objects have an identity, use::

    error("No identity", ?X) :-
    	isA(?X, ?Type),
    	?Type != "Value",
    	?X != "_testDriver",
    	!hasIdentity(?X, ?Identity).


Role-Based Access Control
-------------------------

To model an RBAC system:

- Annotate some methods with :func:`PermittedRole`
- Grant roles to callers based on their identities

For example::

    class File {
        @PermittedRole("reader")
        @PermittedRole("writer")
        public void read() {}

        @PermittedRole("writer")
        public void write() {}
    }

    grantsRole("log.txt", "reader", "alice").
    grantsRole("log.txt", "writer", "bob").


.. function:: grantsRole(?Target, ?Role, ?CallerIdentity)

   The object `Target` grants `Role` to any caller which :func:`hasIdentity` `CallerIdentity`.

.. function:: PermittedRole(?Method, ?Role)

   This method will allow access (:func:`accessAllowed`) to callers with the given role.
