#!/bin/bash

mkdir -p bin
rm -rf bin/*
mkdir -p target
rm -rf target/*


mvn clean assembly:assembly
cp target/blogix-jar-with-dependencies.jar bin/blogix.jar
cp scripts/* bin/.
cp -r templates bin/templates

