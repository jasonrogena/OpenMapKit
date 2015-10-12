package io.ona.openmapkit.tagswipe;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import io.ona.openmapkit.R;
import io.ona.openmapkit.odkcollect.tag.ODKTag;
import io.ona.openmapkit.odkcollect.tag.ODKTagItem;

import java.util.Collection;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SelectOneTagValueFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SelectOneTagValueFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SelectOneTagValueFragment extends Fragment {

    private static final String IDX = "IDX";

    private TagEdit tagEdit;
    private View rootView;

    private TextView tagKeyLabelTextView;
    private TextView tagKeyTextView;

    private OnFragmentInteractionListener mListener;

    
    public static SelectOneTagValueFragment newInstance(int idx) {
        SelectOneTagValueFragment fragment = new SelectOneTagValueFragment();
        Bundle args = new Bundle();
        args.putInt(IDX, idx);
        fragment.setArguments(args);
        return fragment;
    }

    private void setupWidgets() {
        tagKeyLabelTextView = (TextView)rootView.findViewById(R.id.tagKeyLabelTextView);
        tagKeyTextView = (TextView)rootView.findViewById(R.id.tagKeyTextView);

        String keyLabel = tagEdit.getTagKeyLabel();
        String key = tagEdit.getTagKey();

        if (keyLabel != null) {
            tagKeyLabelTextView.setText(keyLabel);
            tagKeyTextView.setText(key);
        } else {
            tagKeyLabelTextView.setText(key);
            tagKeyTextView.setText("");
        }
        
        setupRadioButtons();
    }
    
    private void setupRadioButtons() {
        RadioGroup tagValueRadioGroup = (RadioGroup)rootView.findViewById(R.id.selectOneTagValueRadioGroup);
        tagEdit.setRadioGroup(tagValueRadioGroup);
        Activity activity = getActivity();
        ODKTag odkTag = tagEdit.getODKTag();
        if (odkTag == null) return;
        String prevTagVal = tagEdit.getTagVal();
        Collection<ODKTagItem> odkTagItems = odkTag.getItems();
        for (ODKTagItem item : odkTagItems) {
            String label = item.getLabel();
            String value = item.getValue();
            ToggleableRadioButton button = new ToggleableRadioButton(activity);
            button.setTextSize(18);
            TextView textView = new TextView(activity);
            textView.setPadding(66, 0, 0, 25);
            textView.setOnClickListener(new TextViewOnClickListener(button));
            if (label != null) {
                button.setText(label);
                textView.setText(value);
            } else {
                button.setText(value);
                textView.setText("");
            }
            tagValueRadioGroup.addView(button);
            if (prevTagVal != null && value.equals(prevTagVal)) {
                button.toggle();
            }
            int buttonId = button.getId();
            odkTag.putRadioButtonIdToTagItemHash(buttonId, item);
            tagValueRadioGroup.addView(textView);
        }
    }

    /**
     * Allows us to pass a RadioButton as a parameter to onClick
     * * * 
     */
    private class TextViewOnClickListener implements View.OnClickListener {
        RadioButton radioButton;
        
        public TextViewOnClickListener(RadioButton rb) {
            radioButton = rb;
        }
        
        @Override
        public void onClick(View v) {
            radioButton.toggle();
        }
    }
    
    public SelectOneTagValueFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            int idx = getArguments().getInt(IDX);
            tagEdit = TagEdit.getTag(idx);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView =  inflater.inflate(R.layout.fragment_select_one_tag_value, container, false);
        setupWidgets();
        return rootView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    /**
     * Allows the user to toggle off a previously checked radio button.
     * * * *
     * http://stackoverflow.com/questions/15836789/android-radio-button-uncheck
     * https://github.com/AmericanRedCross/OpenMapKit/issues/9
     * * * * 
     */
    public class ToggleableRadioButton extends RadioButton {
        public ToggleableRadioButton(Context context) {
            super(context);
        }

        @Override
        public void toggle() {
            if(isChecked()) {
                if(getParent() instanceof RadioGroup) {
                    ((RadioGroup)getParent()).clearCheck();
                }
            } else {
                setChecked(true);
            }
        }
    }
}
