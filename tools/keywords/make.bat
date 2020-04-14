setlocal
dir /s /b *.java > sourcefiles
javac -target 1.3 -source 1.3 -d classes @sourcefiles
xcopy /Y /S META-INF classes\META-INF\
pushd classes
jar -cMf ..\keywords.jar *
popd
del sourcefiles
