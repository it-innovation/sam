VPATH = ${SRCDIR}

SAM = 0launch ${BINARYFEED}

SBUILDDIR      = ${DISTDIR}/doc
ALLSPHINXOPTS   = -d $(SBUILDDIR)/doctrees
all: htmldoc tests

JAVA_SOURCES = $(wildcard ${SRCDIR}/src/main/java/eu/serscis/sam/*.java) \
	       $(wildcard ${SRCDIR}/src/main/java/eu/serscis/sam/*/*.java)

SAM_EXAMPLES = $(wildcard ${SRCDIR}/doc/examples/*.sam)

code: parser
	cp ${SRCDIR}/src/main/datalog/*.dl ${DISTDIR}/
	cp ${SRCDIR}/src/main/bin/sam ${DISTDIR}/
	mkdir -p ${DISTDIR}/eu/serscis/sam/lexer/
	mkdir -p ${DISTDIR}/eu/serscis/sam/parser/
	cp ${BUILDDIR}/eu/serscis/sam/lexer/lexer.dat ${DISTDIR}/eu/serscis/sam/lexer/
	cp ${BUILDDIR}/eu/serscis/sam/parser/parser.dat ${DISTDIR}/eu/serscis/sam/parser/
	javac -d ${DISTDIR} -cp .:${CLASSPATH} ${JAVA_SOURCES}

htmldoc: code images examples
	sphinx-build -b html ${ALLSPHINXOPTS} ${SRCDIR}/doc ${SBUILDDIR}

htmlonly:
	sphinx-build -b html ${ALLSPHINXOPTS} ${SRCDIR}/doc ${SBUILDDIR}

examples:
	mkdir -p ${DISTDIR}/doc/examples/includes
	cp ${SRCDIR}/doc/examples/*.sam ${DISTDIR}/doc/examples/
	cp ${SRCDIR}/doc/examples/includes/*.sam ${DISTDIR}/doc/examples/includes/

images: ${SAM_EXAMPLES}
	mkdir -p ${DISTDIR}/doc/_images
	(cd ${DISTDIR}/doc/_images && ${SAM} --batch ${SAM_EXAMPLES})

parser:
	java -jar ${SABLECC}/lib/sablecc.jar -d ${BUILDDIR} ${SRCDIR}/src/main/sablecc/sam.grammar

tests:
	${SAM} --batch ${SRCDIR}/src/test/*.sam
