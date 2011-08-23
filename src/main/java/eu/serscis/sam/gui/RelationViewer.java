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
//	Created Date :			2011-08-23
//	Created for Project :		SERSCIS
//
/////////////////////////////////////////////////////////////////////////
//
//  License : GNU Lesser General Public License, version 2.1
//
/////////////////////////////////////////////////////////////////////////

package eu.serscis.sam.gui;

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

public class RelationViewer {
	public RelationViewer(Shell parent, IRelation relation, IPredicate pred, ITuple columns) {
		Shell shell = new Shell(parent, SWT.RESIZE);
		shell.setText(pred.toString() + columns);

		Table table = new Table(shell, SWT.BORDER);

		int nColumns = columns.size();
		for (int c = 0; c < nColumns; c++) {
			TableColumn column = new TableColumn(table, SWT.LEFT);
			column.setText(columns.get(c).toString());
		}
		table.setHeaderVisible(true);

		ITuple[] rows = new ITuple[relation.size()];
		for (int i = 0; i < rows.length; i++) {
			rows[i] = relation.get(i);
		}
		Arrays.sort(rows);

		for (int i = 0; i < rows.length; i++) {
			TableItem item = new TableItem(table, 0);

			for (int c = 0; c < nColumns; c++) {
				item.setText(c, rows[i].get(c).getValue().toString());
			}
		}

		for (TableColumn column : table.getColumns()) {
			column.pack();
		}

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.verticalSpacing = 0;

		GridData tableLayoutData = new GridData();
		tableLayoutData.horizontalAlignment = GridData.FILL;
		tableLayoutData.verticalAlignment = GridData.FILL;
		tableLayoutData.grabExcessHorizontalSpace = true;
		tableLayoutData.grabExcessVerticalSpace = true;
		table.setLayoutData(tableLayoutData);

		shell.setLayout(gridLayout);

		shell.open();
	}
}
