#!/bin/sh

VERSION=$1
TARGET_JVM=1.5
DEBUG=on

ant -Dversion=${VERSION} -Ddebug=${DEBUG} -Dant.build.javac.target=${TARGET_JVM}

