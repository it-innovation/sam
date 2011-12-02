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

import java.net.URI;

import org.deri.iris.api.terms.IConcreteTerm;
import org.deri.iris.api.terms.ITerm;

public class AnyTerm implements ITerm {
	public static final AnyTerm THE_ONE = new AnyTerm();
	private AnyTerm() {
	}

	public String getValue() {
		return "_";
	}

	public boolean isGround() {
		return true;
	}

	public int compareTo(ITerm o) {
		return o == THE_ONE ? 0 : 1;
	}

	public int hashCode() {
		return "_".hashCode();
	}

	public boolean equals(final Object o) {
		return o == THE_ONE;
	}

	public String toString() {
		return "_";
	}
}
