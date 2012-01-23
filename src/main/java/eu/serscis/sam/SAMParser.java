/////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2011
//
// Copyright in this library belongs to the University of Southampton
// University Road, Highfield, Southampton, UK, SO17 1BJ
//
// This software may not be used, sold, licensed, transferred, copied
// or reproduced in whole or in part in any manner or form or in or
// on any media by any person other than in accordance with the terms
// of the Licence Agreement supplied with the software, or otherwise
// without the prior written consent of the copyright owners.
//
// This software is distributed WITHOUT ANY WARRANTY, without even the
// implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
// PURPOSE, except where stated in the Licence Agreement supplied with
// the software.
//
//	Created By :			Thomas Leonard
//	Created Date :			2011-04-04
//	Created for Project :		SERSCIS
//
/////////////////////////////////////////////////////////////////////////
//
//  License : GNU Lesser General Public License, version 2.1
//
/////////////////////////////////////////////////////////////////////////

package eu.serscis.sam;

import java.io.InputStreamReader;
import java.io.InputStream;
import eu.serscis.sam.lexer.LexerException;
import org.deri.iris.api.terms.IConcreteTerm;
import eu.serscis.sam.parser.ParserException;
import java.io.BufferedReader;
import eu.serscis.sam.node.*;
import java.io.PushbackReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.io.FileReader;
import java.io.Reader;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.basics.ILiteral;
import org.deri.iris.api.basics.IAtom;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.storage.IRelation;
import static org.deri.iris.factory.Factory.*;
import eu.serscis.sam.lexer.Lexer;
import eu.serscis.sam.parser.Parser;
import static eu.serscis.sam.Constants.*;

public class SAMParser {
	public List<IQuery> queries = new LinkedList<IQuery>();
	private Model model;
	private File dir, myPath;

	public SAMParser(Model model, File path) throws Exception {
		this.model = model;
		dir = path.getParentFile();
		myPath = path;

		FileReader reader = new FileReader(path);
		try {
			parse(reader);
		} catch (LexerException ex) {
			reader.close();
			reader = new FileReader(path);
			reportError(ex, path.toString(), reader);
		} catch (ParserException ex) {
			reader.close();
			reader = new FileReader(path);
			reportError(ex, path.toString(), reader);
		} finally {
			reader.close();
		}
	}

	public SAMParser(Model model, String resource) throws Exception {
		this.model = model;

		InputStream is = getClass().getClassLoader().getResourceAsStream(resource);
		try {
			Reader reader = new InputStreamReader(is);
			parse(reader);
		} catch (LexerException ex) {
			is.close();
			is = getClass().getClassLoader().getResourceAsStream(resource);
			Reader reader = new InputStreamReader(is);
			reportError(ex, resource, reader);
		} catch (ParserException ex) {
			is.close();
			is = getClass().getClassLoader().getResourceAsStream(resource);
			Reader reader = new InputStreamReader(is);
			reportError(ex, resource, reader);
		} finally {
			is.close();
		}
	}

	public SAMParser(Model model, SAMInput input, String scenario) throws Exception {
		this.model = model;
		myPath = input.getSource();
		dir = myPath.getParentFile();

		Reader reader = input.getScenario(scenario);

		try {
			parse(reader);
		} catch (LexerException ex) {
			reader.reset();
			reportError(ex, myPath.toString(), reader);
		} catch (ParserException ex) {
			reader.reset();
			reportError(ex, myPath.toString(), reader);
		}
	}

	private void reportError(Exception ex, String source, Reader reader) throws Exception {
		int line = -1;
		int col = -1;

		if (ex instanceof LexerException) {
			String msg = ((LexerException) ex).getMessage();
			int lbracket = msg.indexOf('[');
			int rbracket = msg.indexOf(']', lbracket);
			String[] loc = msg.substring(lbracket + 1, rbracket).split(",");
			line = Integer.valueOf(loc[0]);
			col = Integer.valueOf(loc[1]);
		} else if (ex instanceof ParserException) {
			Token t = ((ParserException) ex).getToken();
			if (t != null) {
				line = t.getLine();
				col = t.getPos();
			}
			if (ex instanceof AddImportException) {
				ex = (InvalidModelException) ex.getCause();
			}
		} else {
			throw ex;
		}

		if (line != -1) {
			throw new InvalidModelException(ex, source, getLine(reader, line), line, col);
		} else {
			throw new InvalidModelException(ex, source, "(unknown location)", -1, -1);
		}
	}

	private void parse(Reader source) throws Exception {
		Map<String,SAMClass> classDefinitions = new HashMap<String,SAMClass>();

		PushbackReader pbr = new PushbackReader(source);
		Lexer lexer = new Lexer(pbr);
		Parser parser = new Parser(lexer);
		Start start = parser.parse();

		for (PToplevel toplevel : ((AProgram) start.getPProgram()).getToplevel()) {
			//System.out.println("Processing " + toplevel);

			if (toplevel instanceof ABehaviourToplevel) {
				SAMClass klass = new SAMDeclaredClass(model, (ABehaviour) ((ABehaviourToplevel) toplevel).getBehaviour());
				if (classDefinitions.containsKey(klass.name)) {
					throw new RuntimeException("Duplicate class definition: " + klass.name);
				}
				classDefinitions.put(klass.name, klass);
			} else if (toplevel instanceof AFactToplevel) {
				addFact((AFact) ((AFactToplevel) toplevel).getFact());
			} else if (toplevel instanceof ARuleToplevel) {
				addRule((ARule) ((ARuleToplevel) toplevel).getRule());
			} else if (toplevel instanceof AQueryToplevel) {
				addQuery((AQuery) ((AQueryToplevel) toplevel).getQuery());
			} else if (toplevel instanceof ADeclareToplevel) {
				addDeclare(((ADeclareToplevel) toplevel).getDeclare());
			} else if (toplevel instanceof AAssertToplevel) {
				addAssert((AAssert) ((AAssertToplevel) toplevel).getAssert());
			} else if (toplevel instanceof AImportToplevel) {
				addImport((AImport) ((AImportToplevel) toplevel).getImport());
			} else if (toplevel instanceof AConfigToplevel) {
				SAMClass klass = processConfig((AConfig) ((AConfigToplevel) toplevel).getConfig());
				if (classDefinitions.containsKey(klass.name)) {
					throw new RuntimeException("Duplicate class definition: " + klass.name);
				}
				classDefinitions.put(klass.name, klass);
			} else {
				throw new RuntimeException("UNKNOWN " + toplevel + ", " + toplevel.getClass());
			}
		}

		for (SAMClass klass : classDefinitions.values()) {
			klass.addDatalog();
		}
	}

	public List<IQuery> getQueries() {
		return queries;
	}


	private void addFact(AFact fact) throws ParserException {
		List<Token> tokens = new LinkedList<Token>();
		IAtom atom = model.parseAtom(fact.getAtom(), null, tokens);
		model.addFact(atom.getPredicate(), atom.getTuple(), tokens);
	}

	private void addRule(ARule rule) throws ParserException {
		List<List<Token>> tokens = new LinkedList<List<Token>>();
		List<Token> headTokens = new LinkedList<Token>();
		tokens.add(headTokens);
		ILiteral head = BASIC.createLiteral(true, model.parseAtom(rule.getHead(), null, headTokens));

		List<ILiteral> body = model.parseLiterals((ALiterals) rule.getBody(), null, tokens);

		IRule r = BASIC.createRule(makeList(head), body);
		//System.out.println(" --> " + r);

		model.addRule(r, tokens);
	}

	private void addQuery(AQuery query) throws ParserException {
		List<List<Token>> tokens = new LinkedList<List<Token>>();
		List<ILiteral> literals = model.parseLiterals((ALiterals) query.getLiterals(), null, tokens);

		IQuery q = BASIC.createQuery(literals);

		model.validateQuery(q, tokens);

		queries.add(q);
	}

	private void addAssert(AAssert ass) throws ParserException {
		List<List<Token>> tokens = new LinkedList<List<Token>>();
		List<ILiteral> body = model.parseLiterals((ALiterals) ass.getLiterals(), null, tokens);

		if (body.size() != 1) {
			throw new ParserException(ass.getAssertTok(), "Assert must contain exactly one goal");
		}
		ILiteral bodyLit = body.get(0);

		int assertions = model.nextAssertion();

		ITerm assertionN = CONCRETE.createInteger(assertions);

		// If body contains a single literal that refers to two objects,
		// add a suitable red arrow to the graph
		//
		// For positive assertions, the object terms must be literals:
		//
		//   assert didCall("a", "b", ?M)
		// becomes
		//   assertionArrow("a", "b", false) :- !didCall("a", "b", ?M)
		//
		// For negative assertions:
		//
		//   assert !didCall(?X, "b", ?M).
		// becomes
		//   assertionArrow(?X, "b", true) :- didCall(?X, "b", ?M).
		int nObjectTerms = 0;
		ITerm[] objectTerms = new ITerm[2];

		IPredicate pred = bodyLit.getAtom().getPredicate();
		boolean positive = bodyLit.isPositive();
		TermDefinition[] terms = model.getDefinition(pred);
		//System.out.println(":" + ass + " => " + terms);

		int i = 0;
		for (TermDefinition term : terms) {
			String var = term.name;
			ITuple bodyTuple = bodyLit.getAtom().getTuple();

			if ("caller".equals(var) || "target".equals(var) || "object".equals(var) ||
			    "source".equals(var) || "sourceObject".equals(var) || "targetObject".equals(var)) {
				ITerm bodyTerm = bodyTuple.get(i);
				if (positive && !(bodyTerm instanceof IConcreteTerm)) {
					//System.out.println("Can't do arrow for variable term " + bodyTerm + " in " + ass);
					break;
				}
				objectTerms[nObjectTerms] = bodyTerm;
				nObjectTerms++;
				if (nObjectTerms == 2) {
					break;
				}
			}
			i++;
		}

		ILiteral opposite = BASIC.createLiteral(!positive, bodyLit.getAtom());

		if (nObjectTerms == 2) {
			// assertionArrow(n, ?Source, ?Target, positive) :- !body
			ILiteral arrow = BASIC.createLiteral(true,
						BASIC.createAtom(assertionArrowP,
							BASIC.createTuple(
								assertionN,
								objectTerms[0],
								objectTerms[1],
								CONCRETE.createBoolean(bodyLit.isPositive()))));

			IRule r = BASIC.createRule(makeList(arrow), makeList(opposite));
			model.addRule(r);
			//System.out.println(r);

			// failedAssertion(?N) :- assertionArrow(?N, ?Source, ?Target, ?Positive) in checks.sam
		} else {
			// failedAssertion(?N) :- !body
			ILiteral head = BASIC.createLiteral(true,
						BASIC.createAtom(failedAssertionP,
							BASIC.createTuple(assertionN)));

			IRule r = BASIC.createRule(makeList(head), makeList(opposite));
			model.addRule(r);
		}

		// assertionMsg(n, msg).
		String loc = myPath.getName() + ":" + ass.getAssertTok().getLine();
		String msg = "Assertion failed (" + loc + "): ";
		boolean first = true;
		for (ILiteral lit : body) {
			if (first) {
				first = false;
			} else {
				msg += ", ";
			}
			msg += lit;
		}

		model.addFact(assertionMessageP, BASIC.createTuple(assertionN, TERM.createString(msg)));
	}

	private void addDeclare(PDeclare declare) throws ParserException {
		if (declare instanceof APredicateDeclare) {
			declarePredicate((APredicateDeclare) declare);
		} else {
			declareScenario((AScenarioDeclare) declare);
		}
	}

	private void declarePredicate(APredicateDeclare declare) throws ParserException {
		ATermDecls decls = (ATermDecls) declare.getTermDecls();

		TName name = declare.getName();

		if (decls == null) {
			IPredicate predicate = BASIC.createPredicate(name.getText(), 0);
			model.declare(name, predicate, new TermDefinition[] {});
		} else {
			ATermDecl first = (ATermDecl) decls.getTermDecl();
			List<PTermDeclsTail> rest = decls.getTermDeclsTail();

			TermDefinition[] terms = new TermDefinition[1 + rest.size()];

			terms[0] = new TermDefinition(first);
			int i = 1;
			for (PTermDeclsTail tail : rest) {
				terms[i] = new TermDefinition(((ATermDeclsTail) tail).getTermDecl());
				i++;
			}

			IPredicate predicate = BASIC.createPredicate(name.getText(), terms.length);
			model.declare(name, predicate, terms);
		}
	}

	private void declareScenario(AScenarioDeclare declare) throws ParserException {
		TName name = declare.getName();
		model.addScenario(name);
	}

	private void addImport(AImport aImport) throws Exception {
		String path = getString(aImport.getStringLiteral());

		try {
			new SAMParser(model, new File(dir, path));
		} catch (InvalidModelException ex) {
			throw new AddImportException(aImport.getStringLiteral(), ex);
		}
	}

	/* Get the nth line of a file. */
	private String getLine(Reader source, int line) throws Exception {
		BufferedReader in = new BufferedReader(source);
		for (int i = 1; i < line; i++) {
			in.readLine();
		}
		String lineText = in.readLine();
		if (lineText == null) {
			return "(end-of-file)";
		}
		return lineText;
	}

	private SAMClass processConfig(AConfig config) throws ParserException {
		AConfigBody body = (AConfigBody) config.getConfigBody();
		SAMTestDriver testDriver = new SAMTestDriver(model);

		for (PConfigField field : body.getConfigField()) {
			testDriver.declareField(((AConfigField) field).getName());
		}

		for (PNamedblock block : body.getNamedblock()) {
			ANamedblock namedBlock = (ANamedblock) block;
			String name = namedBlock.getName().getText();

			if ("setup".equals(name) || "test".equals(name)) {
				testDriver.add(namedBlock);
			} else {
				throw new ParserException(namedBlock.getName(), "Unknown block '" + name + "'; try 'setup' or 'test'");
			}
		}

		return testDriver;
	}

	private static class AddImportException extends ParserException {
		public AddImportException(Token token, InvalidModelException cause) {
			super(token, "Error in import");
			initCause(cause);
		}
	}
}
