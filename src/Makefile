VPATH = ${SRCDIR}

SAM = 0launch ${DISTDIR}/0install/serscis-access-modeller.xml

SBUILDDIR      = ${DISTDIR}/doc
ALLSPHINXOPTS   = -d $(SBUILDDIR)/doctrees
all: htmldoc

JAVA_SOURCES = ${SRCDIR}/src/main/java/eu/serscis/Eval.java \
	       ${SRCDIR}/src/main/java/eu/serscis/Debugger.java

code:
	cp ${SRCDIR}/src/main/datalog/*.dl ${DISTDIR}/
	javac -d ${DISTDIR} -cp ${CLASSPATH} ${JAVA_SOURCES}

htmldoc: code examples
	sphinx-build -b html ${ALLSPHINXOPTS} ${SRCDIR}/doc ${SBUILDDIR}

examples: factory.png factory-showing-invocations.png factory1.png factory2.png factory3.png factory4.png factory5.png factory6.png

%.png: doc/examples/%.dl
	mkdir -p ${DISTDIR}/doc/_images
	${SAM} $< || echo Fail
	mv access.dot.png ${DISTDIR}/doc/_images/$@
