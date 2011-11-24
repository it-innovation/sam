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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.deri.iris.storage.IRelation;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.ILiteral;
import org.eclipse.swt.layout.GridData;
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
import static org.deri.iris.factory.Factory.*;
import org.deri.iris.utils.TermMatchingAndSubstitution;

public class ResultsTable implements Updatable {
	private ITuple[] rows;
	private final IQuery myQuery;
	private final Table myTable;
	private final LiveResults myResults;
	private List<IVariable> myBindings;

	public ResultsTable(Composite parent, IQuery query, LiveResults results) throws Exception {
		myTable = new Table(parent, SWT.BORDER | SWT.FULL_SELECTION);
		myQuery = query;
		myResults = results;

		List<IVariable> vars = query.getVariables();
		for (IVariable var : vars) {
			String heading = var.toString();
			TableColumn column = new TableColumn(myTable, SWT.LEFT);
			column.setText(heading);
		}
		myTable.setHeaderVisible(true);

		GridData tableLayoutData = new GridData();
		tableLayoutData.horizontalAlignment = GridData.FILL;
		tableLayoutData.verticalAlignment = GridData.FILL;
		tableLayoutData.grabExcessHorizontalSpace = true;
		tableLayoutData.grabExcessVerticalSpace = true;
		myTable.setLayoutData(tableLayoutData);

		myTable.addSelectionListener(new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {
				try {
					if (rows != null) {
						showDebugger(rows[myTable.getSelectionIndex()]);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});

		update();
	}

	public void fillTable(IRelation rel) {
		rows = new ITuple[rel.size()];
		for (int i = 0; i < rows.length; i++) {
			rows[i] = rel.get(i);
		}
		Arrays.sort(rows);

		int nColumns = 0;
		if (rows.length > 0) {
			nColumns = rows[0].size();
		}

		myTable.removeAll();

		for (int i = 0; i < rows.length; i++) {
			TableItem item = new TableItem(myTable, 0);

			for (int c = 0; c < nColumns; c++) {
				item.setText(c, rows[i].get(c).getValue().toString());
			}
		}

		for (TableColumn column : myTable.getColumns()) {
			column.pack();
		}
	}

	public Table getTable() {
		return myTable;
	}

	public void update() throws Exception {
		if (myTable.isDisposed()) {
			return;
		}
		Results results = myResults.getResults();
		if (results.finalKnowledgeBase != null) {
			myBindings = new LinkedList<IVariable>();
			IRelation rel = results.finalKnowledgeBase.execute(myQuery, myBindings);
			fillTable(rel);
		} else {
			myTable.removeAll();
			rows = null;
			TableItem item = new TableItem(myTable, 0);
			item.setText("problem: " + results.exception);

			for (TableColumn column : myTable.getColumns()) {
				column.pack();
			}
		}

		myResults.whenUpdated(this);
	}

	private void showDebugger(ITuple row) throws Exception {
		// Go through myQuery replacing all the variables with values

		Map<IVariable,ITerm> varMap = new HashMap<IVariable,ITerm>();
		int i = 0;
		for (IVariable var : myBindings) {
			varMap.put(var, row.get(i));
			i++;
		}

		List<ILiteral> lits = new LinkedList<ILiteral>();
		for (ILiteral literal : myQuery.getLiterals()) {
			ITuple oldTuple = literal.getAtom().getTuple();
			ITuple newTuple = TermMatchingAndSubstitution.substituteVariablesInToTuple(oldTuple, varMap);
			lits.add(BASIC.createLiteral(true, literal.getAtom().getPredicate(), newTuple));
		}

		new DebugViewer(myTable.getShell(), myResults, BASIC.createQuery(lits));
	}
}
