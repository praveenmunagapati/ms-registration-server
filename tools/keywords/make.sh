#!/bin/sh
find src -name '*.java' > sourcefiles
mkdir classes
if ! javac -d classes @sourcefiles; then
    echo "build failed"
    exit 1
fi
cp -Rf META-INF classes
pushd classes
jar -cMf ../keywords.jar *
popd
rm sourcefiles
