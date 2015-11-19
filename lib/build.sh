#!/bin/sh
mkdir classes
javac -encoding utf-8 -d classes -classpath tdbapi.jar ../Demo/tdbapi/Wddl.java
cd classes
jar -cvf ../wddl.jar ./ 

