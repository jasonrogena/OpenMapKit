/**
 * Created by Nicholas Hallahan on 1/2/15.
 * nhallahan@spatialdev.com
 */
package com.spatialdev.osm.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.spatialdev.osm.OSMUtil;
import com.spatialdev.osm.renderer.OSMPath;
import com.vividsolutions.jts.geom.Geometry;

import org.xmlpull.v1.XmlSerializer;


public abstract class OSMElement {
    
    private static LinkedList<OSMElement> selectedElements = new LinkedList<>();
    private static boolean selectedElementsChanged = false;
    
    private static LinkedList<OSMElement> modifiedElements = new LinkedList<>();
    private static LinkedList<OSMElement> modifiedElementsInInstance = new LinkedList<>();

    protected final OSMColorConfig osmColorConfig;
    protected long id;
    protected long version;
    protected String timestamp;
    protected long changeset;
    protected long uid;
    protected String user;
    protected boolean selected = false;
    
    // set to true if the tags for this element have been modified
    protected boolean modified = false;
    
    // set to true if the application modifies tags for this element in this instance
    protected boolean modifiedInInstance = false;

    protected Geometry jtsGeom;

    /**
     * These tags get modified by the application
     */
    protected Map<String, String> tags = new LinkedHashMap<>();

    /**
     * This can be used to keep track of which tag is currently selected in a tag editor
     * like OpenMapKit.
     * * *
     */
    protected String selectedTag;

    /**
     * These tags are the original tags in the data set. This SHOULD NOT BE MODIFIED. 
     */
    protected Map<String, String> originalTags = new LinkedHashMap<>();

    /**
     * This is the object that actually gets drawn by OSMOverlay. 
     */
    protected OSMPath osmPath;

    /**
     * Elements that have been put in a select state* 
     * @return
     */
    public static LinkedList<OSMElement> getSelectedElements() {
        return selectedElements;
    }

    /**
     * All of the modified elements we've got in memory, including those in previous
     * edits of previous survey instances that have been scraped from ODK Collect.
     * * * * 
     * @return all modified OSMElements
     */
    public static LinkedList<OSMElement> getModifiedElements() {
        return modifiedElements;        
    }

    /**
     * Only the modified elements that have had their tags modified in this survey instance
     * * *
     * @return elements with modified tags in this survey instance
     */
    public static LinkedList<OSMElement> getModifiedElementsInInstance() {
        return modifiedElementsInInstance;        
    }
    
    public static boolean hasSelectedElementsChanged() {
        if (selectedElementsChanged) {
            selectedElementsChanged = false;
            return true;
        }
        return false;
    }
    
    public static void deselectAll() {
        for (OSMElement el : selectedElements) {
            selectedElementsChanged = true;
            el.deselect();
        }
    }

    /**
     * This constructor is used by OSMDataSet in the XML parsing process.
     */
    public OSMElement(String idStr,
                      String versionStr,
                      String timestampStr,
                      String changesetStr,
                      String uidStr,
                      String userStr,
                      String action,
                      OSMColorConfig osmColorConfig) {
        try {
            id = Long.valueOf(idStr);
        } catch (Exception e) {
            // dont assign
        }
        try {
            version = Long.valueOf(versionStr);
        } catch (Exception e) {
            // dont assign
        }
        try {
            timestamp = timestampStr;
        } catch (Exception e) {
            // dont assign
        }
        try {
            changeset = Long.valueOf(changesetStr);
        } catch (Exception e) {
            // dont assign
        }
        try {
            uid = Long.valueOf(uidStr);
        } catch (Exception e) {
            // dont assign
        }
        try {
            user = userStr;
        } catch (Exception e) {
            // dont assign
        }
        if (action != null && action.equals("modify")) {
            setAsModified();
        }
        this.osmColorConfig = osmColorConfig;
    }

    /**
     * This constructor is used when we are creating an new OSMElement,
     * such as when a new Node is created. This constructor assumes
     * that we are creating a NEW element in the current survey.
     */
    public OSMElement(OSMColorConfig osmColorConfig) {
        id = getUniqueNegativeId();
        setAsModifiedInInstance();
        this.osmColorConfig = osmColorConfig;
    }

    public static long getUniqueNegativeId() {
        /*negativeId--;
        return uniqueDevId + negativeId;*/
        long random = UUID.randomUUID().getMostSignificantBits();
        if(random < 0){
            return random;
        } else {
            return random * -1;
        }
    }

    /**
     * All OSM Element types need to have this implemented. This checksum is composed
     * of the tags sorted alphabetically by key. The rest of the implementation is
     * defined differently whether it is Node, Way, or Relation.
     *
     * @return SHA-1 HEX checksum of the element
     */
    public abstract String checksum();

    /**
     * The tags are sorted by key, and each key, value is
     * iterated and concatenated to a String.
     *
     * @return
     */
    public StringBuilder tagsAsSortedKVString() {
        List<String> keys = new ArrayList<>(tags.keySet());
        java.util.Collections.sort(keys);
        StringBuilder tagsStr = new StringBuilder();
        for (String k : keys) {
            String v = tags.get(k);
            if (v.length() > 0) {
                tagsStr.append(k);
                tagsStr.append(v);
            }
        }
        return tagsStr;
    }

    void xml(XmlSerializer xmlSerializer, String omkOsmUser) throws IOException {
        // set the tags for the element (all element types can have tags)
        Set<String> tagKeys = tags.keySet();
        for (String tagKey : tagKeys) {
            String tagVal = tags.get(tagKey);
            if (tagVal == null || tagVal.equals("")) {
                continue;
            }
            xmlSerializer.startTag(null, "tag");
            xmlSerializer.attribute(null, "k", tagKey);
            xmlSerializer.attribute(null, "v", tagVal);
            xmlSerializer.endTag(null, "tag");
        }
    }
    
    protected void setOsmElementXmlAttributes(XmlSerializer xmlSerializer, String omkOsmUser) throws IOException {
        xmlSerializer.attribute(null, "id", String.valueOf(id));
        if (isModified()) {
            xmlSerializer.attribute(null, "action", "modify");
        }
        if (version != 0) {
            xmlSerializer.attribute(null, "version", String.valueOf(version));
        }
        if (changeset != 0) {
            xmlSerializer.attribute(null, "changeset", String.valueOf(changeset));
        }
        /**
         * If the element just got modified, we want to set the time stamp when the record
         * is serialized. If it has not been modified or was modified in a previous session,
         * we want to stay with the previously recorded timestamp.
         */
        if (modifiedInInstance) {
            xmlSerializer.attribute(null, "timestamp", OSMUtil.nowTimestamp());
        } else if (timestamp != null) {
            xmlSerializer.attribute(null, "timestamp", timestamp);
        }
        /**
         * We want to put the OSM user set in OMK Android for all of the elements we are writing.
         * This is important, because when we are filtering in OMK iD, we need to be able to filter
         * by OSM user. The OSM user should refer to all elements affected by an edit. This means
         * that the OMK Android OSM user name should apply to nodes referenced by an edited way
         * as well (so that filtering gets the complete geometry in).
         */
        xmlSerializer.attribute(null, "user", omkOsmUser);
    }

    /**
     * Maintains state over which tag is selected in a tag editor UI
     * * *
     * @param tagKey
     */
    public void selectTag(String tagKey) {
        selectedTag = tagKey;
    }

    /**
     * If a tag is edited or added, this should be called by the application.* 
     * @param k
     * @param v
     */
    public void addOrEditTag(String k, String v) {
        // OSM requires tag keys and values to not have trailing whitespaces.
        String trimKey = k.trim();
        String trimVal = v.trim();
        
        String origVal = tags.get(trimKey);
        // if the original tag is the same as this, we're not really editing anything.
        if (trimVal.equals(origVal)) {
            return;
        }
        setAsModifiedInInstance();
        tags.put(trimKey, trimVal);
    }

    /**
     * If the user removes a tag, call this method with the key of the tag.* 
     * @param k
     */
    public void deleteTag(String k) {
        String origVal = tags.get(k);
        // Don't do anything if we are not deleting anything.
        if (origVal == null) {
            return;
        }
        setAsModifiedInInstance();
        tags.remove(k);
    }
    
    public boolean isModified() {
        return modified;
    }

    /**
     * Any element that has been modified, either in the current instance or in previous
     * survey instances.
     * * * * 
     */
    protected void setAsModified() {
        modified = true;
        modifiedElements.add(this);
    }

    /**
     * This is when an element is modified in this survey instance rather than a previous survey.
     * We need to know this so that the edits can be written to OSM XML in ODK Collect.
     * * * 
     */
    private void setAsModifiedInInstance() {
        setAsModified();
        modifiedInInstance = true;
        modifiedElementsInInstance.add(this);
    }
    
    /**
     * This should only be used by the parser. 
     * @param k
     * @param v
     */
    public void addParsedTag(String k, String v) {
        originalTags.put(k, v);
        tags.put(k, v);
    }
    

    public long getId() {
        return id;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public int getTagCount() {
        return tags.size();
    }

    public void setJTSGeom(Geometry geom) {
        jtsGeom = geom;
    }

    public Geometry getJTSGeom() {
        return jtsGeom;
    }

    public void select() {
        selectedElementsChanged = true;
        selected = true;
        selectedElements.push(this);
        if (osmPath != null) {
            osmPath.select();
        }
    }

    public void deselect() {
        selectedElementsChanged = true;
        selected = false;
        selectedElements.remove(this);
        if (osmPath != null) {
            osmPath.deselect();
        }
    }

    public void toggle() {
        if (selected) {
            deselect();
        } else {
            select();
        }
    }

    public boolean isSelected() {
        return selected;
    }

    public OSMColorConfig getOsmColorConfig() {
        return this.osmColorConfig;
    }

}
