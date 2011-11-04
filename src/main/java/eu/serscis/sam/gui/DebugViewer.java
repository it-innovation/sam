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

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionAdapter;
import java.util.HashMap;
import java.util.Map;
import org.deri.iris.api.basics.IRule;
import eu.serscis.sam.Reporter;
import eu.serscis.sam.Model;
import eu.serscis.sam.Constants;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.ILiteral;
import org.deri.iris.storage.IRelation;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.basics.IPredicate;
import java.util.Arrays;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.RowLayout;
import static org.deri.iris.factory.Factory.*;
import eu.serscis.sam.Debugger;

public class DebugViewer implements Updatable {
	private final Shell myShell;
	private final Tree myTree;
	private final Text myText;
	private final LiveResults myResults;
	private final ILiteral myProblem;
	private final Map<TreeItem,Details> extraData = new HashMap<TreeItem,Details>();

	public DebugViewer(final Shell parent, final LiveResults results, ILiteral problem) throws Exception {
		myShell = new Shell(parent, SWT.RESIZE);
		myShell.setText("Debug: " + problem);
		myResults = results;
		myProblem = problem;

		myTree = new Tree(myShell, 0);
		myText = new Text(myShell, SWT.MULTI | SWT.READ_ONLY);

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
		myTree.setLayoutData(tableLayoutData);

		GridData textLayoutData = new GridData();
		textLayoutData.horizontalAlignment = GridData.FILL;
		textLayoutData.verticalAlignment = GridData.FILL;
		textLayoutData.grabExcessHorizontalSpace = true;
		textLayoutData.grabExcessVerticalSpace = false;
		myText.setLayoutData(textLayoutData);

		myTree.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				TreeItem item = (TreeItem) e.item;
				Details details = extraData.get(item);
				myText.setText(details != null ? details.notes : "");
				myShell.layout();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				TreeItem item = (TreeItem) e.item;
				Details details = extraData.get(item);
				if (details != null && details.lit != null) {
					try {
						new DebugViewer(parent, results, details.lit);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		});

		myShell.setLayout(gridLayout);

		myShell.open();

		update();
	}

	public void update() throws Exception {
		if (myShell.isDisposed()) {
			return;
		}

		//System.out.println("refresh " + myShell.getText());

		myTree.removeAll();

		Model model = myResults.getResults().model;

		GUIReporter reporter = new GUIReporter();
		Debugger debugger = new Debugger(model);
		debugger.debug(myProblem, reporter);

		Display.getCurrent().timerExec(0, new Runnable() {
			public void run() {
				for (TreeItem item : myTree.getItems()) {
					item.setExpanded(true);
					for (TreeItem child : item.getItems()) {
						child.setExpanded(true);
					}
				}
			}
		});

		myResults.whenUpdated(this);
	}

	private class GUIReporter implements Reporter {
		private ILiteral optNegativeNeedHeader;
		private TreeItem currentItem;

		public void enter(ILiteral literal) {
			//System.out.println("enter " + literal + ", " + currentItem);
			TreeItem item;
			if (currentItem == null) {
				item = new TreeItem(myTree, 0);
			} else {
				item = new TreeItem(currentItem, 0);
			}
			item.setText("" + literal);

			extraData.put(item, new Details(literal));

			currentItem = item;
		}

		public void leave(ILiteral literal) {
			currentItem = currentItem.getParentItem();
		}

		public void noteNewProblem(ILiteral problem) {
		}

		public void noteQuery(IQuery ruleQ) {
			String msg = "Rule body:\n";
			for (ILiteral literal : ruleQ.getLiterals()) {
				msg += literal + "\n";
			}
			extraData.get(currentItem).notes += msg;
		}


		/* We are about to explain why literal is false. */
		public void enterNegative(ILiteral literal) {
			optNegativeNeedHeader = literal;
			currentItem = new TreeItem(currentItem, 0);
			currentItem.setText("" + literal + "; none of these was true:");
			extraData.put(currentItem, new Details(literal));
		}


		public void leaveNegative() {
			if (optNegativeNeedHeader != null) {
				currentItem.setText("" + optNegativeNeedHeader + "; no rules for this predicate");
			}
			currentItem = currentItem.getParentItem();
		}


		/* This rule might have been intended to fire, but there was no match. */
		public void noteNegative(IRule rule, IQuery unified) {
			optNegativeNeedHeader = null;

			TreeItem ruleItem = new TreeItem(currentItem, 0);
			ruleItem.setText("" + rule);

			Details details = new Details(null);
			details.notes += unified;

			extraData.put(ruleItem, details);
		}
	}

	private class Details {
		private ILiteral lit;
		private String notes = "";

		private Details(ILiteral lit) {
			this.lit = lit;
		}
	}
}
