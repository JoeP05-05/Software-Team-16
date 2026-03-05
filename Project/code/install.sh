#!/bin/bash

echo "Updating package lists..."
sudo apt update
echo
echo

echo "Installing Java JDK..."
sudo apt install default-jdk -y
echo
echo

echo "Checking Java installation..."
java -version
echo
echo

echo "Installing Git..."
sudo apt install git -y
echo
echo

# Check if javac exists
if ! command -v javac &> /dev/null
then
  echo "Java compiler not installed."
  echo "Try: sudo apt install default-jdk"
  exit 1
fi
echo
echo

echo "Compilation done."
