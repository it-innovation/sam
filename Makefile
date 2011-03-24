all: eu/serscis/Eval.class
	java -cp .:${CLASSPATH} eu.serscis.Eval factory.dl

IRIS=/home/tal/langs/iris-0.60/
CLASSPATH=${IRIS}/iris-app-0.60.jar:${IRIS}/iris-0.60.jar:${IRIS}/iris-parser-0.60.jar

%.class: %.java
	javac -cp ${CLASSPATH} $<
