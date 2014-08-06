package com.udb4o.util.xml;

import gnu.trove.map.hash.*;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

public class Tag implements Cloneable {
	private final static String EMPTY_STRING = "";

	private String name = "";
	private String content;
	private Map<String, String> attributes = null;
	private List<Tag> children = null;
	private Object userData;

	private boolean translated = false;
	
	public void setTranslated(){
		translated = true;
	}
	
	public boolean isTranslated(){
		return translated;
	}
	
	public void accept(TagVisitor visitor) {
		visitor.visit(this);
		if (children != null && children.size() > 0) {
			for (Tag c : new ArrayList<Tag>(children)) {
				c.accept(visitor);
			}
		}
	}

	public Tag() {
		// Empty contructor
	}

	public Tag(String name) {
		this.name = name;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return cloneTag();
	}

	public Tag cloneTag() {
		Tag clone = new Tag(name);
		clone.content = content;
		if (attributes != null)
			clone.attributes().putAll(attributes);

		if (children != null) {
			List<Tag> cc = clone.children();
			for (Tag tag : children) {
				cc.add(tag.cloneTag());
			}
		}
		clone.userData = userData;
		return clone;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Tag> getChildren() {
		return children();
	}

	public String getContent() {
		return content;
	}

	public Tag setContent(String content) {
		if (content != null && content.trim().length() == 0) {
			this.content = EMPTY_STRING;
		} else {
			this.content = content;
		}
		return this;
	}

	public Tag enterTag(String name) {
		Tag tag = new Tag(name);
		children().add(tag);
		return tag;
	}

	public Tag addChild(Tag tag) {
		children().add(tag);
		return tag;
	}

	public Tag setAttribute(String key, String value) {
		attributes().put(key, value);
		return this;
	}

	public String render() {
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		PrintWriter out = new PrintWriter(buf);
		render(out);
		out.flush();
		return new String(buf.toByteArray());
	}

	public void render(PrintWriter out) {

		render("", out); //$NON-NLS-1$
	}

	public static String encodeTagName(String s) {
		StringBuilder buf = new StringBuilder();
		int len = (s == null ? -1 : s.length());

		for (int i = 0; i < len; i++) {
			char c = s.charAt(i);
			if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' &&
					c <= '9') {
				buf.append(c);
			} else {
				buf.append(String.format("_%02x", (int) c));
			}
		}
		return buf.toString();
	}

	public static String decodeTagName(String s) {
		StringBuilder buf = new StringBuilder();
		int len = (s == null ? -1 : s.length());

		for (int i = 0; i < len; i++) {
			char c = s.charAt(i);
			if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' &&
					c <= '9') {
				buf.append(c);
			} else if (c == '_') {
				buf.append((char) Integer.parseInt(s.substring(i + 1, i + 3),
						16));
				i += 2;
			} else {
				System.out.println("WARNING: character '" + c +
						"' illegal for html, ignoring it");
			}
		}
		return buf.toString();
	}

	private void render(String ident, PrintWriter out) {
		out.append(ident);
		String encodedName;
		if (name != null) {
			encodedName = encodeTagName(name);
			out.append("<").append(encodedName); //$NON-NLS-1$
			Set<Entry<String, String>> e = attributes().entrySet();
			for (Entry<String, String> entry : e) {
				out.append("\n" + ident + "   ").append(entry.getKey()).append("=").append('"') //$NON-NLS-1$
				.append(entry.getValue()).append('"');
			}
		} else {
			encodedName = null;
		}
		if (children == null || children().isEmpty()) {
			if (content == null || content.trim().length() == 0) {
				out.append(" />"); //$NON-NLS-1$
				return;
			}
			out.append(">").append(content); //$NON-NLS-1$
		} else {
			out.append(">").append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
			for (Tag tag : children()) {
				tag.render(ident + "   ", out); //$NON-NLS-1$
				out.append("\n"); //$NON-NLS-1$
			}
			if (content != null) {
				out.append(ident).append("   ").append(content).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			out.append(ident);
		}
		if (name != null) {
			out.append("</").append(encodedName).append(">"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	public Tag getDirectChildBy(String field, String value) {
		return getDirectChildBy(null, field, value);
	}

	public Tag getDirectChildByName(String tagName) {
		return getChildBy(0, tagName, null, null);
	}

	public Tag getDirectChildBy(String tagName, String field, String value) {
		return getChildBy(0, tagName, field, value);
	}

	public Tag getChildBy(String field, String value) {
		return getChildBy(null, field, value);
	}

	public Tag getChildBy(String tagName) {
		return getChildBy(Integer.MAX_VALUE, tagName, null, null);
	}

	public Tag getChildBy(String tagName, String field, String value) {
		Tag child = getChildBy(Integer.MAX_VALUE, tagName, field, value);
//		if (child == null)
//			logger.error("element " + tagName + " with " + field +
//					" of value " + value + " not found!");

		return child;
	}

    public Tag getChildBy(int maxRecursion, String tagName, String field,
            String value) {
        return getChildBy(maxRecursion, tagName, field, value, null);
    }
    
    public Tag getChildBy(String tagName, String field, String value, String content) {
        return getChildBy(Integer.MAX_VALUE, tagName, field, value, content);
    }
    
    public Tag getChildBy(int maxRecursion, String tagName, String field,
                String value, String content) {
		if (children == null) {
			return null;
		}
		for (Tag tag : children()) {
			boolean byName = tagName == null || tagName.equals(tag.getName());
			boolean byField = field == null || value.equals(tag.getAttribute(field));
			boolean byContent = content == null || content.equals(tag.getContent());
			if (byName && byField && byContent) {
				return tag;
			}
			if (maxRecursion > 0) {
				Tag child = tag.getChildBy(maxRecursion - 1, tagName, field,
						value);
				if (child != null) {
					return child;
				}
			}
		}
		return null;
	}

	public List<Tag> getChildrenBy(String field, String value) {
		return getChildrenBy(null, field, value);
	}

	public List<Tag> getDirectChildrenBy(String field, String value) {
		return getChildrenBy(0, null, field, value);
	}

	public List<Tag> getChildrenBy(String tagName, String field, String value) {
		return getChildrenBy(Integer.MAX_VALUE, tagName, field, value);
	}

	public List<Tag> getDirectChildrenByName(String tagName) {
		return getChildrenBy(0, tagName, null, null);
	}

	public List<Tag> getChildrenByName(int maxRecursion, String tagName) {
		return getChildrenBy(maxRecursion, tagName, null, null);
	}

	public List<Tag> getChildrenBy(int maxRecursion, String tagName,
			String field, String value) {
		List<Tag> ret = new LinkedList<Tag>();
		getChildrenBy(maxRecursion, ret, tagName, field, value);
		return ret;
	}

	public void getChildrenBy(int maxRecursion, List<Tag> toList,
			String tagName, String field, String value) {
		if (children == null) {
			return;
		}
		for (Tag tag : children()) {
			boolean byName = tagName == null || tagName.equals(tag.getName());
			boolean byField = field == null ||
					value.equals(tag.getAttribute(field));
			if (byName && byField) {
				toList.add(tag);
			}
			if (maxRecursion > 0) {
				tag.getChildrenBy(maxRecursion - 1, toList, tagName, field,
						value);
			}
		}
	}

	public String getAttribute(String field) {
		return getAttribute(field, null);
	}

	public String getAttribute(String field, String _default) {
		String value = attributes().get(field);
		return value == null ? _default : value;
	}

	@Override
	public String toString() {
		StringBuilder out = new StringBuilder();
		out.append("<").append(name); //$NON-NLS-1$
		Set<Entry<String, String>> e = attributes().entrySet();
		for (Entry<String, String> entry : e) {
			out.append(' ').append(entry.getKey()).append("=").append('"')//$NON-NLS-1$
			.append(entry.getValue()).append('"');
		}
		if (children == null || children().isEmpty()) {

			if (content == null) {
				out.append(" />");//$NON-NLS-1$
				return out.toString();
			}
			out.append(">...");//$NON-NLS-1$
		} else {
			out.append(">").append("...");//$NON-NLS-1$ //$NON-NLS-2$
		}
		out.append("</").append(name).append(">");//$NON-NLS-1$ //$NON-NLS-2$
		return out.toString();
	}

	public String getXmlContents() {
		if (children == null || children().isEmpty()) {
			return "";
		}
		StringWriter out = new StringWriter();
		PrintWriter pw = new PrintWriter(out);
		for (Tag t : children) {
			t.render(pw);
		}
		pw.flush();

		return out.toString();
	}

	public void setUserData(Object userData) {
		this.userData = userData;
	}

	public Object getUserData() {
		return userData;
	}

	public void removeDirectChild(String tagName) {
		if (children == null) {
			return;
		}
		for (Iterator<Tag> it = children().iterator(); it.hasNext();) {
			Tag tag = it.next();
			if (tagName.equals(tag.getName())) {
				it.remove();
				return;
			}

		}
	}

	public int count() {
		if (children == null) {
			return 0;
		}
		int count = children().size();
		for (Tag child : children()) {
			count += child.count();
		}
		return count;
	}

	private Map<String, String> attributes() {
		if (attributes == null) {
			attributes = new THashMap<String, String>();
		}
		return attributes;
	}

	@SuppressWarnings("unchecked")
	public Map<String, String> getAttributes() {
		if (attributes != null) {
			return Collections.unmodifiableMap(attributes);
		}
		return Collections.EMPTY_MAP;
	}

	private List<Tag> children() {
		if (children == null) {
			children = new ArrayList<Tag>();
		}
		return children;
	}

	public void clear() {
		getChildren().clear();
	}

}