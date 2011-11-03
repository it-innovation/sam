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

import java.util.LinkedList;
import java.util.List;
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
import static org.deri.iris.factory.Factory.*;

public class ObjectViewer {
	private final String myName;
	private final Results myResults;
	private final Shell myShell;

	public ObjectViewer(Shell parent, Results results, String name) throws Exception {
		myShell = new Shell(parent, SWT.RESIZE);
		myShell.setText(name);

		myName = name;
		myResults = results;

		TabFolder folder = new TabFolder(myShell, 0);

		TabItem fieldsTab = new TabItem(folder, 0);
		fieldsTab.setText("Fields");
		Control fieldsBody = addFields(folder);
		fieldsTab.setControl(fieldsBody);

		TabItem localsTab = new TabItem(folder, 0);
		localsTab.setText("Local variables");
		Control localsBody = addLocals(folder);
		localsTab.setControl(localsBody);

		TabItem wasCalledTab = new TabItem(folder, 0);
		wasCalledTab.setText("Was called");
		Control wasCalledBody = addWasCalled(folder);
		wasCalledTab.setControl(wasCalledBody);

		TabItem calledTab = new TabItem(folder, 0);
		calledTab.setText("Called");
		Control calledBody = addCalled(folder);
		calledTab.setControl(calledBody);

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
		folder.setLayoutData(layoutData);

		myShell.setLayout(gridLayout);

		myShell.open();
	}

	private Control makeTable(Composite parent, List<IVariable> headings, ITuple[] rows) {
		Table table = new Table(parent, SWT.BORDER);

		int nColumns = headings.size();
		for (IVariable var : headings) {
			TableColumn column = new TableColumn(table, SWT.LEFT);
			column.setText(var.toString().substring(1));
		}
		table.setHeaderVisible(true);

		for (int i = 0; i < rows.length; i++) {
			TableItem item = new TableItem(table, 0);

			for (int c = 0; c < nColumns; c++) {
				item.setText(c, rows[i].get(c).getValue().toString());
			}
		}

		for (TableColumn column : table.getColumns()) {
			column.pack();
		}

		GridData tableLayoutData = new GridData();
		tableLayoutData.horizontalAlignment = GridData.FILL;
		tableLayoutData.verticalAlignment = GridData.FILL;
		tableLayoutData.grabExcessHorizontalSpace = true;
		tableLayoutData.grabExcessVerticalSpace = true;
		table.setLayoutData(tableLayoutData);
		
		return table;
	}

	private Control addLocals(Composite parent) throws Exception {
		ITuple args = BASIC.createTuple(
				TERM.createString(myName),
				TERM.createVariable("Invocation"),
				TERM.createVariable("VarName"),
				TERM.createVariable("Value"));

		ILiteral lit = BASIC.createLiteral(true, Constants.localP, args);
		IQuery query = BASIC.createQuery(lit);
		List<IVariable> bindings = new LinkedList<IVariable>();
		IRelation rel = myResults.finalKnowledgeBase.execute(query, bindings);

		ITuple[] rows = new ITuple[rel.size()];
		for (int i = 0; i < rows.length; i++) {
			rows[i] = rel.get(i);
		}
		Arrays.sort(rows);

		return makeTable(parent, bindings, rows);
	}

	private Control addFields(Composite parent) throws Exception {
		ITuple args = BASIC.createTuple(
				TERM.createString(myName),
				TERM.createVariable("FieldName"),
				TERM.createVariable("Value"));

		ILiteral lit = BASIC.createLiteral(true, Constants.fieldP, args);
		IQuery query = BASIC.createQuery(lit);
		List<IVariable> bindings = new LinkedList<IVariable>();
		IRelation rel = myResults.finalKnowledgeBase.execute(query, bindings);

		ITuple[] rows = new ITuple[rel.size()];
		for (int i = 0; i < rows.length; i++) {
			rows[i] = rel.get(i);
		}
		Arrays.sort(rows);

		return makeTable(parent, bindings, rows);
	}

	private Control addWasCalled(Composite parent) throws Exception {
		ITuple args = BASIC.createTuple(
				TERM.createVariable("Caller"),
				TERM.createVariable("CallerInvocation"),
				TERM.createVariable("CallSite"),
				TERM.createString(myName),
				TERM.createVariable("TargetInvocation"),
				TERM.createVariable("Method"));

		ILiteral lit = BASIC.createLiteral(true, Constants.didCallP, args);
		IQuery query = BASIC.createQuery(lit);
		List<IVariable> bindings = new LinkedList<IVariable>();
		IRelation rel = myResults.finalKnowledgeBase.execute(query, bindings);

		ITuple[] rows = new ITuple[rel.size()];
		for (int i = 0; i < rows.length; i++) {
			rows[i] = rel.get(i);
		}
		Arrays.sort(rows);

		return makeTable(parent, bindings, rows);
	}

	private Control addCalled(Composite parent) throws Exception {
		ITuple args = BASIC.createTuple(
				TERM.createString(myName),
				TERM.createVariable("CallerInvocation"),
				TERM.createVariable("CallSite"),
				TERM.createVariable("Target"),
				TERM.createVariable("TargetInvocation"),
				TERM.createVariable("Method"));

		ILiteral lit = BASIC.createLiteral(true, Constants.didCallP, args);
		IQuery query = BASIC.createQuery(lit);
		List<IVariable> bindings = new LinkedList<IVariable>();
		IRelation rel = myResults.finalKnowledgeBase.execute(query, bindings);

		ITuple[] rows = new ITuple[rel.size()];
		for (int i = 0; i < rows.length; i++) {
			rows[i] = rel.get(i);
		}
		Arrays.sort(rows);

		return makeTable(parent, bindings, rows);
	}
}
