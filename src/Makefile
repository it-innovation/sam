VPATH = ${SRCDIR}

SAM = 0launch ${DISTDIR}/0install/serscis-access-modeller.xml

SBUILDDIR      = ${DISTDIR}/doc
ALLSPHINXOPTS   = -d $(SBUILDDIR)/doctrees
all: htmldoc

JAVA_SOURCES = $(wildcard ${SRCDIR}/src/main/java/eu/serscis/*.java) \
	       $(wildcard ${SRCDIR}/src/main/java/eu/serscis/sam/*/*.java)

code:
	cp ${SRCDIR}/src/main/datalog/*.dl ${DISTDIR}/
	mkdir -p ${DISTDIR}/eu/serscis/sam/lexer/
	mkdir -p ${DISTDIR}/eu/serscis/sam/parser/
	cp ${SRCDIR}/src/main/java/eu/serscis/sam/lexer/lexer.dat ${DISTDIR}/eu/serscis/sam/lexer/
	cp ${SRCDIR}/src/main/java/eu/serscis/sam/parser/parser.dat ${DISTDIR}/eu/serscis/sam/parser/
	javac -d ${DISTDIR} -cp ${CLASSPATH} ${JAVA_SOURCES}

htmldoc: code examples
	sphinx-build -b html ${ALLSPHINXOPTS} ${SRCDIR}/doc ${SBUILDDIR}

examples: factory.png factory-showing-invocations.png factory1.png factory2.png factory3.png factory4.png factory5.png sealers.png

%.png: doc/examples/%.dl
	mkdir -p ${DISTDIR}/doc/_images
	${SAM} $< || echo Fail
	mv access.dot.png ${DISTDIR}/doc/_images/$@

parser:
	rm -rf ${SRCDIR}/src/main/java/eu/serscis/sam/parser/
	rm -rf ${SRCDIR}/src/main/java/eu/serscis/sam/lexer/
	rm -rf ${SRCDIR}/src/main/java/eu/serscis/sam/analysis/
	rm -rf ${SRCDIR}/src/main/java/eu/serscis/sam/node/
	java -jar ${SRCDIR}/sablecc.jar -d ${SRCDIR}/src/main/java ${SRCDIR}/src/main/sablecc/sam.grammar
