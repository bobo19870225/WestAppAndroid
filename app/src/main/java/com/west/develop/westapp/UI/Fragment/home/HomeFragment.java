package com.west.develop.westapp.UI.Fragment.home;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.west.develop.westapp.UI.base.BaseFragment;
import com.west.develop.westapp.UI.Activity.MainActivity;
import com.west.develop.westapp.R;

public class HomeFragment extends BaseFragment implements View.OnClickListener{

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private LinearLayout diagnosis;
    private LinearLayout upgrade;
    private LinearLayout setting;
    private LinearLayout report;
    private Button button;

    public HomeFragment() {
        // Required empty public constructor
    }


    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_home, container, false);
        ininView(view);
        return view;
    }

    private void ininView(View view) {
        diagnosis = (LinearLayout) view.findViewById(R.id.main_sliding_diagnosis_rb);
        upgrade = (LinearLayout) view.findViewById(R.id.main_sliding_upgrade_rb);
        setting = (LinearLayout) view.findViewById(R.id.main_sliding_setting_rb);
        report = (LinearLayout) view.findViewById(R.id.main_sliding_comm_rb);
       // button = (Button) view.findViewById(R.id.video);

        diagnosis.setOnClickListener(this);
        upgrade.setOnClickListener(this);
        setting.setOnClickListener(this);
        report.setOnClickListener(this);
        //button.setOnClickListener(this);
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_sliding_diagnosis_rb:
                if (getActivity() instanceof MainActivity){
                    ((MainActivity) getActivity()).onFragSwitch(MainActivity.DIAGNOSIS_ID);
                }
                break;
            case R.id.main_sliding_upgrade_rb:
                if (getActivity() instanceof MainActivity){
                    ((MainActivity) getActivity()).onFragSwitch(MainActivity.UPGRADE_ID);
                }
                break;
            case R.id.main_sliding_setting_rb:
                if (getActivity() instanceof MainActivity){
                    ((MainActivity) getActivity()).onFragSwitch(MainActivity.SETTING_ID);
                }
                break;
            case R.id.main_sliding_comm_rb:
                if (getActivity() instanceof MainActivity){
                    ((MainActivity) getActivity()).onFragSwitch(MainActivity.REPORT_ID);
                }
                break;
           /* case R.id.video:
                Intent intent = new Intent(getActivity(), OnlineVideoActivity.class);
                startActivity(intent);*/
        }

    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
