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
//	Created Date :			2011-04-04
//	Created for Project :		SERSCIS
//
/////////////////////////////////////////////////////////////////////////
//
//  License : GNU Lesser General Public License, version 2.1
//
/////////////////////////////////////////////////////////////////////////

package eu.serscis.sam;

import eu.serscis.sam.node.*;
import java.util.List;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.basics.ILiteral;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.storage.IRelation;
import static org.deri.iris.factory.Factory.*;
import static eu.serscis.sam.Constants.*;

class SAMDeclaredClass extends SAMClass {
	private ABehaviour behaviour;

	public SAMDeclaredClass(Model model, ABehaviour behaviour) throws Exception {
		super(model, behaviour.getName().getText()); 
		this.behaviour = behaviour;
	}

	public void addDatalog() throws Exception {
		super.addDatalog();

		AExtends extend = (AExtends) behaviour.getExtends();
		if (extend != null) {
			String superclass = extend.getName().getText();
			ITuple objAndSuper = BASIC.createTuple(TERM.createVariable("X"), TERM.createString(superclass));
			ITuple objAndName = BASIC.createTuple(TERM.createVariable("X"), TERM.createString(name));
			ILiteral head = BASIC.createLiteral(true, isAP, objAndSuper);
			ILiteral body = BASIC.createLiteral(true, isAP, objAndName);
			IRule rule = BASIC.createRule(makeList(head), makeList(body));
			//System.out.println(rule);
			model.addRule(rule);
		}

		AClassBody body = (AClassBody) behaviour.getClassBody();

		for (PField f : body.getField()) {
			AField field = (AField) f;

			Type.validateJavaName(((AType) field.getType()).getName());
			declareField(field.getName());
		}

		IRelation hasConstructorRel = model.getRelation(hasConstructorP);
		IRelation hasMethodRel = model.getRelation(hasMethodP);
		IRelation methodNameRel = model.getRelation(methodNameP);

		ITerm constructorNameFull = TERM.createString(this.name + ".<init>");
		// hasConstructor(type, "class.<init>")
		hasConstructorRel.add(BASIC.createTuple(TERM.createString(this.name), constructorNameFull));
		// methodName("class.<init>", "class")
		methodNameRel.add(BASIC.createTuple(constructorNameFull, TERM.createString(this.name)));

		List<PMethod> methods = body.getMethod();
		for (PMethod m : methods) {
			AMethod method = (AMethod) m;

			PPattern methodPattern = method.getName();
			String methodName;
			if (methodPattern instanceof ANamedPattern) {
				methodName = ((ANamedPattern) methodPattern).getName().getText();
			} else if (methodPattern instanceof AAnyPattern) {
				methodName = "*";
			} else if (methodPattern instanceof ADollarPattern) {
				methodName = "$" + ((ADollarPattern) methodPattern).getName().getText();
			} else {
				throw new RuntimeException("Unknown pattern: " + methodPattern);
			}

			ITerm methodNameFull;

			if (method.getType() == null) {
				if (!methodName.equals(this.name)) {
					throw new RuntimeException("Constructor must be named after class (or missing return type): " + methodName + " in " + this.name);
				}
				methodNameFull = constructorNameFull;
			} else {
				methodNameFull = TERM.createString(this.name + "." + methodName);

				// hasMethod(type, "class.method")
				hasMethodRel.add(BASIC.createTuple(TERM.createString(this.name), methodNameFull));

				// methodName("class.method", "method")
				ITerm methodNameTerm;
				if (methodPattern instanceof ANamedPattern) {
					methodNameTerm = TERM.createString(methodName);
				} else {
					methodNameTerm = new AnyTerm(Type.StringT);
				}
				methodNameRel.add(BASIC.createTuple(methodNameFull, methodNameTerm));
			}

			SAMMethod sm = new SAMMethod(this, method, methodNameFull);
			sm.addDatalog();
		}
	}
}
