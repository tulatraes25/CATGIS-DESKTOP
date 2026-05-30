/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.iver.utiles.XMLEntity
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.io.WKTReader
 *  org.apache.log4j.Logger
 */
package org.saig.core.model.bookmark;

import com.iver.andami.ConfigurationException;
import com.iver.utiles.XMLEntity;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;
import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.log4j.Logger;
import org.saig.core.model.bookmark.BasicBookmark;
import org.saig.core.model.bookmark.BookmarkCategory;
import org.saig.core.model.bookmark.IBookmark;
import org.saig.core.model.bookmark.TemporalBookmark;
import org.saig.core.util.XMLUtils;
import org.saig.jump.lang.I18N;

public class BookmarkXMLPersistence {
    private static final Logger LOGGER = Logger.getLogger(BookmarkXMLPersistence.class);
    private static final String XML_BOOKMARK = "bookmarks.xml";
    private XMLEntity xml = null;
    private XMLEntity categoriesXML = null;
    private static final String BOOKMARK_CATEGORIES = "categories";
    private static final String BOOKMARK_CATEGORY_NAME = "name";
    private static final String BOOKMARK_NAME = "name";
    private static final String BOOKMARK_DESCRIPTION = "description";
    private static final String BOOKMARK_LOCALIZATION = "localization";
    private static final String BOOKMARK_TIMEMARK = "timemark";
    private static final String BOOKMARK_TIMEMARK_IS_ACTUAL_DATE = "isActual";
    private static final WKTReader reader = new WKTReader();

    public BookmarkXMLPersistence(Object pluginClassInstance) {
        try {
            this.xml = XMLUtils.persistenceFromXML(XML_BOOKMARK);
        }
        catch (ConfigurationException e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        int child = 0;
        while (child < this.xml.getChildrenCount()) {
            if (this.xml.getChild(child).getPropertyValue(0).equals(BOOKMARK_CATEGORIES)) {
                this.categoriesXML = this.xml.getChild(child);
            }
            ++child;
        }
        if (this.categoriesXML == null) {
            XMLEntity xmlEntity = new XMLEntity();
            xmlEntity.putProperty("groupName", BOOKMARK_CATEGORIES);
            this.xml.addChild(xmlEntity);
            this.categoriesXML = xmlEntity;
        }
    }

    public BookmarkXMLPersistence() {
        try {
            this.xml = XMLUtils.persistenceFromXML(XML_BOOKMARK);
        }
        catch (ConfigurationException e) {
            LOGGER.error((Object)"", (Throwable)e);
            this.xml = new XMLEntity();
        }
        int child = 0;
        while (child < this.xml.getChildrenCount()) {
            if (this.xml.getChild(child).getPropertyValue(0).equals(BOOKMARK_CATEGORIES)) {
                this.categoriesXML = this.xml.getChild(child);
            }
            ++child;
        }
        if (this.categoriesXML == null) {
            XMLEntity xmlEntity = new XMLEntity();
            xmlEntity.putProperty("groupName", BOOKMARK_CATEGORIES);
            this.xml.addChild(xmlEntity);
            this.categoriesXML = xmlEntity;
        }
    }

    public void setPersistent() {
        try {
            File xmlFile = new File(XML_BOOKMARK);
            File parentDirectory = new File(xmlFile.getAbsolutePath()).getParentFile();
            if (!xmlFile.exists() && parentDirectory.canWrite() || xmlFile.canWrite()) {
                XMLUtils.persistenceToXML(this.xml, XML_BOOKMARK);
            } else {
                LOGGER.warn((Object)I18N.getMessage("org.saig.core.model.bookmark.BookmarkXMLPersistence.the-persistence-file-{0}-can-not-be-written", new Object[]{xmlFile.getAbsolutePath()}));
            }
        }
        catch (ConfigurationException e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
    }

    public void setBookmarkCategories(Collection<BookmarkCategory> categories) {
        this.categoriesXML.getXmlTag().removeAllXmlTag();
        for (BookmarkCategory currentCategory : categories) {
            this.categoriesXML.addChild(this.bookmarkCategoryToXml(currentCategory));
        }
    }

    public XMLEntity bookmarkToXml(IBookmark bookmark) {
        XMLEntity xmlEnt = new XMLEntity();
        xmlEnt.putProperty("name", bookmark.getName());
        xmlEnt.putProperty(BOOKMARK_DESCRIPTION, bookmark.getDescription());
        if (bookmark.getLocalization() != null) {
            xmlEnt.putProperty(BOOKMARK_LOCALIZATION, bookmark.getLocalization().toText());
        }
        if (bookmark instanceof TemporalBookmark) {
            TemporalBookmark timeBookmark = (TemporalBookmark)bookmark;
            if (timeBookmark.getTimemark() != null) {
                xmlEnt.putProperty(BOOKMARK_TIMEMARK, timeBookmark.getTimemark().toString());
            } else {
                xmlEnt.putProperty(BOOKMARK_TIMEMARK_IS_ACTUAL_DATE, true);
            }
        }
        return xmlEnt;
    }

    public XMLEntity bookmarkCategoryToXml(BookmarkCategory category) {
        XMLEntity xmlEnt = new XMLEntity();
        xmlEnt.putProperty("name", category.getName());
        for (IBookmark currentBookmark : category.getBookmarks()) {
            xmlEnt.addChild(this.bookmarkToXml(currentBookmark));
        }
        return xmlEnt;
    }

    public IBookmark xmlToBookmark(XMLEntity xmlEnt) {
        String timeString;
        String name = "";
        String description = "";
        Geometry localization = null;
        name = xmlEnt.getStringProperty("name");
        description = xmlEnt.getStringProperty(BOOKMARK_DESCRIPTION);
        if (xmlEnt.contains(BOOKMARK_LOCALIZATION)) {
            try {
                localization = reader.read(xmlEnt.getStringProperty(BOOKMARK_LOCALIZATION));
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
            }
        }
        Timestamp timemark = null;
        boolean isActualDate = false;
        if (xmlEnt.contains(BOOKMARK_TIMEMARK) && (timeString = xmlEnt.getStringProperty(BOOKMARK_TIMEMARK)) != null && !timeString.isEmpty()) {
            timemark = Timestamp.valueOf(timeString);
        }
        if (xmlEnt.contains(BOOKMARK_TIMEMARK_IS_ACTUAL_DATE)) {
            isActualDate = xmlEnt.getBooleanProperty(BOOKMARK_TIMEMARK_IS_ACTUAL_DATE);
        }
        BasicBookmark bookmark = null;
        bookmark = timemark == null && !isActualDate ? new BasicBookmark(name, description, localization) : new TemporalBookmark(name, description, localization, timemark);
        return bookmark;
    }

    public BookmarkCategory xmlToBookmarkCategory(XMLEntity xmlEnt) {
        String name = "";
        name = xmlEnt.getStringProperty("name");
        BookmarkCategory category = new BookmarkCategory(name);
        int i = 0;
        while (i < xmlEnt.getChildrenCount()) {
            category.addBookmark(this.xmlToBookmark(xmlEnt.getChild(i)));
            ++i;
        }
        return category;
    }

    public XMLEntity getXml() {
        return this.xml;
    }

    public void setXml(XMLEntity xml) {
        this.xml = xml;
    }

    public List<BookmarkCategory> getBookmarkCategories() {
        ArrayList<BookmarkCategory> categories = new ArrayList<BookmarkCategory>();
        int i = 0;
        while (i < this.categoriesXML.getChildrenCount()) {
            categories.add(this.xmlToBookmarkCategory(this.categoriesXML.getChild(i)));
            ++i;
        }
        return categories;
    }
}

