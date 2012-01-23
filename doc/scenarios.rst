Scenarios
=========

When looking for possible unexpected access, it is particularly useful to compare two scenarios: a baseline scenario in which all actors have the expected behaviour, and a test scenario in which some actors have `Unknown` behaviour.

SAM allows you to define two (or more) scenarios in a single file using a set of preprocessor directives to indicate which lines in the file should be used in which scenarios. For example::

  #declare scenario attacker
  ...
  config {
      test "Other" {
  #if attacker
    	Ref other = new Unknown();
  #elif baseline
    	Ref other = new TrustedUser();
  #endif
    }
  }

The SAM GUI will display one tab for each scenario ("baseline" and "attacker"), allowing you to see the differences between them easily.
