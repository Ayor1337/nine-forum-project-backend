package com.ayor.mapper;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.StringReader;

import static org.assertj.core.api.Assertions.assertThat;
import org.xml.sax.InputSource;

class PermissionMapperXmlTest {

    @Test
    void getUserPermissionVoShouldMapPermissionsAsCollection() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        var builder = factory.newDocumentBuilder();
        builder.setEntityResolver((publicId, systemId) -> new InputSource(new StringReader("")));
        Document document = builder.parse(new File("src/main/resources/mapper/PermissionMapper.xml"));

        Element resultMap = findElementById(document, "resultMap", "UserPermissionVOMap");
        Element collection = findCollectionByProperty(resultMap, "permissions");
        Element select = findElementById(document, "select", "getUserPermissionVO");

        assertThat(collection.getAttribute("ofType")).isEqualTo("java.lang.String");
        assertThat(collection.getAttribute("column")).isEqualTo("account_id");
        assertThat(collection.getAttribute("select")).isEqualTo("getPermissionsByAccountId");
        assertThat(select.getAttribute("resultMap")).isEqualTo("UserPermissionVOMap");
    }

    private Element findElementById(Document document, String tagName, String id) {
        NodeList nodes = document.getElementsByTagName(tagName);
        for (int i = 0; i < nodes.getLength(); i++) {
            Element element = (Element) nodes.item(i);
            if (id.equals(element.getAttribute("id"))) {
                return element;
            }
        }
        throw new AssertionError("Missing " + tagName + " with id " + id);
    }

    private Element findCollectionByProperty(Element resultMap, String property) {
        NodeList nodes = resultMap.getElementsByTagName("collection");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element element = (Element) nodes.item(i);
            if (property.equals(element.getAttribute("property"))) {
                return element;
            }
        }
        throw new AssertionError("Missing collection with property " + property);
    }
}
