/**
 * 
 */
package se.liu.imt.mi.snomedct.expression.tools;

import java.util.Map;
import java.util.Map.Entry;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionLexer;
import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser;
import se.liu.imt.mi.snomedct.parser.OWLVisitor;

/**
 * Wrapper class for the SNOMED CT ANTLR parser and OWL converter.
 * 
 * @author Daniel Karlsson, daniel.karlsson@liu.se
 *
 */

public class SNOMEDCTParserUtil {
	static final String PC_IRI = "http://snomed.info/expid/";

	/**
	 * Wrapper method for parsing an SNOMED CT expression from a string
	 * 
	 * @param expression
	 *            string representation of a SNOMED CT expression
	 * @return parse tree resulting from parsing
	 * @throws ExpressionSyntaxError
	 *             thrown when syntax error in expression string
	 */
	public static ParseTree parseExpression(String expression)
			throws ExpressionSyntaxError {

		ParseTree tree = null;

		// parse string and throw ExpressionSyntaxError if unparsable
		ANTLRInputStream input = new ANTLRInputStream(expression);
		SNOMEDCTExpressionLexer lexer = new SNOMEDCTExpressionLexer(input);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		SNOMEDCTExpressionParser parser = new SNOMEDCTExpressionParser(tokens);
		parser.setErrorHandler(new BailErrorStrategy());
		try {
			tree = parser.expression();
		} catch (Exception e) {
			throw new ExpressionSyntaxError(e);
		}
		if (tree == null)
			throw new ExpressionSyntaxError(
					"Parse result is null. Should not happen ever!");

		return tree;
	}
	
	/**
	 * Wrapper method for parsing an SNOMED CT statement from a string
	 * 
	 * @param statement
	 *            string representation of a SNOMED CT statement
	 * @return parse tree resulting from parsing
	 * @throws ExpressionSyntaxError
	 *             thrown when syntax error in statement string
	 */
	public static ParseTree parseStatement(String statement)
			throws ExpressionSyntaxError {

		ParseTree tree = null;

		// parse string and throw ExpressionSyntaxError if unparsable
		ANTLRInputStream input = new ANTLRInputStream(statement);
		SNOMEDCTExpressionLexer lexer = new SNOMEDCTExpressionLexer(input);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		SNOMEDCTExpressionParser parser = new SNOMEDCTExpressionParser(tokens);
		parser.setErrorHandler(new BailErrorStrategy());
		try {
			tree = parser.statement();
		} catch (Exception e) {
			throw new ExpressionSyntaxError(e);
		}
		if (tree == null)
			throw new ExpressionSyntaxError(
					"Parse result is null. Should not happen ever!");

		return tree;
	}

	/**
	 * Parses expression, converts to an OWLAxiom and adds it to ontology,
	 * including any descriptions as rdfs:label annotations
	 * 
	 * @param expression
	 *            the expression to parse
	 * @param ontology
	 *            the ontology to which the parsed, converted expression is
	 *            added
	 *            
	 * @return the resulting OWLAxiom
	 * @throws ExpressionSyntaxError
	 */
	public static OWLAxiom parseExpressionToOWLAxiom(String expression,
			OWLOntology ontology) throws ExpressionSyntaxError {
		return parseExpressionToOWLAxiom(expression, ontology, (OWLClass) null, false);
	}

	/**
	 * Parses expression, converts to an OWLAxiom and adds it to ontology,
	 * including any descriptions as rdfs:label annotations
	 * 
	 * @param expression
	 *            the expression to parse
	 * @param ontology
	 *            the ontology to which the parsed, converted expression is
	 *            added
	 * @param subj
	 *            a string, if not null, a new class is created which is the
	 *            definiendum of the new axiom
	 * @param defaultToPrimitive
	 *            make subclassOf axiom if no definitions status in expression
	 * 
	 * @return the resulting OWLAxiom
	 * @throws ExpressionSyntaxError
	 */
	public static OWLAxiom parseExpressionToOWLAxiom(String expression,
			OWLOntology ontology, String subj, boolean defaultToPrimitive)
			throws ExpressionSyntaxError {
		final OWLOntologyManager manager = ontology.getOWLOntologyManager();

		if (subj == null)
			return parseExpressionToOWLAxiom(expression, ontology,
					(OWLClass) null, defaultToPrimitive);

		OWLClass newClass = manager.getOWLDataFactory().getOWLClass(
				IRI.create(PC_IRI + subj));

		return parseExpressionToOWLAxiom(expression, ontology,
				newClass, defaultToPrimitive);
	}
	
	/**
	 * Parses expression, converts to an OWLAxiom and adds it to ontology,
	 * including any descriptions as rdfs:label annotations
	 * 
	 * @param expression
	 *            the expression to parse
	 * @param ontology
	 *            the ontology to which the parsed, converted expression is
	 *            added
	 * @param definiendum
	 *            an OWLClass which will be the definiendum of the new axiom
	 * 
	 * @return the resulting OWLAxiom
	 * @throws ExpressionSyntaxError
	 */
	public static OWLAxiom parseExpressionToOWLAxiom(String expression,
			OWLOntology ontology, OWLClass definiendum) throws ExpressionSyntaxError {
		return parseExpressionToOWLAxiom(expression, ontology, definiendum, false);
	}

	/**
	 * Parses expression, converts to an OWLAxiom and adds it to ontology,
	 * including any descriptions as rdfs:label annotations
	 * 
	 * @param expression
	 *            the expression to parse
	 * @param ontology
	 *            the ontology to which the parsed, converted expression is
	 *            added
	 * @param definiendum
	 *            an OWLClass which will be the definiendum of the new axiom
	 * @param defaultToPrimitive
	 *            make subclassOf axiom if no definitions status in expression
	 *            
	 * @return the resulting OWLAxiom
	 * @throws ExpressionSyntaxError
	 */
	public static OWLAxiom parseExpressionToOWLAxiom(String expression,
			OWLOntology ontology, OWLClass definiendum,
			boolean defaultToPrimitive) throws ExpressionSyntaxError {
		OWLAxiom owlAxiom = null;

		final OWLOntologyManager manager = ontology.getOWLOntologyManager();
		final OWLDataFactory dataFactory = manager.getOWLDataFactory();

		ParseTree tree = null;
		if(expression.startsWith("("))
			tree = parseStatement(expression);
		else
			tree = parseExpression(expression);

		OWLVisitor visitor = new OWLVisitor(manager, definiendum);
		// convert from parse tree to OWLAxiom
		owlAxiom = (OWLClassAxiom) visitor.visit(tree);

		// add axiom to ontology
		manager.addAxiom(ontology, owlAxiom);

		// labels for expression parts are kept in a map
		Map<IRI, OWLAnnotation> annotations = visitor.getLabels();
		// add labels, if any
		for (Entry<IRI, OWLAnnotation> label : annotations.entrySet()) {
			if (dataFactory.getOWLClass(label.getKey())
					.getAnnotations(ontology).isEmpty())
				manager.addAxiom(
						ontology,
						dataFactory.getOWLAnnotationAssertionAxiom(
								label.getKey(), label.getValue()));
		}

		return owlAxiom;
	}

}
