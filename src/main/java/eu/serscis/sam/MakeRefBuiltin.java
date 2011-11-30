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
//	Created Date :			2011-12-01
//	Created for Project :		SERSCIS
//
/////////////////////////////////////////////////////////////////////////
//
//  License : GNU Lesser General Public License, version 2.1
//
/////////////////////////////////////////////////////////////////////////

// Based on IRIS's StringConcatBuiltin

package eu.serscis.sam;

import static org.deri.iris.factory.Factory.BASIC;

import org.deri.iris.EvaluationException;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.terms.IStringTerm;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.builtins.FunctionalBuiltin;
import org.deri.iris.builtins.datatype.ToStringBuiltin;
import org.deri.iris.factory.Factory;

public class MakeRefBuiltin extends FunctionalBuiltin {

	private static final String PREDICATE_STRING = "MAKE_OBJECT";
	/** The predicate defining this built-in. */
	private static final IPredicate PREDICATE = BASIC.createPredicate(PREDICATE_STRING, -1);

	public MakeRefBuiltin(ITerm... terms) {
		super(BASIC.createPredicate(PREDICATE_STRING, terms.length), terms);
		
		if (terms.length < 3) {
			throw new IllegalArgumentException("The amount of terms <" + terms.length + "> must at least 3");
		}
	}

	protected ITerm computeResult(ITerm[] terms) throws EvaluationException {
		
		StringBuilder buffer = new StringBuilder();
		
		int endIndex = terms.length - 1;
		for (int i = 0; i < endIndex; i++) {
			IStringTerm string = ToStringBuiltin.toString(terms[i]);
			
			if (string == null)
				return null;
			
			buffer.append(string.getValue());
		}
		
		return new RefTerm(buffer.toString());
	}

}
