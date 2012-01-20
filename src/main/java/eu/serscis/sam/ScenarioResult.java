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
	
	public void save(Writer writer) throws Exception {
		IPredicate[] relations = model.declared.keySet().toArray(new IPredicate[] {});
		Arrays.sort(relations);
		for (IPredicate pred : relations) {
			TermDefinition[] termDefinitions = model.declared.get(pred);
			IQuery query = BASIC.createQuery(BASIC.createLiteral(true, pred, TermDefinition.makeTuple(termDefinitions)));
			IRelation rel = finalKnowledgeBase.execute(query);

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
	}
}
