package com.west.develop.westapp.UI.base;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

/**
 * Created by Develop11 on 2017/5/2.
 */
public abstract class BaseFragment extends Fragment {


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public void showLogd(String msg) {
        Log.d("xxxxxxx", msg);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        /*if (Config.getInstance(context).getLanguage() == Config.LANGUAGE_EN) {//如果是英文，则执行以下程序
            MDBHelper.getInstance(context).updateCarNames();
            //Config.getInstance(context).setTbIsZhorEn(true);
        } else {
            MDBHelper.getInstance(context).updateCarNames();
            //Config.getInstance(context).setTbIsZhorEn(false);
        }*/
    }

}
