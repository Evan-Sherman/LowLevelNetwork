# To run project

1. Compile Router.Java, Bridge.java, and Host.java
2. Create a batch file with commands to run the network by running processes asynchonously. This is done with & in UNIX, or start /B in Windows.
3. Watch the magic happen

NOTE: SINCE THIS IS IN JAVA, IT IS RECOMMENDED TO RUN ON THE UTD NET SERVERS RATHER THAN THE USUAL CS ONES


EXAMPLE OF A BATCH FILE IN WINDOWS FOR SCENARIO 1:

javac Host.java
javac Router.java
javac Bridge.java

start /B java Host 1 1 23 1 9 1 1 1 3 "Hi, how are you"
start /B java Host 1 3 54 1 9 1 2 1 1
start /B java Bridge 1 2
