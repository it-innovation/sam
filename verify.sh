#!/bin/sh
# (just using git-diff here because it colours the results nicely)
export GIT_DIR=/
exec git diff -- src/results.gold build/results
