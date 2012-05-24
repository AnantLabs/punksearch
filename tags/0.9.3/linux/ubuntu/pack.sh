#!/bin/sh

VERSION=0.9.3

SRC=punksearch-$VERSION-quickstart
DST=punksearch_$VERSION
DIR=punksearch-$VERSION

rm *.zip
rm -rf $DIR

mkdir $DIR

cp ../../builder/$SRC.zip .
./zip2tgz.sh $SRC $DST

cp $DST.tar.gz $DIR
cd $DIR
tar -xzvf $DST.tar.gz
rm $DST.tar.gz

cp -r ../debian debian

debuild

