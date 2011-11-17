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
	private final LiveResults myResults;
	private final Shell myShell;

	private ResultsTable myLocals;
	private ResultsTable myFields;
	private ResultsTable myCalled;
	private ResultsTable myWasCalled;
	private ResultsTable myHasRoles;
	private ResultsTable myGrantsRoles;

	public ObjectViewer(Shell parent, final LiveResults results, final String name) throws Exception {
		myShell = new Shell(parent, SWT.BORDER | SWT.CLOSE | SWT.MIN | SWT.MAX | SWT.RESIZE | SWT.TITLE);
		myShell.setText(name);

		myName = name;
		myResults = results;

		TabFolder folder = new TabFolder(myShell, 0);

		IQuery fieldsQ = BASIC.createQuery(
				BASIC.createLiteral(true, Constants.fieldP,
					BASIC.createTuple(
						TERM.createString(myName),
						TERM.createVariable("FieldName"),
						TERM.createVariable("Value"))));

		IQuery localsQ = BASIC.createQuery(
				BASIC.createLiteral(true, Constants.localP,
					BASIC.createTuple(
						TERM.createString(myName),
						TERM.createVariable("Invocation"),
						TERM.createVariable("VarName"),
						TERM.createVariable("Value"))));

		IQuery wasCalledQ = BASIC.createQuery(
				BASIC.createLiteral(true, Constants.didCallP,
					BASIC.createTuple(
						TERM.createVariable("Caller"),
						TERM.createVariable("CallerInvocation"),
						TERM.createVariable("CallSite"),
						TERM.createString(myName),
						TERM.createVariable("TargetInvocation"),
						TERM.createVariable("Method"))));

		IQuery calledQ = BASIC.createQuery(
				BASIC.createLiteral(true, Constants.didCallP,
					BASIC.createTuple(
						TERM.createString(myName),
						TERM.createVariable("CallerInvocation"),
						TERM.createVariable("CallSite"),
						TERM.createVariable("Target"),
						TERM.createVariable("TargetInvocation"),
						TERM.createVariable("Method"))));

		IQuery hasRolesQ = BASIC.createQuery(
				BASIC.createLiteral(true, Constants.grantsRoleP,
					BASIC.createTuple(
						TERM.createVariable("Object"),
						TERM.createVariable("Role"),
						TERM.createVariable("Identity"))),

				BASIC.createLiteral(true, Constants.hasIdentityP,
					BASIC.createTuple(
						TERM.createString(myName),
						TERM.createVariable("Identity"))));

		IQuery grantsRolesQ = BASIC.createQuery(
				BASIC.createLiteral(true, Constants.grantsRoleP,
					BASIC.createTuple(
						TERM.createString(myName),
						TERM.createVariable("Role"),
						TERM.createVariable("Identity"))));

		addTab(folder, "Fields", fieldsQ);
		addTab(folder, "Local variables", localsQ);
		addTab(folder, "Was called", wasCalledQ);
		addTab(folder, "Has roles", hasRolesQ);
		addTab(folder, "Grants roles", grantsRolesQ);

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

	private void addTab(TabFolder folder, String tabLabel, IQuery query) throws Exception {
		TabItem tabItem = new TabItem(folder, 0);
		tabItem.setText(tabLabel);

		ResultsTable table = new ResultsTable(folder, query, myResults);
		tabItem.setControl(table.getTable());
	}
}
