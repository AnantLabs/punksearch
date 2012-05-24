#!/bin/sh

rm -rf tmp
rm $2.tar.gz
rm $2.orig.tar.gz

mkdir tmp

unzip $1.zip -d tmp/

tar -czvf $2.tar.gz -C tmp . 

cp $2.tar.gz $2.orig.tar.gz

rm -rf tmp
