#!/bin/sh

cd hooks

HOOK_DIR=../../.git/hooks
mkdir -p $HOOK_DIR

for i in * ; do 
  cp ./$i $HOOK_DIR/$i
  chmod a+x $i $HOOK_DIR/$i
done
