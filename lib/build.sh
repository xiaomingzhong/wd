#!/bin/sh
mkdir classes
javac -encoding gbk -d classes -classpath tdbapi.jar ../Demo/tdbapi/Wddl.java
cd classes
jar -cvf ../wddl.jar ./ 

