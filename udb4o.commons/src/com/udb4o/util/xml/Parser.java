package com.udb4o.util.xml;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Stack;

import org.xmlpull.mxp1.MXParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class Parser {

	public Tag processDocument(String filename, String charset) throws IOException {
		return processDocument(new FileInputStream(filename), charset);
	}
	
	public Tag processDocument(InputStream in, String charset) throws IOException {
		try {
			MXParser parser = new MXParser();
			parser.setInput(new InputStreamReader(in,charset));
			return new Parser().processDocument(parser);
		} catch (XmlPullParserException e) {
			throw new RuntimeException(e);
		}
	}

	public Tag processDocument(StringReader reader) throws IOException {
		try {
			MXParser parser = new MXParser();
			parser.setInput(reader);
			return new Parser().processDocument(parser);
		} catch (XmlPullParserException e) {
			throw new RuntimeException(e);
		}
	}

	
	private Tag processDocument(XmlPullParser xpp)
			throws XmlPullParserException, IOException {

		Tag root = new Tag();
		Stack<Tag> stack = new Stack<Tag>();

		stack.push(root);

		boolean ended = false;
		while (!ended) {

			int eventType = xpp.next();

			switch (eventType) {

			case XmlPullParser.START_DOCUMENT:
				break;

			case XmlPullParser.END_DOCUMENT:
				ended = true;
				break;

			case XmlPullParser.START_TAG:
				Tag tag = processStartElement(stack.lastElement(), xpp);
				stack.add(tag);
				break;

			case XmlPullParser.END_TAG:
				stack.pop();
				break;

			case XmlPullParser.TEXT:
				processText(stack.lastElement(), xpp);
				break;
			}
		}
		return root.getChildren().get(0);
	}

	private Tag processStartElement(Tag parent, XmlPullParser xpp) {

		Tag tag = parent.enterTag(Tag.decodeTagName(xpp.getName()));

		for (int i = 0; i < xpp.getAttributeCount(); i++) {
			String field = xpp.getAttributeName(i);
			String value = xpp.getAttributeValue(i);
			tag.setAttribute(field, value);
		}

		return tag;
	}

	int holderForStartAndLength[] = new int[2];

	private void processText(Tag tag, XmlPullParser xpp)
			{
		char ch[] = xpp.getTextCharacters(holderForStartAndLength);
		int start = holderForStartAndLength[0];
		int length = holderForStartAndLength[1];
		tag.setContent(new String(ch, start, length));
	}


}
