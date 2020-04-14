package vocabulary;

import org.xml.sax.*;
import org.xml.sax.helpers.XMLReaderFactory;

import java.util.Collection;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.*;


public class Owl2Tsv
    {
	private static void logError(String s, Exception e)
    	{
		System.err.println(s);
		e.printStackTrace(System.err);
    	}

	private static void parserSetFeature(XMLReader parser, String feature, boolean b)
    	{
		try
    		{
			parser.setFeature(feature, b);
    		}
		catch (SAXNotSupportedException e)
    		{
			logError("parserSetFeature", e);
    		}
		catch (SAXNotRecognizedException e)
    		{
			logError("parserSetFeature", e);
    		}
    	}


	static class Concept
    	{
		String id;
		String name;
		String parent;
		String label;
		String tree;	// used to create parent
		String synonyms;
		String description;
		String umls;
		String semanticType;


		public static String columns()
    		{
			return "ID" + "\t" + "UMLS" + "\t" + "Name" + "\t" + "label" + "\t" + "Parent" + "\t" + "SemanticType" + "\t" + "Synonyms" + "\t" + "Description";
    		}

		private static String valueOf(String s)
    		{
			return null == s ? "" : s;
    		}

		public String toString()
    		{
			return valueOf(id) +  "\t" + valueOf(umls) + "\t" + valueOf(name) + "\t" + valueOf(label) + "\t" + valueOf(parent) + "\t" + valueOf(semanticType) + "\t" + valueOf(synonyms) + "\t" + valueOf(description);
    		}
    	}


	public static Collection<Concept> loadMeshConcepts(InputStream in, Collection<String> errors)
    	{
		ArrayList<Concept> concepts = new ArrayList<Concept>();

		try
    		{
			XMLReader parser = XMLReaderFactory.createXMLReader();
//			parserSetFeature(parser, "http://xml.org/sax/features/namespaces", false);
//			parserSetFeature(parser, "http://xml.org/sax/features/namespace-prefixes", false);
			parserSetFeature(parser, "http://xml.org/sax/features/validation", false);
			parserSetFeature(parser, "http://apache.org/xml/features/validation/dynamic", false);
			parserSetFeature(parser, "http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
			parserSetFeature(parser, "http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			parserSetFeature(parser, "http://apache.org/xml/features/validation/schema", false);
			parserSetFeature(parser, "http://apache.org/xml/features/validation/schema-full-checking", false);
			parserSetFeature(parser, "http://apache.org/xml/features/validation/dynamic", false);
			parserSetFeature(parser, "http://apache.org/xml/features/continue-after-fatal-error", false);

			MeshHandler handler = new MeshHandler(concepts, errors);
			parser.setContentHandler(handler);
			parser.parse(new InputSource(in));
    		}
		catch (UnsupportedEncodingException e)
    		{
			logError(e.getMessage(), e);
			errors.add(e.getMessage());
    		}
		catch (IOException e)
    		{
			logError(e.getMessage(), e);
			errors.add(e.getMessage());
    		}
		catch (SAXException e)
    		{
			logError(e.getMessage(), e);
			errors.add(e.getMessage());
    		}
		return concepts;
    	}


	private static class MeshHandler extends org.xml.sax.helpers.DefaultHandler
    	{
		// return
		Collection<Concept> _concepts;
		Collection<String> _errors;

		// state
		Concept _concept;
		StringBuilder _text = new StringBuilder();
		String _string;
		boolean _preferredConcept = false;


		MeshHandler(Collection<Concept> concepts, Collection<String> errors)
    		{
			_concepts = concepts;
			_errors = errors;
    		}


		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    		{
			_text.setLength(0);

			switch (localName.charAt(0))
    			{
				case 'C':
					if (localName.equals("Class"))
    					{
						_concept = new Concept();

						int index = attributes.getIndex("rdf:ID");
						if (-1 == index)
							break;
						String name = attributes.getValue(index);
						_concept.name = name;
    					}
					break;
				case 's':
					if (localName.equals("subClassOf"))
    					{
						int index = attributes.getIndex("rdf:resource");
						if (-1 == index)
							break;
						String parent = attributes.getValue(index);
						if (parent.startsWith("#"))
							parent = parent.substring(1);
						_concept.parent = parent;
						break;
    					}
					break;
    			}
    		}


		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException
    		{
			if (null == _concept)
				return;

			switch (localName.charAt(0))
    			{
				case 'C':
					if (localName.equals("Class"))
    					{
						_concepts.add(_concept);
						_concept = null;
    					}
					break;
				case 'c':
					if (localName.equals("code"))
						_concept.id = _text.toString();
					break;
				case 'S':
					if (localName.equals("Synonym"))
    					{
						if (_concept.synonyms == null)
							_concept.synonyms = "";
						else
							_concept.synonyms += "|";
						_concept.synonyms += _text.toString().trim();
					   	break;
    					}
					else if (localName.equals("Semantic_Type"))
    					{
						if (_concept.semanticType == null)
							_concept.semanticType = "";
						else
							_concept.semanticType += "|";
						_concept.semanticType += _text.toString().trim();
    					}

					break;
				case 'P':
					if (localName.equals("Preferred_Name"))
    					{
						_concept.label = _text.toString();
						break;
    					}
					break;
				case 'U':
					if (localName.equals("UMLS_CUI"))
    					{
						_concept.umls = _text.toString();
    					}
					break;
				case 'd':
                    // <dDEFINITION><![CDATA[<def-source>NCI</def-source><def-definition>The length of a person's life, stated in years since birth.</def-definition>]]></dDEFINITION>
				    if (localName.equals("dDEFINITION"))
                        {
                        String cdata = _text.toString();
                        int start = cdata.indexOf("<def-definition>");
                        if (-1 == start)
                            break;
                        start += "<def-definition>".length();
                        int end = cdata.indexOf("</def-definition>");
                        if (-1 == end)
                            break;
                        String definition = cdata.substring(start, end);
                        definition = definition.replaceAll("\\s", " ");
                        _concept.description = definition;
                        }
    			}
    		}

		public void characters(char ch[], int start, int length) throws SAXException
    		{
			_text.append(ch, start, length);
    		}

		@Override public void warning(SAXParseException e) throws SAXException
    		{
    		}

		@Override public void error(SAXParseException e) throws SAXException
    		{
			_errors.add(e.getMessage());
    		}

		@Override public void fatalError(SAXParseException e) throws SAXException
    		{
			_errors.add(e.getMessage());
    		}
    	}


	public static void main(String[] args) throws Exception
    	{
		File f = new File(args[0]);
		Collection<Concept> concepts = loadMeshConcepts(new FileInputStream(f), new ArrayList<String>());
		System.out.println(Concept.columns());
		for (Concept concept : concepts)
    		{
    		if (concept.name == null)
    		    continue;
			System.out.println(concept.toString());
    		}
    	}
    }
