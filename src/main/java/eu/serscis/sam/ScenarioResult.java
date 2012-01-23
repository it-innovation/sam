/////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2012
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
//	Created Date :			2012-01-19
//	Created for Project :		SERSCIS
//
/////////////////////////////////////////////////////////////////////////
//
//  License : GNU Lesser General Public License, version 2.1
//
/////////////////////////////////////////////////////////////////////////

package eu.serscis.sam;

import java.util.Map;
import java.util.HashMap;
import java.io.FileWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.LinkedList;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import org.deri.iris.storage.IRelation;
import org.deri.iris.rules.IRuleSafetyProcessor;
import org.deri.iris.RuleUnsafeException;
import static org.deri.iris.factory.Factory.*;

public class ScenarioResult {
	public Model model;
	public IKnowledgeBase finalKnowledgeBase;
	public Phase phase = Phase.Init;
	public boolean expectingFailure = false;
	public List<ILiteral> errors = new LinkedList<ILiteral>();

	public ScenarioResult(Model model) {
		this.model = new Model(model);
	}
	
	public Map<IPredicate,IRelation> saveBaseline(Writer writer) throws Exception {
		Map<IPredicate,IRelation> baseline = new HashMap<IPredicate,IRelation>();

		IPredicate[] relations = model.declared.keySet().toArray(new IPredicate[] {});
		Arrays.sort(relations);
		for (IPredicate pred : relations) {
			TermDefinition[] termDefinitions = model.declared.get(pred);
			IQuery query = BASIC.createQuery(BASIC.createLiteral(true, pred, TermDefinition.makeTuple(termDefinitions)));
			IRelation rel = finalKnowledgeBase.execute(query);
			baseline.put(pred, rel);

			if (rel.size() == 0) {
				continue;
			}

			String[] rows = new String[rel.size()];
			for (int i = 0; i < rows.length; i++) {
				rows[i] = rel.get(i).toString() + "\n";
			}
			Arrays.sort(rows);

			writer.write(pred.toString() + "\n");
			for (String row : rows) {
				writer.write(row);
			}
			writer.write("\n");
		}

		return baseline;
	}

	public void saveDiff(Writer writer, Map<IPredicate,IRelation> baseline) throws Exception {
		IPredicate[] relations = model.declared.keySet().toArray(new IPredicate[] {});
		Arrays.sort(relations);
		for (IPredicate pred : relations) {
			TermDefinition[] termDefinitions = model.declared.get(pred);
			IQuery query = BASIC.createQuery(BASIC.createLiteral(true, pred, TermDefinition.makeTuple(termDefinitions)));
			IRelation rel = finalKnowledgeBase.execute(query);

			IRelation baseRel = baseline.get(pred);
			if (baseRel == null) {
				baseRel = model.configuration.relationFactory.createRelation();	// empty
			}

			ArrayList<String> items = new ArrayList<String>();

			for (int i = rel.size() - 1; i >= 0; i--) {
				ITuple tup = rel.get(i);
				if (!baseRel.contains(tup)) {
					items.add("+ " + tup.toString() + "\n");
				}
			}

			for (int i = baseRel.size() - 1; i >= 0; i--) {
				ITuple tup = baseRel.get(i);
				if (!rel.contains(tup)) {
					items.add("- " + tup.toString() + "\n");
				}
			}

			if (items.size() == 0) {
				continue;
			}

			String[] rows = new String[items.size()];
			for (int i = 0; i < rows.length; i++) {
				rows[i] = items.get(i).toString();
			}
			Arrays.sort(rows);

			writer.write(pred.toString() + "\n");
			for (String row : rows) {
				writer.write(row);
			}
			writer.write("\n");
		}

		// Check for entire relations that are missing
		IPredicate[] baseRels = baseline.keySet().toArray(new IPredicate[] {});
		Arrays.sort(baseRels);
		for (IPredicate pred : baseRels) {
			if (!model.declared.containsKey(pred)) {
				writer.write("- " + pred + "\n");
			}
		}
	}

	public String[] getObjects() throws Exception {
		ILiteral lit = BASIC.createLiteral(true, Constants.isRefP, BASIC.createTuple(TERM.createVariable("Object")));

		IQuery query = BASIC.createQuery(lit);
		IRelation rel = finalKnowledgeBase.execute(query);
		String[] objects = new String[rel.size()];
		for (int i = 0; i < objects.length; i++) {
			objects[i] = rel.get(i).get(0).getValue().toString();
		}
		Arrays.sort(objects);
		return objects;
	}

}
