package eu.serscis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.io.FileReader;
import java.io.IOException;
import org.deri.iris.Configuration;
import org.deri.iris.KnowledgeBaseFactory;
import org.deri.iris.api.IKnowledgeBase;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.compiler.Parser;
import org.deri.iris.storage.IRelation;

// Based on IRIS Demo code

public class Eval {
	private static final String loadFile( String filename ) throws IOException {
		FileReader r = new FileReader( filename );

		StringBuilder builder = new StringBuilder();

		int ch = -1;
		while( ( ch = r.read() ) >= 0 )
		{
			builder.append( (char) ch );
		}
		return builder.toString();
	}

	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			throw new Exception("usage: Eval INPUTFILE.dl");
		}
		String filename = args[0];

		Configuration configuration = KnowledgeBaseFactory.getDefaultConfiguration();
		String program = loadFile( filename );

		Parser parser = new Parser();
		parser.parse( program );

		Map<IPredicate,IRelation> facts = parser.getFacts();
		List<IRule> rules = parser.getRules();
		IKnowledgeBase knowledgeBase = KnowledgeBaseFactory.createKnowledgeBase( facts, rules, configuration );


		List<IVariable> variableBindings = new ArrayList<IVariable>();

		for( IQuery query : parser.getQueries() )
		{
			// Execute the query
			IRelation results = knowledgeBase.execute( query, variableBindings );

			System.out.println("\n" +  query );

			if( results.size() == 0 ) {
				System.out.println( "no results" );
			} else {
				boolean first = true;
				for( IVariable variable : variableBindings )
				{
					if( first )
						first = false;
					else
						System.out.print( ", " );
					System.out.print( variable );
				}
				System.out.println( );

				formatResults( results );
			}
		}
	}

	static private void formatResults(IRelation m )
	{
		for(int t = 0; t < m.size(); ++t )
		{
			ITuple tuple = m.get( t );
			System.out.println( tuple.toString() );
		}
	}

}
