#!/bin/bash

echo "Updating package lists..."
sudo apt update

echo "Installing Java JDK..."
sudo apt install default-jdk -y

echo "Checking Java installation..."
java -version

echo "Installing Git..."
sudo apt install git -y

# Check if javac exists
if ! command -v javac &> /dev/null
then
  echo "Java compiler not installed."
  echo "Try: sudo apt install default-jdk"
  exit 1
fi

echo "Compilation done."
