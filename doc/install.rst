.. _install:

Installation
============

Linux
-----
To install, you will need `0install <http://0install.net>`_. For example, on Ubuntu::

  $ sudo apt-get install zeroinstall-injector

To get SAM (creating a command called "sam")::

  $ 0alias sam http://www.serscis.eu/0install/serscis-access-modeller 

Windows
-------
Get 0install from `0install.de <http://0install.de/downloads/?lang=en>`_.

Then create a file called `sam.bat` containing this one line::

  @0launch http://www.serscis.eu/0install/serscis-access-modeller %*

Running
-------

To run SAM::

  $ sam scenario.sam

See the :ref:`tutorial` for instructions on writing the scenario.sam file.
