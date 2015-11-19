#!/bin/sh
mkdir classes
javac -encoding gbk -d classes -classpath tdbapi.jar ../Demo/tdbapi/Demo.java
cd classes
jar -cvf ../demo.jar ./ 

