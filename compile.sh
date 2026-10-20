#!/bin/bash
mkdir -p bin
find src -name "*.java" > sources.txt
javac -d bin @sources.txt
EXIT_CODE=$?
if [ $EXIT_CODE -eq 0 ]; then
    echo "Compilation successful!"
else
    echo "Compilation failed with exit code: $EXIT_CODE"
fi
rm -f sources.txt
exit $EXIT_CODE
