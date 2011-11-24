VPATH = ${SRCDIR}

SAM = 0launch ${BINARYFEED}

SBUILDDIR      = ${DISTDIR}/doc
ALLSPHINXOPTS   = -W -d $(SBUILDDIR)/doctrees
all: htmldoc tests

JAVA_SOURCES = $(wildcard ${SRCDIR}/src/main/java/eu/serscis/sam/*.java) \
	       $(wildcard ${SRCDIR}/src/main/java/eu/serscis/sam/*/*.java)

SAM_EXAMPLES = $(wildcard ${SRCDIR}/doc/examples/*.sam)

code: parser
	cp ${SRCDIR}/src/main/sam/*.sam ${DISTDIR}/
	cp ${SRCDIR}/src/main/bin/sam ${DISTDIR}/
	mkdir -p ${DISTDIR}/eu/serscis/sam/lexer/
	mkdir -p ${DISTDIR}/eu/serscis/sam/parser/
	cp ${BUILDDIR}/eu/serscis/sam/lexer/lexer.dat ${DISTDIR}/eu/serscis/sam/lexer/
	cp ${BUILDDIR}/eu/serscis/sam/parser/parser.dat ${DISTDIR}/eu/serscis/sam/parser/
	javac -d ${DISTDIR} -cp .:${CLASSPATH} ${JAVA_SOURCES}

htmldoc: code images examples
	make -f ${SRCDIR}/src/Makefile htmlonly

htmlonly:
	# sphinx insists on copying the generated images itself, so we put them
	# in $BUILDDIR and let it copy them from there.
	rm -rf ${BUILDDIR}/doc-tmp
	mkdir ${BUILDDIR}/doc-tmp
	ln -s ${SRCDIR}/doc/*.rst ${BUILDDIR}/doc-tmp/
	ln -s ${SRCDIR}/doc/conf.py ${BUILDDIR}/doc-tmp/
	ln -s ${SRCDIR}/doc/_static ${BUILDDIR}/doc-tmp/
	ln -s ${SRCDIR}/doc/_templates ${BUILDDIR}/doc-tmp/
	ln -s ${BUILDDIR}/sam-output ${BUILDDIR}/doc-tmp/_images
	sphinx-build -b html ${ALLSPHINXOPTS} ${BUILDDIR}/doc-tmp ${SBUILDDIR}

examples:
	mkdir -p ${DISTDIR}/doc/examples/includes
	cp ${SRCDIR}/doc/examples/*.sam ${DISTDIR}/doc/examples/
	cp ${SRCDIR}/doc/examples/includes/*.sam ${DISTDIR}/doc/examples/includes/

images: ${SAM_EXAMPLES}
	mkdir -p ${BUILDDIR}/sam-output
	(cd ${BUILDDIR}/sam-output && ${SAM} --batch --results-dir ${BUILDDIR}/results ${SAM_EXAMPLES})

parser:
	java -jar ${SABLECC}/lib/sablecc.jar -d ${BUILDDIR} ${SRCDIR}/src/main/sablecc/sam.grammar

tests:
	${SAM} --batch --results-dir ${BUILDDIR}/results ${SRCDIR}/src/test/*.sam
