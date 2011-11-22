#!/usr/bin/env python
import os, sys

documented = set()
declared = set()

mydir = os.path.dirname(sys.argv[0])
docdir = os.path.join(mydir, "doc")
dldir = os.path.join(mydir, "src", "main", "datalog")

for f in os.listdir(docdir):
	if not f.endswith('.rst'): continue
	for line in open(os.path.join(docdir, f)):
		if line.startswith('.. function:: '):
			documented.add(line.split(' ', 2)[2].strip())

def parse_dl(path):
	for line in open(path):
		if line.startswith('declare '):
			declared.add(line.split(' ', 1)[1].strip()[:-1])
parse_dl(os.path.join(dldir, 'base.sam'))

undocumented = declared - documented
if undocumented:
	print "Undocumented:"
	for sig in sorted(undocumented):
		print "-", sig

obsolete = documented - declared
obsolete.remove("error(?Message, ?Args...)")
if obsolete:
	print "\nObsolete:"
	for sig in sorted(obsolete):
		print "-", sig
