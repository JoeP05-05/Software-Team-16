#!/bin/bash

if ! command -v javac &> /dev/null
then
  echo "Java compiler not installed."
  echo "Do: sudo apt install default-jdk"
exit 1
fi

echo "Compiling Java files: "

#Compiles all java files
javac -cp .:postgresql-42.7.4.jar *.java

#Checks if possible to compile java files
if [ $? -ne 0 ]; then
    echo "Compilation failed."
    exit 1
fi
