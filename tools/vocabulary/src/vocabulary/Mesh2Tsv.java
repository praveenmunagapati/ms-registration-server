package vocabulary;

import org.xml.sax.*;
import org.xml.sax.helpers.XMLReaderFactory;

import java.util.Collection;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.*;


public class Mesh2Tsv
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
		String tree;	// used to create parent
		String synonyms;
		String description;
		String umls;
		String semanticType;


		public static String columns()
    		{
			return "ID" + "\t" + "UMLS" + "\t" + "Name" + "\t" + "Label" + "\t" + "Parent" + "\t" + "SemanticType" + "\t" + "Synonyms" + "\t" + "Description";
    		}

		private static String valueOf(String s)
    		{
			return null == s ? "" : s;
    		}

    	String getPropertyName()
			{
			String s = valueOf(name).replaceAll("[, ]", "_");
            s = s.replaceAll("__", "_");
			return s;
			}

		public String toString()
    		{
			return valueOf(id) +  "\t" + valueOf(umls) + "\t" + getPropertyName() + "\t" + valueOf(name) + "\t" + valueOf(parent) + "\t" + valueOf(semanticType) + "\t" + valueOf(synonyms) + "\t" + valueOf(description);
    		}
    	}


	public static Collection<Concept> loadMeshConcepts(InputStream in, Collection<String> errors)
    	{
		ArrayList<Concept> concepts = new ArrayList<Concept>();

		try
    		{
			XMLReader parser = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
			parserSetFeature(parser, "http://xml.org/sax/features/namespaces", false);
			parserSetFeature(parser, "http://xml.org/sax/features/namespace-prefixes", false);
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

			// fix-up parent
			HashMap<String,Concept> mapTreeConcept = new HashMap<String,Concept>(concepts.size() * 2);
			for (Concept concept : concepts)
				mapTreeConcept.put(concept.tree, concept);

			for (Concept concept : concepts)
    			{
				String tree = concept.tree;
				if (null == tree)
					continue;
				int period = tree.lastIndexOf('.');
				if (-1 == period)
					continue;
				String parentTree = tree.substring(0,period);
				Concept parent = mapTreeConcept.get(parentTree);
				if (null == parent)
					continue;
				concept.parent = parent.getPropertyName();
    			}
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
			switch (qName.charAt(0))
    			{
				case 'D':
					if (qName.equals("DescriptorRecord"))
    					{
						_concept = new Concept();
    					}
					break;

				case 'C':
					if (qName.equals("Concept"))
    					{
						int index = attributes.getIndex("PreferredConceptYN");
						if (index >= 0 && "Y".equals(attributes.getValue(index)))
							_preferredConcept = true;
						else
							_preferredConcept = false;
    					}
					break;
    			}
    		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException
    		{
			if (null == _concept)
				return;

			switch (qName.charAt(0))
    			{
				case 'D':
					if (qName.equals("DescriptorRecord"))
    					{
						_concepts.add(_concept);
						_concept = null;
    					}
					else if (qName.equals("DescriptorName"))
    					{
						if (null == _concept.name)
							_concept.name = _string.toString();
    					}
					break;
				case 'S':
					if (qName.equals("String"))
    					{
						_string = _text.toString();
    					}
					else if (_preferredConcept && qName.equals("ScopeNote"))
    					{
						_concept.description = _text.toString().trim();
    					}
					else if (_preferredConcept && qName.equals("SemanticTypeName"))
    					{
						if (null == _concept.semanticType)
							_concept.semanticType = "";
						else
							_concept.semanticType += '|';
						_concept.semanticType += _text.toString();
    					}
					break;
				case 'C':
					if (qName.equals("Concept"))
    					{
						_preferredConcept = false;
    					}
					else if (_preferredConcept && qName.equals("ConceptUMLSUI"))
    					{
						_concept.umls = _text.toString();
    					}
					else if (_preferredConcept && qName.equals("ConceptUI"))
    					{
						_concept.id = _text.toString();
    					}
					break;
				case 'T':
					if (qName.equals("TreeNumber"))
    					{
						_concept.tree = _text.toString();
    					}
					break;
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
