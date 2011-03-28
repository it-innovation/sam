Introduction
============

The SERSCIS Access Modeller (SAM) takes a model of a system (e.g. a set of objects
within a computer program or a set of machines on a network) and attempts to verify
certain security properties about the system, by exploring all the ways access can
propagate through the system.

It is designed to handle dynamic systems (e.g. systems containing factories
which may create new objects at runtime) and systems where behaviour of some
of the objects is unknown or not trusted.

A typical approach is to model a system initially with most objects having
unknown behaviour, and then to refine the model until the security goals are met.

Once you have a model that meets the goals, it should tell you:

* what behaviours must be ensured for components you own
* what behaviours you require of other parties you rely on

Without a model (formal or not), we would never know whether it was safe to
grant access to anything to anyone. Having a formal model (rather than simply
relying on the programmers’ and administrators’ intuitions) is useful because:

* It reduces the chance of mistakes.

* It makes assumptions explicit. For example, if a security property
  could be enforced by adding a restriction in either one of two
  components being developed, each component developer might
  assume it would be added at the other point. Modelling the
  whole system forces us to make that choice and document it.

* All the safety properties that are checked when building the initial
  system can be automatically rechecked when the system changes.
  When safety properties are checked manually when writing code
  (or deploying systems), changes to the system later can make
  the assumptions behind those checks invalid.
