VPATH = ${SRCDIR}

SAM = 0launch ${DISTDIR}/0install/serscis-access-modeller.xml

SBUILDDIR      = ${DISTDIR}/doc
ALLSPHINXOPTS   = -d $(SBUILDDIR)/doctrees
all: htmldoc

code: src/main/java/eu/serscis/Eval.class
	cp ${SRCDIR}/src/main/datalog/*.dl ${DISTDIR}/

%.class: %.java
	javac -d ${DISTDIR} -cp ${CLASSPATH} $<

htmldoc: code examples
	sphinx-build -b html ${ALLSPHINXOPTS} ${SRCDIR}/doc ${SBUILDDIR}

examples: factory.png factory-showing-invocations.png factory1.png factory2.png factory3.png factory4.png factory5.png

%.png: doc/examples/%.dl
	mkdir -p ${DISTDIR}/doc/_images
	${SAM} $< || echo Fail
	mv access.dot.png ${DISTDIR}/doc/_images/$@
