#!/bin/bash

mkdir -p bin
rm -rf bin/*
mkdir -p target
rm -rf target/*

VERSION=$( grep '<version>' pom.xml | head -n 1 | sed 's!.*<version>\(.*\)</version>.*!\1!' )

mvn clean assembly:assembly
cp target/blogix-jar-with-dependencies.jar bin/blogix.jar
cp scripts/* bin/.
cp -r templates bin/templates

mkdir -p download
rm -rf download/*
mkdir download/bin

cp -r bin/* download/bin/.
cp LICENSE-2.0.txt download/bin/.
cp README download/bin/.
cd download/bin


ZIP=blogix-bin-${VERSION}.zip 
zip -9 -r $ZIP *
find . | grep -v "$ZIP" | xargs rm -rf

cd ../..

mkdir download/src
cp -r src/main/* download/src/.
cp LICENSE-2.0.txt download/src/.
cp README download/src/.

ZIP_SRC=blogix-sources-${VERSION}.zip
cd download/src
zip -r -9 $ZIP_SRC *
find . | grep -v "$ZIP_SRC" | xargs rm -rf

cd ../
cp src/* .
cp bin/* .
rm -rf src/ bin/

