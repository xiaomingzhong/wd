rem ÉèÖÃJNI¼ÓÔØÂ·¾¶

PATH=%PATH%;win32

rem java -classpath demo.jar;tdbapi.jar;bin tdbapi.Demo ip port user password

"%JDK_HOME%\bin\java" -classpath demo.jar;tdbapi.jar;bin tdbapi.Demo 10.100.7.18 10012 dev_test dev_test

pause