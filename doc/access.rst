.. _access:

Access Control
==============

SAM normally models pure object-capability systems, but it can also be used to
model identity-based access control systems.

.. function:: accessControlOn()

   This must be asserted to say that you are using the access control features.
   Otherwise, the default is to assume that all access is allowed.

.. function:: accessAllowed(Ref caller, Ref target, String method)

   Indicates that the access control system should allow `Caller` to invoke `Target.Method`.

.. function:: accessAllowed(Ref caller, Ref target)

   Indicates that the access control system should allow `Caller` to invoke any method on `Target`.

Objects are always permitted to call themselves.

Identity-Based Access Control
-----------------------------

Objects may be given an identity using :func:`hasIdentity`. Child objects inherit the identity of their parents. Objects can always call other objects with the same identity.

.. function:: hasIdentity(Ref object, String identity)

   The given object may have this identity.

.. function:: noDefaultIdentity(String callSite)

   When a call-site creates a new object, the new object normally gets the same identity as its parent. This can be used to disable the default behaviour.
   For example, if a service creates tasks with their own identities::

     noDefaultIdentity("Service.makeTask:task=new-Task").

Identities are often used with role-based access control rules (see below).

To check that all objects have an identity, use::

    error("No identity", ?X) :-
    	isA(?X, ?Type),
    	?Type != "Value",
    	?X != <_testDriver>,
    	!hasIdentity(?X, ?Identity).


Role-Based Access Control
-------------------------

To model an RBAC system:

- Annotate some fields with :func:`FieldGrantsRole`
- Annotate some methods with :func:`PermittedRole`

For example::

    class File {
        @FieldGrantsRole("owner")
        private String myOwners;

        @FieldGrantsRole("reader")
        private String myReaders;

        @FieldGrantsRole("writer")
        private String myWriters;

        public File(String owner) {
            myOwners = owner;
        }

        @PermittedRole("owner")
        @PermittedRole("reader")
        public void read() {}

        @PermittedRole("owner")
        @PermittedRole("writer")
        public void write() {}

        @PermittedRole("owner")
        public void addReader(String reader) {
            myReaders = reader;
        }

        @PermittedRole("owner")
        public void addWriter(String writer) {
            myWriters = writer;
        }
    }

.. function:: grantsRole(Ref target, String role, String callerIdentity)

   The object `Target` grants `Role` to any caller which :func:`hasIdentity` `CallerIdentity`.

.. function:: FieldGrantsRole(String type, String fieldName, String role)

   Callers with an identity stored in the given field have the given role.

.. function:: PermittedRole(String method, String role)

   This method will allow access (:func:`accessAllowed`) to callers with the given role.
