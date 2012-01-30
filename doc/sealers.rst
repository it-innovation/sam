.. highlight:: java

Sealers
=======

Sealers and unsealers are a *rights-amplification* pattern. The idea is that a
value is "sealed" in a sealed box using a "sealer", and can only be recovered
by using the corresponding unsealer. Neither the sealed box nor the unsealer
alone is sufficient to recover the value. An analogy would be that the sealed
box is a locked box, and the unsealer is the key.

A simple solution
-----------------

One simple way to model this is to store the value inside the box. To model the
fact that only the unsealer can read the value, we can use some embedded
Datalog in the `Unsealer` class to extract it (bypassing the usual capability
rules)::

  class Box {
    private Object precious;
    private Object myUnsealer;

    public Box(Unsealer unsealer, Object value) {
      precious = value;
      myUnsealer = unsealer;
    }
  }
  
  class Unsealer {
    public Object unseal(Box box) {
      Object value = ?Value :- field(box, "precious", ?Value), field(box, "myUnsealer", this);
      return value;
    }
  }

The Datalog in the `unseal` method says `value` may have a particular value if
the `box` passed as an input parameter has a field called `precious` with that
value and a field called `unsealer` with the unsealer.

Notice that we could not implement a real unsealer this way, because it wouldn't
have access to the box's private fields. However, having created this unsealer, we
can now use it in larger patterns.


A better approach
-----------------

However, the design above has a flaw. The `Unknown` type is supposed to be able to do anything
that any other object can do. But if we give `unsealer` an `Unknown` object with the same references
as a `box`, it won't extract the contents, and if we make a `Box` with an `Unknown` unsealer, the unsealer
won't be able to get the contents.

That may be acceptable, as long we are sure that all initial non-Box objects do not contain sealed boxes
that may need to be unpacked by our unsealer, etc.

The problem is that we have under-approximated the behaviour of `Box` (it never reveals its value, according
to its class definition). A clue that we did this was in the use of a Datalog variable as the value in the
expression::

  Object value = ?Value :- field(box, "precious", ?Value), field(box, "myUnsealer", this);

The fact that we had to use a Datalog variable here, rather than a Java variable, shows that the unsealer needed
to do things that a normal class couldn't, because the `Box` didn't implement all the behaviour it should have done.

To fix this, we add a method to the box that causes it to send its value to the unsealer::

  class Box {
    private Object precious;
    private Object myUnsealer;

    public Box(Unsealer unsealer, Object value) {
      precious = value;
      myUnsealer = unsealer;
    }

    public void offerContent() {
      myUnsealer.acceptContent(precious);
    }
  }

A `Box` in the model is now a safe over-approximation of a real box; if we created a `Box` with an `Unknown` unsealer then
the unsealer would be able to get its contents.

We can now update the definition of the `Unsealer`::

  class Unsealer {
    private Object myTempContents;

    public Object unseal(Box box) {
      box.offerContent();
      Object value = myTempContents :-
          didReceive(this, $Context, "Unsealer.acceptContent", 0, myTempContents);
      return value;
    }

    public void acceptContent(Object contents) {
      myTempContents = contents;
    }
  }

The real behaviour is that an `Unsealer` sets `myTempContents` to `null`, asks the box to send its contents, and then returns `myTempContents`. We can't model
setting the field to `null`, but we can add a restriction that `acceptContent` received that value in the same invocation context as the call to `unseal`.

With this change, we can pass an `Unknown` object holding a value to an `Unsealer` and it will unseal it successfully.


Examples
--------

In the :example:`sealers` example, `sender` seals a value (`precious`)
in a box and passes the box to various other objects. Those with access to the
unsealer (aggregated as `withUnsealer`) are able to get access to the value,
while those without it can't:

.. sam-output:: sealers-baseline

The :example:`sealers2` example has `sender` seal two different values and give them to
different objects, which all have access to the unsealer. Each object can only
unseal the correct value:

.. sam-output:: sealers2-baseline

To prove this, we needed to aggregate calls to the unsealer separately for the two groups
of clients, and to the sealer separately for the two values being sealed.
