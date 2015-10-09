package org.ona.openmapkit.tagswipe;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import org.ona.openmapkit.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ReadOnlyTagFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ReadOnlyTagFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReadOnlyTagFragment extends Fragment {

    private static final String IDX = "IDX";

    private TagEdit tagEdit;
    private View rootView;

    private TextView tagKeyLabelTextView;
    private TextView tagKeyTextView;
    private TextView tagValueLabelTextView;
    private TextView tagValueTextView;
    
    
    private OnFragmentInteractionListener mListener;


    public static ReadOnlyTagFragment newInstance(int idx) {
        ReadOnlyTagFragment fragment = new ReadOnlyTagFragment();
        Bundle args = new Bundle();
        args.putInt(IDX, idx);
        fragment.setArguments(args);
        return fragment;
    }
    
    private void setupWidgets() {
        tagKeyLabelTextView = (TextView)rootView.findViewById(R.id.tagKeyLabelTextView);
        tagKeyTextView = (TextView)rootView.findViewById(R.id.tagKeyTextView);
        tagValueLabelTextView = (TextView)rootView.findViewById(R.id.tagValueLabelTextView);
        tagValueTextView = (TextView)rootView.findViewById(R.id.tagValueTextView);
        
        String keyLabel = tagEdit.getTagKeyLabel();
        String key = tagEdit.getTagKey();
        String valLabel = tagEdit.getTagValLabel();
        String val = tagEdit.getTagVal();

        if (keyLabel != null) {
            tagKeyLabelTextView.setText(keyLabel);
            tagKeyTextView.setText(key);
        } else {
            tagKeyLabelTextView.setText(key);
            tagKeyTextView.setText("");
        }
        
        if (valLabel != null) {
            tagValueLabelTextView.setText(valLabel);
            tagValueTextView.setText(val);
        } else {
            tagValueLabelTextView.setText(val);
            tagValueTextView.setText("");
        }
    }

    public ReadOnlyTagFragment() {
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
        
        rootView = inflater.inflate(R.layout.fragment_read_only_tag, container, false);
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

}
