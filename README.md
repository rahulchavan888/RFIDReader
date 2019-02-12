# RFIDReader


Standalone spring boot application to read arduino RFID reader scanned tag data from the serial port and send it to server.
Used Java Communications API.

#### Note:
Projet Work on 32-bit JVM Only.

To Run it follow below steps:
1.Install 32bit JDK.
2.Copy 'win32com.dll' to JDK_HOME\jre\bin.
3.Copy 'javax.comm.properties'to to JDK_HOME\jre\lib.
4.Copy 'comm.jar'to to JDK_HOME\jre\lib\ext.
Now run your program and it should work.
