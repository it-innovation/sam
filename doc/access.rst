Access Control
==============

SAM normally modells pure object-capability systems, but it can also be used to
model identity-based access control systems.

.. function:: accessControlOn

   This must be asserted to say that you are using the access control features.
   Otherwise, the default is to assume that all access is allowed.

.. function:: accessAllowed(?Caller, ?Target, ?Method)

   Indicates that the access control system should allow `Caller` to invoke `Target.Method`.

