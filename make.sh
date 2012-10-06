#!/bin/bash

mkdir -p bin
rm -rf bin/*

mvn clean
mvn assembly:assembly
cp target/blogix-jar-with-dependencies.jar bin/blogix.jar
cp scripts/* bin/.

