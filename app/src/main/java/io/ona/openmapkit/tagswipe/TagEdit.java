package io.ona.openmapkit.tagswipe;

import android.widget.EditText;
import android.widget.RadioGroup;

import com.spatialdev.osm.model.OSMElement;
import com.spatialdev.osm.renderer.util.ColorXmlParser;

import io.ona.openmapkit.odkcollect.ODKCollectData;
import io.ona.openmapkit.odkcollect.ODKCollectHandler;
import io.ona.openmapkit.odkcollect.tag.ODKTag;
import io.ona.openmapkit.odkcollect.tag.ODKTagItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Nicholas Hallahan on 3/4/15.
 * nhallahan@spatialdev.com
 * * *
 */
public class TagEdit {

    private static LinkedHashMap<String, TagEdit> tagEditHash;
    private static List<TagEdit> tagEdits;
    private static OSMElement osmElement;
    
    private String tagKey;
    private String tagVal;
    private ODKTag odkTag;
    private boolean readOnly;
    private int idx = -1;
    private EditText editText;
    private RadioGroup radioGroup;
    
    public static List<TagEdit> buildTagEdits() {
        int idx = 0;
        tagEditHash = new LinkedHashMap<>();
        tagEdits = new ArrayList<>();
        osmElement = OSMElement.getSelectedElements().getFirst();
        Map<String, String> tags = osmElement.getTags();
        
        // Tag Edits for ODK Collect Mode
        if (ODKCollectHandler.isODKCollectMode()) {
            Map<String, String> readOnlyTags = new HashMap<>(tags);
            ODKCollectData odkCollectData = ODKCollectHandler.getODKCollectData();
            Collection<ODKTag> requiredTags = odkCollectData.getRequiredTags();
            for (ODKTag odkTag : requiredTags) {
                String tagKey = odkTag.getKey();
                TagEdit tagEdit = new TagEdit(tagKey, tags.get(tagKey), odkTag, false, idx++);
                tagEditHash.put(tagKey, tagEdit);
                tagEdits.add(tagEdit);
                readOnlyTags.remove(tagKey);
            }
            Set<String> readOnlyKeys = readOnlyTags.keySet();
            for (String readOnlyKey : readOnlyKeys) {
                TagEdit tagEdit = new TagEdit(readOnlyKey, readOnlyTags.get(readOnlyKey), true, idx++);
                tagEditHash.put(readOnlyKey, tagEdit);
                tagEdits.add(tagEdit);
            }
        }
        
        // Tag Edits for Standalone Mode
        else {
            Set<String> keys = tags.keySet();
            for (String key : keys) {
                TagEdit tagEdit = new TagEdit(key, tags.get(key), false, idx++);
                tagEditHash.put(key, tagEdit);
                tagEdits.add(tagEdit);
            }
        }
        
        return tagEdits;
    }
    
    public static TagEdit getTag(int idx) {
        return tagEdits.get(idx);
    }
    
    public static TagEdit getTag(String key) {
        return tagEditHash.get(key);        
    }

    public static int getIndexForTagKey(String key) {
        TagEdit tagEdit = tagEditHash.get(key);
        if (tagEdit != null) {
            return tagEdit.getIndex();
        }
        // If its not there, just go to the first TagEdit
        return 0;
    }
    
    public static boolean saveToODKCollect() {
        updateTagsInOSMElement();

        String sprayStatus = osmElement.getTags().get("spray_status");
        if (sprayStatus == null) {
            return false;
        }
        ODKCollectHandler.saveXmlInODKCollect(osmElement);
        return true;
    }
    
    private static void updateTagsInOSMElement() {
        for (TagEdit tagEdit : tagEdits) {
            tagEdit.updateTagInOSMElement();
        }
    }
    
    private TagEdit(String tagKey, String tagVal, ODKTag odkTag, boolean readOnly, int idx) {
        this.tagKey = tagKey;
        this.tagVal = tagVal;
        this.odkTag = odkTag;
        this.readOnly = readOnly;
        this.idx = idx;
    }
    
    private TagEdit(String tagKey, String tagVal, boolean readOnly, int idx) {
        this.tagKey = tagKey;
        this.tagVal = tagVal;
        this.readOnly = readOnly;
        this.idx = idx;
    }

    /**
     * The EditText widget from StringTagValueFragment gets passed into here
     * so that the value can be retrieved on save.
     * * * *
     * @param editText
     */
    public void setEditText(EditText editText) {
        this.editText = editText;
    }
    
    public void setRadioGroup(RadioGroup radioGroup) {
        this.radioGroup = radioGroup;
    }
    
    public ODKTag getODKTag() {
        return odkTag;
    }
    
    private void updateTagInOSMElement() {
        if (radioGroup != null && odkTag != null) {
            int checkedId = radioGroup.getCheckedRadioButtonId();
            if (checkedId != -1) {
                tagVal = odkTag.getTagItemValueFromRadioButtonId(checkedId);
                osmElement.addOrEditTag(tagKey, tagVal);
            } else {
                osmElement.deleteTag(tagKey);
            }
        } else if (editText != null) {
            tagVal = editText.getText().toString();
            osmElement.addOrEditTag(tagKey, tagVal);
        }
    }
    
    public String getTitle() {
        return tagKey;
    }
    
    public String getTagKeyLabel() {
        if (odkTag != null) {
            return odkTag.getLabel();
        }
        return null;
    }

    public String getTagKey() {
        return tagKey;
    }
    
    public String getTagValLabel() {
        if (odkTag == null) return null;
        ODKTagItem item = odkTag.getItem(tagVal);
        if (item != null) {
            return item.getLabel();
        }
        return null;
    }
    
    public String getTagVal() {
        return tagVal;
    }
    
    public boolean isReadOnly() {
        return readOnly;
    }
    
    public boolean isSelectOne() {
        if ( !readOnly &&
                odkTag != null &&
                odkTag.getItems().size() > 0 ) {
            return true;
        }
        return false;
    }
    
    public int getIndex() {
        return idx;
    }
    
}
