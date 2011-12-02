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
//	Created Date :			2011-12-02
//	Created for Project :		SERSCIS
//
/////////////////////////////////////////////////////////////////////////
//
//  License : GNU Lesser General Public License, version 2.1
//
/////////////////////////////////////////////////////////////////////////

package eu.serscis.sam;

import static org.deri.iris.factory.Factory.BASIC;

import org.deri.iris.EvaluationException;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.terms.IStringTerm;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.builtins.FunctionalBuiltin;
import org.deri.iris.builtins.datatype.ToStringBuiltin;
import org.deri.iris.factory.Factory;

public class MatchBuiltin extends FunctionalBuiltin {

	private static final String PREDICATE_STRING = "MATCH";
	private static final IPredicate PREDICATE = BASIC.createPredicate(PREDICATE_STRING, -1);

	public MatchBuiltin(ITerm... terms) {
		super(BASIC.createPredicate(PREDICATE_STRING, terms.length), terms);
		
		if (terms.length < 2 || terms.length > 3) {
			throw new IllegalArgumentException("The amount of terms <" + terms.length + "> must be 2 or 3");
		}
	}

	protected ITerm computeResult(ITerm[] terms) throws EvaluationException {
		ITerm first = terms[0];
		ITerm second = terms[1];

		ITerm result;

		if (first instanceof AnyTerm) {
			result = second;
		} else if (second instanceof AnyTerm) {
			result = first;
		} else {
			if (first.equals(second)) {
				result = first;
			} else {
				result = null;
			}
		}

		return result;
	}

}
