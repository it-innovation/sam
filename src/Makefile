VPATH = ${SRCDIR}

all: src/main/java/eu/serscis/Eval.class
	cp ${SRCDIR}/src/main/datalog/*.dl ${DISTDIR}/

%.class: %.java
	javac -d ${DISTDIR} -cp ${CLASSPATH} $<
