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
//	Created Date :			2011-12-08
//	Created for Project :		SERSCIS
//
/////////////////////////////////////////////////////////////////////////
//
//  License : GNU Lesser General Public License, version 2.1
//
/////////////////////////////////////////////////////////////////////////

package eu.serscis.sam;

import eu.serscis.sam.node.TNumber;
import eu.serscis.sam.node.TBool;
import eu.serscis.sam.node.TName;
import eu.serscis.sam.node.TRefLiteral;
import eu.serscis.sam.node.TStringLiteral;
import java.util.Iterator;
import org.deri.iris.api.basics.IQuery;
import eu.serscis.sam.node.AAnyTerm;
import org.deri.iris.api.terms.concrete.IBooleanTerm;
import org.deri.iris.api.terms.concrete.IIntegerTerm;
import org.deri.iris.api.terms.IStringTerm;
import org.deri.iris.api.terms.IVariable;
import eu.serscis.sam.node.ARefTerm;
import eu.serscis.sam.node.AJavavarTerm;
import eu.serscis.sam.node.ANeqBinop;
import eu.serscis.sam.node.AEqBinop;
import eu.serscis.sam.node.PBinop;
import eu.serscis.sam.node.ANegativeLiteral;
import eu.serscis.sam.node.APositiveLiteral;
import eu.serscis.sam.node.PLiteral;
import eu.serscis.sam.node.ALiteralTail;
import eu.serscis.sam.node.PLiteralTail;
import eu.serscis.sam.node.ALiterals;
import eu.serscis.sam.node.ATermTail;
import eu.serscis.sam.node.PTermTail;
import eu.serscis.sam.node.ABuiltinAtom;
import eu.serscis.sam.node.ANullaryAtom;
import eu.serscis.sam.node.ANormalAtom;
import eu.serscis.sam.node.AIntTerm;
import eu.serscis.sam.node.ABoolTerm;
import eu.serscis.sam.node.AVarTerm;
import eu.serscis.sam.node.AStringTerm;
import org.deri.iris.api.basics.IAtom;
import eu.serscis.sam.node.PAtom;
import eu.serscis.sam.node.ATerms;
import eu.serscis.sam.node.PTerm;
import eu.serscis.sam.parser.ParserException;
import eu.serscis.sam.node.Token;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.deri.iris.Configuration;
import org.deri.iris.KnowledgeBaseFactory;
import org.deri.iris.api.IKnowledgeBase;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.basics.ILiteral;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.storage.IRelation;
import org.deri.iris.compiler.BuiltinRegister;
import static org.deri.iris.factory.Factory.*;

public class TypeChecker {
	private final Model model;
	private final Map<IVariable,Type> types = new HashMap<IVariable,Type>();
	private final Map<IVariable,IVariable> canonical = new HashMap<IVariable,IVariable>();

	public TypeChecker(Model model) {
		//System.out.println("\nnew TypeChecker");
		this.model = model;
	}

	private IVariable lookupAlias(IVariable var) {
		IVariable c = canonical.get(var);
		if (c == null) {
			return var;
		} else {
			return c;
		}
	}

	/* Add a mapping alias -> can
	 * If we have type information for alias, merge it into can.
	 */
	private void setCanonical(IVariable alias, IVariable can) {
		//System.out.println("alias " + alias + " -> " + can);
		Type type = types.get(can);
		if (type == null) {
			type = Type.ObjectT;
		}

		/* e.g. if we know that ?X is an Object and ?Y is a Value
		 * and we are making ?Y an alias for ?X, then the type of ?X
		 * is now Value.
		 */
		Type t2 = types.get(alias);
		if (t2 != null) {
			type = type.intersect(t2);
			types.remove(alias);
			types.put(can, type);
		}

		/* Update aliases pointing to 'alias' */
		for (Map.Entry<IVariable,IVariable> entry: canonical.entrySet()) {
			if (entry.getValue().equals(alias)) {
				canonical.put(entry.getKey(), can);
			}
		}

		canonical.put(alias, can);
	}

	private void updateTypesEqual(ITuple tuple, List<Token> tokens) throws ParserException {
		Type type = Type.ObjectT;
		IVariable first = null;

		/* Intersect all (both) terms to get the type of both */
		for (ITerm term : tuple) {
			if (term instanceof IVariable) {
				IVariable var = (IVariable) term;
				term = lookupAlias(var);

				if (first == null) {
					first = var;
				} else {
					// create a new alias
					setCanonical(var, first);
					term = first;
				}
				Type termType = types.get(term);
				if (termType != null) {
					type = type.intersect(termType);
				}
			} else {
				type = type.intersect(Type.fromTerm(term));
			}
		}
		
		if (first == null) {
			throw new ParserException(tokens == null ? null : tokens.get(0), "No variables: " + tuple);
		}
		types.put(first, type);
	}

	public void check(ILiteral lit, boolean head, List<Token> tokens) throws ParserException {
		//System.out.println("check " + lit);
		IPredicate predicate = lit.getAtom().getPredicate();
		ITuple tuple = lit.getAtom().getTuple();
		TermDefinition[] terms = model.getDefinition(predicate);
		if (tokens != null && tokens.size() != tuple.size() + 1) {
			throw new RuntimeException("Wrong number of tokens:\n" + tokens + "\n" + lit);
		}

		if (terms == null) {
			// Special-case the type for ASSIGN("type", ?X, ?Y).
			if (predicate.equals(Constants.ASSIGNP)) {
				if (tuple.get(0) instanceof IStringTerm) {
					Type type = Type.fromJavaName(tuple.get(0).getValue().toString());
					terms = new TermDefinition[] {
							new TermDefinition(Type.StringT),
							new TermDefinition(Type.ObjectT),
							new TermDefinition(type)
						};
				} else {
					terms = new TermDefinition[] {
							new TermDefinition(Type.StringT),
							new TermDefinition(Type.ObjectT),
							new TermDefinition(Type.ObjectT)
						};
				}
			} else if ((predicate.equals(Constants.EQUALP) ||
				    predicate.equals(Constants.MATCH2P) ||
				    predicate.equals(Constants.MATCH_TOP)) && !head) {
				updateTypesEqual(tuple, tokens);
				return;
			} else if ((predicate.getPredicateSymbol().equals("error")) && tuple.size() > 0) {
				terms = new TermDefinition[tuple.size()];
				terms[0] = new TermDefinition(Type.StringT);
				for (int i = 1; i < terms.length; i++) {
					terms[i] = new TermDefinition(Type.ObjectT);
				}
			} else if ((predicate.getPredicateSymbol().equals("STRING_CONCAT"))) {
				terms = new TermDefinition[tuple.size()];
				TermDefinition s = new TermDefinition(Type.ObjectT);
				for (int i = 0; i < terms.length - 1; i++) {
					terms[i] = s;
				}
				terms[terms.length - 1] = new TermDefinition(Type.StringT);
			} else {
				throw new ParserException(tokens == null ? null : tokens.get(0), "Unknown predicate: " + predicate);
			}
		}

		//System.out.println(predicate);

		int i = 0;
		for (ITerm term : tuple) {
			try {
				Type termType;
				if (term instanceof IVariable) {
					term = lookupAlias((IVariable) term);
					if (types == null) {
						throw new ParserException(null, "Variable in fact: " + term);
					}
					termType = types.get(term);
					if (termType == null) {
						if (head) {
							throw new ParserException(null, "Unlimited variable: " + term);
						}
						termType = terms[i].type;
					} else {
						termType = terms[i].checkType(termType, head);
					}
					if (lit.isPositive()) {
						types.put((IVariable) term, termType);
					}
				} else {
					termType = Type.fromTerm(term);
					terms[i].checkType(termType, head);
				}
			} catch (ParserException ex) {
				throw new ParserException(tokens == null ? null : tokens.get(i + 1), ex.getMessage() + "\nin " + predicate + "'s " + term);
			}

			i++;
		}
	}
}
