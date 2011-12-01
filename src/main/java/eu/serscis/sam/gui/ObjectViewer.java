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
//	Created Date :			2011-11-03
//	Created for Project :		SERSCIS
//
/////////////////////////////////////////////////////////////////////////
//
//  License : GNU Lesser General Public License, version 2.1
//
/////////////////////////////////////////////////////////////////////////

package eu.serscis.sam.gui;

import eu.serscis.sam.TermDefinition;
import eu.serscis.sam.RefTerm;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.api.terms.ITerm;
import eu.serscis.sam.Constants;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.ILiteral;
import org.deri.iris.storage.IRelation;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.basics.IPredicate;
import java.util.Arrays;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import eu.serscis.sam.Graph;
import eu.serscis.sam.Eval;
import eu.serscis.sam.Results;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionAdapter;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.File;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.RowLayout;
import org.deri.iris.utils.TermMatchingAndSubstitution;
import static org.deri.iris.factory.Factory.*;

public class ObjectViewer implements Updatable {
	private final String myName;
	private final LiveResults myResults;
	private final Shell myShell;
	private final TabFolder myFolder;

	private Set<ITuple> myTabConfig = new HashSet<ITuple>();
	private List<Tab> myTabs = new LinkedList<Tab>();

	public ObjectViewer(Shell parent, final LiveResults results, final String name) throws Exception {
		myShell = new Shell(parent, SWT.BORDER | SWT.CLOSE | SWT.MIN | SWT.MAX | SWT.RESIZE | SWT.TITLE);
		myShell.setText(name);

		myName = name;
		myResults = results;

		myFolder = new TabFolder(myShell, 0);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.verticalSpacing = 0;

		GridData layoutData = new GridData();
		layoutData.horizontalAlignment = GridData.FILL;
		layoutData.verticalAlignment = GridData.FILL;
		layoutData.grabExcessHorizontalSpace = true;
		layoutData.grabExcessVerticalSpace = true;
		myFolder.setLayoutData(layoutData);

		myShell.setLayout(gridLayout);

		update();

		myShell.open();
	}

	// Checks if the tab configuration has changed
	public void update() throws Exception {
		if (myShell.isDisposed()) {
			return;
		}

		IQuery query = BASIC.createQuery(BASIC.createLiteral(true, Constants.guiObjectTabP,
					BASIC.createTuple(
						TERM.createVariable("?SortKey"),
						TERM.createVariable("?TabName"),
						TERM.createVariable("?Predicate"),
						TERM.createVariable("?ObjectVar"))));
		IRelation rel = myResults.getResults().finalKnowledgeBase.execute(query);

		Set<ITuple> newTabConfig = new HashSet<ITuple>();
		for (int i = rel.size() - 1; i >= 0; i--) {
			newTabConfig.add(rel.get(i));
		}

		if (newTabConfig.equals(myTabConfig)) {
			//System.out.println("No change in tabs");
		} else {
			for (Tab tab: myTabs) {
				tab.dispose();
			}
			ITuple[] rows = new ITuple[rel.size()];
			for (int i = 0; i < rows.length; i++) {
				rows[i] = rel.get(i);
			}
			Arrays.sort(rows);
			for (int i = 0; i < rows.length; i++) {
				ITuple tuple = rows[i];
				String tabName = tuple.get(1).getValue().toString();
				String predName = tuple.get(2).getValue().toString();
				String varName = tuple.get(3).getValue().toString();

				String[] predParts = predName.split("/");
				if (predParts.length != 2) {
					throw new RuntimeException("Bad tab configuration '" + tabName + "': predicate should be 'name/arity', not " + predName);
				}
				IPredicate pred = BASIC.createPredicate(predParts[0], Integer.valueOf(predParts[1]));
				ITuple args = TermDefinition.makeTuple(myResults.getResults().model.getDefinition(pred));
				if (args == null) {
					throw new RuntimeException("No such predicate " + pred + ", in configuration for GUI tab '" +tabName + "'");
				}

				IVariable var = TERM.createVariable(varName);
				Map<IVariable,ITerm> map = new HashMap<IVariable,ITerm>();
				map.put(var, new RefTerm(myName));

				if (!args.getVariables().contains(var)) {
					throw new RuntimeException("Declaration " + pred + args + " has no variable '" + var + "' requested for GUI tab '" + tabName + "'");
				}

				ITuple thisObjectArgs = TermMatchingAndSubstitution.substituteVariablesInToTuple(args, map);

				IQuery tabQuery = BASIC.createQuery(BASIC.createLiteral(true, pred, thisObjectArgs));
				myTabs.add(new Tab(tabName, tabQuery));
			}
			myTabConfig = newTabConfig;
		}

		myResults.whenUpdated(this);
	}

	private class Tab {
		private TabItem myTabItem;
		private ResultsTable myTable;
		private IQuery myQuery;

		private Tab(String tabLabel, IQuery query) throws Exception {
			myQuery = query;

			myTabItem = new TabItem(myFolder, 0);
			myTabItem.setText(tabLabel);

			myTable = new ResultsTable(myFolder, query, myResults);
			myTabItem.setControl(myTable.getTable());
		}

		private void dispose() {
			myTable.getTable().dispose();
			myTabItem.dispose();
		}
	}
}
