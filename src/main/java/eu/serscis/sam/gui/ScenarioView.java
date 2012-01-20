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
//	Created Date :			2011-08-17
//	Created for Project :		SERSCIS
//
/////////////////////////////////////////////////////////////////////////
//
//  License : GNU Lesser General Public License, version 2.1
//
/////////////////////////////////////////////////////////////////////////

package eu.serscis.sam.gui;

import eu.serscis.sam.Phase;
import eu.serscis.sam.ScenarioResult;
import org.eclipse.swt.graphics.Font;
import java.util.Iterator;
import eu.serscis.sam.InvalidModelException;
import eu.serscis.sam.TermDefinition;
import org.eclipse.swt.custom.ScrolledComposite;
import org.deri.iris.api.terms.ITerm;
import java.io.FileWriter;
import java.io.Writer;
import java.util.LinkedList;
import eu.serscis.sam.Constants;
import org.deri.iris.api.basics.ILiteral;
import org.deri.iris.storage.IRelation;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.basics.IPredicate;
import java.util.Arrays;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.FillLayout;
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
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.RowLayout;
import static org.deri.iris.factory.Factory.*;

public class ScenarioView {
	private Label mainImage;
	private List messageList;
	private LinkedList<ILiteral> myProblems;
	private TabItem tab;
	private Color white;
	private final GUI gui;
	private Display display;
	private Composite view;

	public ScenarioView(final GUI gui, TabFolder parent) {
		this.gui = gui;
		final Shell shell = parent.getShell();
		display = shell.getDisplay();
		white = new Color(display, 255, 255, 255);

		tab = new TabItem(parent, 0);

		view = new Composite(parent, 0);
		tab.setControl(view);

		final ScrolledComposite mainScrollArea = new ScrolledComposite(view,
							SWT.H_SCROLL | SWT.V_SCROLL);

		mainImage = new Label(mainScrollArea, SWT.LEFT);
		mainImage.setBackground(white);
		mainScrollArea.setContent(mainImage);
		mainScrollArea.setBackground(white);

		messageList = new List(view, 0);
		messageList.addSelectionListener(new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {
				ILiteral optProblem = myProblems.get(messageList.getSelectionIndex());
				System.out.println(optProblem);
				if (optProblem != null) {
					try {
						new DebugViewer(shell, gui.liveResults, optProblem);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		});

		GridLayout gridLayout = new GridLayout();
 		gridLayout.numColumns = 1;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.verticalSpacing = 0;

		GridData imageLayoutData = new GridData();
		imageLayoutData.horizontalAlignment = GridData.FILL;
		imageLayoutData.verticalAlignment = GridData.FILL;
		imageLayoutData.grabExcessHorizontalSpace = true;
		imageLayoutData.grabExcessVerticalSpace = true;
		mainScrollArea.setLayoutData(imageLayoutData);

		GridData assertionsLayoutData = new GridData();
		assertionsLayoutData.horizontalAlignment = GridData.FILL;
		assertionsLayoutData.verticalAlignment = GridData.FILL;
		assertionsLayoutData.grabExcessHorizontalSpace = true;
		assertionsLayoutData.grabExcessVerticalSpace = false;
		messageList.setLayoutData(assertionsLayoutData);

 		view.setLayout(gridLayout);
	}

	public TabItem getTab() {
		return tab;
	}

	public void update(LiveResults liveResults) throws Exception {
		gui.relationsMenuHeader.setEnabled(false);
		gui.objectsMenuHeader.setEnabled(false);

		for (MenuItem item : gui.relationsMenu.getItems()) {
			item.dispose();
		}

		for (MenuItem item : gui.objectsMenu.getItems()) {
			item.dispose();
		}

		myProblems = new LinkedList<ILiteral>();
		messageList.removeAll();

		ScenarioResult results = liveResults.getResults();

		if (results.phase != Phase.Success) {
			addWarning("Problem in " + results.phase + " phase", null);
		} else {
			addInfo("OK");
		}

		for (ILiteral errorLit : results.errors) {
			ITuple tuple = errorLit.getAtom().getTuple();
			String msg = tuple.get(0).getValue().toString();
			for (int part = 1; part < tuple.size(); part++) {
				msg += ", " + tuple.get(part).getValue();
			}

			addWarning(msg, errorLit);
		}

		if (results.finalKnowledgeBase != null) {
			File tmpFile = File.createTempFile("sam-", "-graph.png");
			Image image;
			try {
				Graph.graph(results.finalKnowledgeBase, tmpFile, "png");
				InputStream is = new FileInputStream(tmpFile);
				try {
					image = new Image(display, is);
				} finally {
					is.close();
				}
			} finally {
				tmpFile.delete();
			}

			mainImage.setImage(image);
			mainImage.pack();

			gui.relationsMenuHeader.setEnabled(true);
			gui.objectsMenuHeader.setEnabled(true);

			// Populate Relations menu
			IPredicate[] relations = results.model.declared.keySet().toArray(new IPredicate[] {});
			Arrays.sort(relations);
			for (final IPredicate pred : relations) {
				MenuItem item = new MenuItem(gui.relationsMenu, SWT.PUSH);
				final TermDefinition[] args = results.model.declared.get(pred);
				item.setText(pred.toString() + TermDefinition.makeTuple(args));
				item.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						try {
							new RelationViewer(gui.shell, gui.liveResults, pred);
						} catch (Throwable ex) {
							ex.printStackTrace();
						}
					}
				});
			}

			// Populate Objects menu
			String[] objects = GUI.getObjects(results);
			for (final String name : objects) {
				MenuItem item = new MenuItem(gui.objectsMenu, SWT.PUSH);
				item.setText(name);
				item.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						try {
							new ObjectViewer(gui.shell, gui.liveResults, name);
						} catch (Throwable ex) {
							ex.printStackTrace();
						}
					}
				});
			}
		} else {
			mainImage.setImage(null);
		}

		messageList.pack();

		view.layout();
	}

	private void addWarning(String msg, ILiteral optProblem) {
		myProblems.add(optProblem);
		messageList.add(msg);
	}

	private void addInfo(String msg) {
		myProblems.add(null);
		messageList.add(msg);
	}

	public void dispose() {
		tab.dispose();
		tab = null;
		mainImage = null;
	}
}
