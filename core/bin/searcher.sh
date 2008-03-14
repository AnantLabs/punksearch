#!/bin/sh
java -cp ../punksearch-core.jar:../lib/lucene-core-2.1.0.jar:../lib/commons-io-1.3.1.jar org.punksearch.cli.SearcherMain $1
