#!/bin/sh

VERSION=0.9.2
TARGET_JVM=1.5
DEBUG=on

ant -Dversion=${VERSION} -Ddebug=${DEBUG} -Dant.build.javac.target=${TARGET_JVM}

