all: src/main/java/eu/serscis/Eval.class
	java -cp src/main/java:${CLASSPATH} eu.serscis.Eval scenario.dl
	dot -Tpng access.dot > access.png
	dot -Tpng initial.dot > initial.png

IRIS=/home/tal/langs/iris-0.60/
CLASSPATH=${IRIS}/iris-app-0.60.jar:${IRIS}/iris-0.60.jar:${IRIS}/iris-parser-0.60.jar

%.class: %.java
	javac -cp ${CLASSPATH} $<
