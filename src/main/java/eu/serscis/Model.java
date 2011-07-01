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
//	Created Date :			2011-04-14
//	Created for Project :		SERSCIS
//
/////////////////////////////////////////////////////////////////////////
//
//  License : GNU Lesser General Public License, version 2.1
//
/////////////////////////////////////////////////////////////////////////

package eu.serscis;

import eu.serscis.sam.parser.ParserException;
import eu.serscis.sam.node.Token;
import java.io.BufferedReader;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.io.FileWriter;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileReader;
import java.io.Reader;
import java.io.IOException;
import org.deri.iris.Configuration;
import org.deri.iris.KnowledgeBaseFactory;
import org.deri.iris.api.IKnowledgeBase;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.basics.ILiteral;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.compiler.Parser;
import org.deri.iris.storage.IRelation;
import org.deri.iris.rules.IRuleSafetyProcessor;
import org.deri.iris.RuleUnsafeException;
import org.deri.iris.compiler.BuiltinRegister;
import static org.deri.iris.factory.Factory.*;
import eu.serscis.Constants;

public class Model {
	final Configuration configuration;
	final List<IRule> rules = new LinkedList<IRule>();
	final Map<IPredicate,IRelation> facts = new HashMap<IPredicate,IRelation>();
	final Set<IPredicate> declared = new HashSet<IPredicate>();

	public Model(Configuration configuration) {
		this.configuration = configuration;
	}

	public IRelation getRelation(IPredicate pred) {
		IRelation rel = facts.get(pred);
		if (rel == null) {
			rel = configuration.relationFactory.createRelation();
			facts.put(pred, rel);
		}
		return rel;
	}
	
	public IKnowledgeBase createKnowledgeBase() throws Exception {
		Map<IPredicate,IRelation> workingFacts = new HashMap<IPredicate,IRelation>();

		/* Make a copy of the initial facts (otherwise the debugger doesn't work). */
		for (Map.Entry<IPredicate,IRelation> entry: facts.entrySet()) {
			IRelation copy = configuration.relationFactory.createRelation();
			copy.addAll(entry.getValue());
			workingFacts.put(entry.getKey(), copy);
		}

		return KnowledgeBaseFactory.createKnowledgeBase(workingFacts, rules, configuration);
	}

	public void requireDeclared(Token tok, IPredicate pred) throws ParserException {
		if ("error".equals(pred.getPredicateSymbol())) {
			return;
		}

		if (!declared.contains(pred)) {
			throw new ParserException(tok, "Predicate not declared: " + pred + "/" + pred.getArity());
		}
	}
}
