.. _GUI:

GUI
========

.. function:: guiObjectTab(?Position, ?Label, ?Predicate, ?ObjectVar)

    Add a tab to the object viewer window.

    * `Position` : used to order the tabs. The defaults have positions 10, 20, 30 and 40.
    * `Label` : the label to display for the tab
    * `Predicate` : the relation to query
    * `ObjectVar` : the variable in the predicate which should be replaced by the select object

    For example, to display all the roles that an object grants::

      guiObjectTab(60, "Grants roles", "grantsRole/3", "Target").
