Sealers
=======

Sealers and unsealers are a *rights-amplification* pattern. The idea is that a
value is "sealed" in a sealed box using a "sealer", and can only be recovered
by using the corresponding unsealer. Neither the sealed box nor the unsealer
alone is sufficient to recover the value. An analogy would be that the sealed
box is a tin of food, and the unsealer is the tin-opener.

There are various ways to implement sealers in a real system. One way is to have
the unsealer know which sealed box maps to which value and return the correct
value for the box it is given. This cannot be modelled directly in the Java-like
syntax because SAM does not support conditionals.

Another way is to have a method on the sealed box that causes it to deposit the
value somewhere from where the unsealer can retrieve it.

For modelling purposes, however, we can simplify the system by using global knowledge.
We can augment the Unsealer's code with some Datalog::

  class Unsealer {
    public Object unseal(Box box) {
      Object value;
      return value;
    }
  }
  local(?Unsealer, ?Invocation, "Unsealer.unseal.value", ?Value) :-
	  isA(?Unsealer, "Unsealer"),
	  local(?Unsealer, ?Invocation, "Unsealer.unseal.box", ?Box),
	  field(?Box, "precious", ?Value).

This says that `Unsealer.unseal`'s local variable `value` may have a particular
value if the `box` passed as an input parameter has a field called `precious`
with that value.

Notice that we could not implement a real unsealer this way, because it wouldn't
have access to the box's private field. However, having created this unsealer, we
can now use it in larger patterns.

For example, in the `sealers.dl` example, `sender` seals a value (`precious`)
in a box and passes the box to various other objects. Those with access to the
unsealer (aggregated as `withUnsealer`) are able to get access to the value,
while those without it can't:

.. image:: _images/sealers.png

The `sealers2.dl` example has `sender` seal two different values and give them to
different objects, which all have access to the unsealer. Each object can only
unseal the correct value:

.. image:: _images/sealers2.png

To prove this, we needed to aggregate calls to the unsealer separately for the two groups
of clients, and to the sealer separately for the two values being sealed::

  invocationObject("sender", "A", "Sender.test-1", "1").
  invocationObject("sender", "A", "Sender.test-2", "2").
  invocationObject("sender", "A", "Sender.test-3", "1").
  invocationObject("sender", "A", "Sender.test-4", "2").
