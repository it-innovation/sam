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
//	Created Date :			2011-08-09
//	Created for Project :		SERSCIS
//
/////////////////////////////////////////////////////////////////////////
//
//  License : GNU Lesser General Public License, version 2.1
//
/////////////////////////////////////////////////////////////////////////

package eu.serscis.sam;

import eu.serscis.sam.RefTerm;
import eu.serscis.sam.node.*;
import java.util.LinkedList;
import java.util.List;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.basics.ILiteral;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.storage.IRelation;
import static org.deri.iris.factory.Factory.*;
import static eu.serscis.sam.Constants.*;

class SAMTestDriver extends SAMClass {
	private List<ANamedblock> blocks = new LinkedList<ANamedblock>();
	private int count = 1;

	public SAMTestDriver(Model model) {
		super(model, "_TestDriver");
	}

	public void addDatalog() throws Exception {
		super.addDatalog();

		IRelation hasMethodRel = model.getRelation(hasMethodP);
		IRelation methodNameRel = model.getRelation(methodNameP);

		for (ANamedblock block : blocks) {
			addMethod(block);
		}
	}

	private void addMethod(ANamedblock block) throws Exception {
		String context = block.getContext() != null ? getString(block.getContext()) : "";
		String phase = block.getName().getText();
		String methodName = phase + "_" + context + count;
		count++;

		ITerm methodNameFull = TERM.createString(this.name + "." + methodName);

		IRelation hasMethodRel = model.getRelation(hasMethodP);
		IRelation methodNameRel = model.getRelation(methodNameP);

		// hasMethod(type, "class.method")
		hasMethodRel.add(BASIC.createTuple(TERM.createString(this.name), methodNameFull));

		// methodName("class.method", "method")
		methodNameRel.add(BASIC.createTuple(methodNameFull, TERM.createString(methodName)));

		AMethod method = new AMethod(
			new LinkedList<PAnnotation>(),
			null,
			new AType(),
			new ANamedPattern(block.getName()),
			new TLPar(),
			null,
			null,
			new TRPar(),
			new TLBrace(),
			block.getCode(),
			new TRBrace());

		SAMMethod sm = new SAMMethod(this, method, methodNameFull);
		sm.addDatalog();

		/* initialInvocation("_testDriver", context, method) :- phase(name) */
		ITuple headTuple = BASIC.createTuple(
					new RefTerm("_testDriver"),
					TERM.createString(methodName),
					TERM.createString(context));

		ILiteral head = BASIC.createLiteral(true, BASIC.createAtom(initialInvocationP, headTuple));

		ILiteral phaseL = BASIC.createLiteral(true, BASIC.createAtom(phaseP, BASIC.createTuple(
						TERM.createString(phase))));

		IRule rule = BASIC.createRule(makeList(head), makeList(phaseL));
		//System.out.println(rule);
		model.rules.add(rule);
	}

	public void add(ANamedblock block) {
		blocks.add(block);
	}
}
