VPATH = ${SRCDIR}

SAM = 0launch ${BINARYFEED}

SBUILDDIR      = ${DISTDIR}/doc
ALLSPHINXOPTS   = -d $(SBUILDDIR)/doctrees
all: htmldoc tests

JAVA_SOURCES = $(wildcard ${SRCDIR}/src/main/java/eu/serscis/*.java) \
	       $(wildcard ${SRCDIR}/src/main/java/eu/serscis/sam/*/*.java)

SAM_EXAMPLES = $(wildcard ${SRCDIR}/doc/examples/*.sam)

code:
	cp ${SRCDIR}/src/main/datalog/*.dl ${DISTDIR}/
	cp ${SRCDIR}/src/main/bin/sam ${DISTDIR}/
	mkdir -p ${DISTDIR}/eu/serscis/sam/lexer/
	mkdir -p ${DISTDIR}/eu/serscis/sam/parser/
	cp ${SRCDIR}/src/main/java/eu/serscis/sam/lexer/lexer.dat ${DISTDIR}/eu/serscis/sam/lexer/
	cp ${SRCDIR}/src/main/java/eu/serscis/sam/parser/parser.dat ${DISTDIR}/eu/serscis/sam/parser/
	javac -d ${DISTDIR} -cp ${CLASSPATH} ${JAVA_SOURCES}

htmldoc: code images examples
	sphinx-build -b html ${ALLSPHINXOPTS} ${SRCDIR}/doc ${SBUILDDIR}

htmlonly:
	sphinx-build -b html ${ALLSPHINXOPTS} ${SRCDIR}/doc ${SBUILDDIR}

examples:
	mkdir -p ${DISTDIR}/doc/examples
	cp ${SRCDIR}/doc/examples/*.sam ${DISTDIR}/doc/examples/

images: ${SAM_EXAMPLES}
	mkdir -p ${DISTDIR}/doc/_images
	(cd ${DISTDIR}/doc/_images && ${SAM} ${SAM_EXAMPLES})

parser:
	rm -rf ${SRCDIR}/src/main/java/eu/serscis/sam/parser/*
	rm -rf ${SRCDIR}/src/main/java/eu/serscis/sam/lexer/*
	rm -rf ${SRCDIR}/src/main/java/eu/serscis/sam/analysis/*
	rm -rf ${SRCDIR}/src/main/java/eu/serscis/sam/node/*
	java -jar ${SRCDIR}/sablecc.jar -d ${SRCDIR}/src/main/java ${SRCDIR}/src/main/sablecc/sam.grammar

tests:
	${SAM} ${SRCDIR}/src/test/*.sam
