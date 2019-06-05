package com.example.studybitsdocuments.interfaces.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.studybitsdocuments.R;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link IdentityFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link IdentityFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class IdentityFragment extends Fragment {
    private static final String DID = "did";
    private static final String VERKEY = "verkey";
    private static final String NAME = "name";

    //args references
    private String mDid;
    private String mVerkey;
    private String mName;

    private OnFragmentInteractionListener mListener;

    public IdentityFragment() {
        // Required empty public constructor
    }

    public static IdentityFragment newInstance(String did, String verkey, String name) {
        IdentityFragment fragment = new IdentityFragment();
        Bundle args = new Bundle();
        args.putString(DID, "Did: " + did);
        args.putString(VERKEY, "Verkey: " + verkey);
        args.putString(NAME, "Name: " + name);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mDid = getArguments().getString(DID);
            mVerkey = getArguments().getString(VERKEY);
            mName = getArguments().getString(NAME);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_identity, container, false);


        //ui references
        TextView mDidView = rootView.findViewById(R.id.did);
        TextView mVerkeyView = rootView.findViewById(R.id.verkey);
        TextView mNameView = rootView.findViewById(R.id.name);

        mDidView.setText(mDid);
        mVerkeyView.setText(mVerkey);
        mNameView.setText(mName);

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
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
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
