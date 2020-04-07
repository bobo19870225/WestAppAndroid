package com.west.develop.westapp.UI.Fragment.Diagnosis;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.gson.Gson;
import com.west.develop.westapp.Bean.NCarBean;
import com.west.develop.westapp.CallBack.FragmentBackHandler;
import com.west.develop.westapp.Config.Config;
import com.west.develop.westapp.Dialog.TipDialog;
import com.west.develop.westapp.R;
import com.west.develop.westapp.Tools.MDBHelper;
import com.west.develop.westapp.Tools.Utils.PixelUtil;
import com.west.develop.westapp.UI.Activity.Diagnosis.DiagnosisActivity;
import com.west.develop.westapp.UI.Activity.MainActivity;
import com.west.develop.westapp.UI.Adapter.MainDiagnosisAdapter;
import com.west.develop.westapp.UI.base.BaseFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Develop11 on 2017/5/2.
 */

public class DiagnosisFragment extends BaseFragment implements FragmentBackHandler {

    private ImageView menuIv;
    private GridView mGridView;
    private MainDiagnosisAdapter mAdapter;
    private AutoCompleteTextView completeTV;
    private RadioButton searchDelete;
    private List<NCarBean> searchData = new ArrayList<>();

    private ToggleButton toggleButton;
    private ImageButton zoomAdd;
    private ImageButton zoomReduce;
    private Button addIcon;
    private static final int IMAGE_OPEN = 1;
    private TextView title;
    private final Context context = getContext();

    public static DiagnosisFragment newInstance(String param1, String param2) {
        return new DiagnosisFragment();
    }

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_diagnosis_main, container, false);
        initView(view);
        initData();
        initListener();
        return view;
    }

    private void initView(View view) {
        mGridView = view.findViewById(R.id.gird_Diagnosis_Resion);
        menuIv = view.findViewById(R.id.menu_iv);
        completeTV = view.findViewById(R.id.diagnosis_search_tv);
        searchDelete = view.findViewById(R.id.diagnosis_search_delete);
        toggleButton = view.findViewById(R.id.carsort);
        zoomAdd = view.findViewById(R.id.zoomadd);
        zoomReduce = view.findViewById(R.id.zoomreduce);
        addIcon = view.findViewById(R.id.diagnosis_addicon);
        title = view.findViewById(R.id.menu_title);
    }


    private void initData() {
        title.setText(getResources().getString(R.string.diagnosis_function));
        if (context != null) {
            toggleButton.setChecked(Config.getInstance(context).isToggle());
        }
        questCarList();
    }

    public void questCarList() {
        List<NCarBean> nCarBeans = requestData();
        if (nCarBeans != null) {
            if (nCarBeans.size() > 0) {
                List<String> items = new ArrayList<>();
                for (int i = 0; i < nCarBeans.size(); i++) {
                    if (context != null) {
                        if (Config.getInstance(context).getLanguage() == Config.LANGUAGE_EN) {
                            items.add(nCarBeans.get(i).getCarEnglishName());
                        } else if (Config.getInstance(context).getLanguage() == Config.LANGUAGE_CH) {
                            items.add(nCarBeans.get(i).getCarChineseName());
                        }
                    }

                }
                searchData.clear();
                searchData.addAll(nCarBeans);

                mAdapter = new MainDiagnosisAdapter(context, searchData);
                mGridView.setAdapter(mAdapter);

                int width = 0;
                if (context != null) {
                    width = PixelUtil.dp2px(context, 600 / Config.getInstance(context).getIconNum());
                }
                int fontSize = (int) (width * 0.1);
                mGridView.setColumnWidth(width);
                mAdapter.setTextSize(fontSize);

                try {
                    if (context != null) {
                        completeTV.setAdapter(new ArrayAdapter<>(context, R.layout.drop_down_item, items));
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                showDownloadTip();
            }
        }

    }

    private void showDownloadTip() {
        final TipDialog dialog = new TipDialog.Builder(context)
                .setTitle(getString(R.string.tip_title))
                .setMessage(getString(R.string.loadData_Tip))
                .setPositiveClickListener(getResources().getString(R.string.nowLoad_Tip), new TipDialog.OnClickListener() {
                    @Override
                    public void onClick(Dialog dialogInterface, int index, String label) {
                        dialogInterface.dismiss();
                        MainActivity mainActivity = (MainActivity) getActivity();
                        if (mainActivity != null) {
                            (mainActivity).onFragSwitch(MainActivity.UPGRADE_ID);
                        }
                    }
                })
                .setNegativeClickListener(getString(R.string.afterLoad_Tip), new TipDialog.OnClickListener() {
                    @Override
                    public void onClick(Dialog dialogInterface, int index, String label) {
                        dialogInterface.dismiss();
                        //finish();
                    }
                }).build();
        dialog.show();
    }

    private void upData(List<NCarBean> been) {
        searchData.clear();
        if (null != been) {
            searchData.addAll(been);
        }
        if (null != mAdapter) {
            mAdapter.notifyDataSetChanged();
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    private void initListener() {
        completeTV.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                int length = s.toString().length();
                if (length == 0) {
                    searchDelete.setVisibility(View.GONE);
                    List<NCarBean> carListBean = requestData();
                    upData(carListBean);
                } else {
                    searchDelete.setVisibility(View.VISIBLE);
                }
            }
        });
        completeTV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String s = completeTV.getText().toString();
                setSearchData(s);
            }
        });
        completeTV.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    String s = completeTV.getText().toString();
                    setSearchData(s);
                    return true;
                }
                return false;
            }
        });
        searchDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                completeTV.setText("");
                searchDelete.setVisibility(View.GONE);
            }
        });

        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (toggleButton.isChecked()) {
                    //拼音排序
                    sort(true);
                } else {
                    //序号排序
                    sort(false);
                }
            }
        });


        zoomAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (context != null && null != mAdapter) {
                    int iconNum = Config.getInstance(context).getIconNum() - 1;
                    Config.getInstance(context).setIconNum(iconNum);
                    int width = PixelUtil.dp2px(context, 600 / Config.getInstance(context).getIconNum());
                    int fontSize = (int) (width * 0.1);
                    mGridView.setColumnWidth(width);
                    mAdapter.setTextSize(fontSize);
                }
            }
        });

        zoomReduce.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (context != null && null != mAdapter) {
                    int iconNum = Config.getInstance(context).getIconNum() + 1;
                    Config.getInstance(context).setIconNum(iconNum);
                    int width = PixelUtil.dp2px(context, 600 / Config.getInstance(context).getIconNum());
                    int fontSize = (int) (width * 0.1);
                    mGridView.setColumnWidth(width);
                    mAdapter.setTextSize(fontSize);
                }
            }
        });


        menuIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).onFragSwitch(MainActivity.HOME_ID);
                }
            }
        });

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Gson gson = new Gson();
                String beanJson = gson.toJson(searchData.get(position));
                Intent intent = new Intent(context, DiagnosisActivity.class);
                intent.putExtra(DiagnosisActivity.kCarBean, beanJson);
                startActivity(intent);

            }
        });

        addIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, IMAGE_OPEN);

            }
        });

        mGridView.setOnTouchListener(new View.OnTouchListener() {
            float currentDistance;
            float lastDistance;
            boolean isScaled = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        if (event.getPointerCount() >= 2) {
                            isScaled = true;
                            float offSetX = event.getX(0) - event.getX(1);
                            float offSetY = event.getY(0) - event.getY(1);
                            currentDistance = (float) Math.sqrt(offSetX * offSetX + offSetY * offSetY);

                            if (lastDistance <= 0) {
                                lastDistance = currentDistance;
                            } else if (currentDistance - lastDistance > 60) {
                                // 放大
                                zoomAdd.performClick();
                                lastDistance = currentDistance;
                            } else if (lastDistance - currentDistance > 60) {
                                //缩小
                                zoomReduce.performClick();
                                lastDistance = currentDistance;
                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        lastDistance = 0;
                        currentDistance = 0;
                        if (isScaled) {
                            isScaled = false;
                            return true;
                        }
                        break;
                }
                return false;
            }
        });
    }

    //排序
    private void sort(boolean isPINYIN) {
        List<NCarBean> list;
        //等数据全部下载完了以后才显示数据
        if (isPINYIN) {
            list = MDBHelper.getInstance(getActivity()).getCarList(Config.SORT_BY_PINYIN);
            if (context != null) {
                Config.getInstance(context).setToggle(true);
            }
        } else {
            list = MDBHelper.getInstance(getActivity()).getCarList(Config.SORT_BY_ID);
            if (context != null) {
                Config.getInstance(context).setToggle(false);
            }
        }
        upData(list);

    }

    //搜素数据---更新列表
    private void setSearchData(String data) {
        if (mAdapter == null) {
            return;
        }
        if (data == null || data.equals("")) {
            return;
        }

        for (int i = 0; i < searchData.size(); i++) {
            String searchName = null;
            if (context != null) {
                if (Config.getInstance(context).getLanguage() == Config.LANGUAGE_EN) {
                    searchName = searchData.get(i).getCarEnglishName();
                } else if (Config.getInstance(context).getLanguage() == Config.LANGUAGE_CH) {
                    searchName = searchData.get(i).getCarChineseName();
                }
            }
            if (searchName != null && !searchName.equals(data)) {
                searchData.remove(i);
                i--;
            }
        }

        mAdapter.notifyDataSetChanged();

    }

    private List<NCarBean> requestData() {
        if (context != null) {
            return MDBHelper.getInstance(context).getCarList(Config.getInstance(context).getSortBy());
        }
        return null;
    }

    @Override
    public boolean onBackPressed() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).onFragSwitch(MainActivity.HOME_ID);
        }
        return true;
    }
}