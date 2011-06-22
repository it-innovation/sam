.. _Configuration:

Configuration
=============

These predicates are used to describe the initial configuration of the system being modelled:

.. function:: initialObject(?Object, ?Type)

   There is an object named `Object` which :func:`isA` `Type`.

.. function:: initialInvocation(?Object, ?Invocation, ?MethodName)

   The method named `MethodName` on `Object` is initially active (does not need
   to be invoked by something else before it can take actions). The initial
   actions should be grouped under the named invocation context. By default,
   any invocations of other objects called from this one will be grouped using
   the same context name.

.. function:: initialInvocation(?Object, ?Invocation)

   Similar to the three argument form, but marks all methods as active.

You can also use :func:`field` to define the initial references held by an object.
