package com.west.develop.westapp.UI.Fragment.Upgrade;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.NoConnectionError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.west.develop.westapp.Bean.Upgrade.UpdateDB;
import com.west.develop.westapp.CallBack.FragmentBackHandler;
import com.west.develop.westapp.CallBack.SelectItemListener;
import com.west.develop.westapp.Communicate.Service.UsbService;
import com.west.develop.westapp.Config.Config;
import com.west.develop.westapp.CustomView.QuikeSideBarView.OnQuickSideBarTouchListener;
import com.west.develop.westapp.CustomView.QuikeSideBarView.QuickSideBarView;
import com.west.develop.westapp.CustomView.RefreshLayout;
import com.west.develop.westapp.Dialog.LoadDialog;
import com.west.develop.westapp.Dialog.SignDialog;
import com.west.develop.westapp.Dialog.TipDialog;
import com.west.develop.westapp.Download.Threads.CompareVersionThread;
import com.west.develop.westapp.R;
import com.west.develop.westapp.Tools.MDBHelper;
import com.west.develop.westapp.Tools.Utils.FileUtil;
import com.west.develop.westapp.Tools.Utils.VolleyUtil;
import com.west.develop.westapp.Tools.Utils.VolleyUtil.IVolleyCallback;
import com.west.develop.westapp.Tools.constant.RequestCodeConstant;
import com.west.develop.westapp.Tools.constant.URLConstant;
import com.west.develop.westapp.UI.Activity.MainActivity;
import com.west.develop.westapp.UI.Activity.Upgrade.DownloadTaskActivity;
import com.west.develop.westapp.UI.Adapter.UpgradeAdapter;
import com.west.develop.westapp.UI.base.BaseFragment;
import com.west.develop.westapp.usb.UsbSerialPort;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class UpgradeFragment extends BaseFragment
        implements View.OnClickListener,FragmentBackHandler,
        OnQuickSideBarTouchListener {

    private static final int MSG_REFRESH = 0;
    private static final int MSG_COMPARE = 1;
    private static final int MSG_LOCAL_NEW_VERSION = 2;
    private static final int MSG_FINDVERSION = 3;
    private static final int MSG_SHOW_LOADING_DIALOG = 5;

    private UpgradeAdapter adapter;
    private ListView listView;

    private ScrollView quickSideScroll;
    private QuickSideBarView quickSideBarView;

    private RefreshLayout swipeRefreshLayout;
    private CheckBox checkBoxAll;
    private LinearLayout checkBoxAll_ll;
    private ImageView menuIv;
    private ImageView refreshIv;
    private TextView menu_title;
    private TextView update_num;
    private Button updateRadiobutton_ll;
    private OnFragmentInteractionListener mListener;
    private boolean allItemflag = false;//标志全选按钮是否要被选中

    private LoadDialog mDialog;


    /**
     * 网络请求回调
     */
    IVolleyCallback callback = new IVolleyCallback() {
        @Override
        public void getResponse(JSONObject jsonObject) {
            Log.e("Volley CARUPDATEVERSION",jsonObject.toString());
            if ("".equals(jsonObject) || jsonObject == null) {
                mHandler.sendEmptyMessage(MSG_REFRESH);
                return;
            }
            try {
                if (list == null) {
                    list = new ConcurrentHashMap<>();
                }
                list.clear();
                int code = jsonObject.getInt("code");
                if (code == 0) {
                    JSONObject arr = jsonObject.getJSONObject("data");
                    final JSONObject json = arr.getJSONObject("versions");
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Gson gson = new Gson();
                                ConcurrentHashMap<String, LinkedTreeMap> map = gson.fromJson(json.toString(), ConcurrentHashMap.class);
                                for (String key : map.keySet()) {
                                    LinkedTreeMap updateDBStr = map.get(key);

                                    JSONObject jsonObject1 = new JSONObject(updateDBStr);

                                    UpdateDB updateDB = gson.fromJson(jsonObject1.toString(), UpdateDB.class);

                                    MDBHelper.getInstance(getContext()).insertProgramAuth(updateDB);
                                    list.put(updateDB.getProgramName(), updateDB);
                                }

                                if(list.isEmpty()){
                                    mHandler.sendEmptyMessage(MSG_REFRESH);
                                }
                                else {
                                    mHandler.sendEmptyMessage(MSG_COMPARE);
                                }
                            } catch (Exception ex) {
                                mHandler.sendEmptyMessage(MSG_REFRESH);
                            }
                        }
                    }).start();
                }

            } catch (JSONException e) {
                mHandler.sendEmptyMessage(MSG_REFRESH);
                e.printStackTrace();
            }

        }

        @Override
        public void onErrorResponse(VolleyError error) {
            mHandler.sendEmptyMessage(MSG_REFRESH);
            if (error instanceof TimeoutError) {
                Log.e("questVersionError", "TimeOut");
                Toast.makeText(getContext(),getString(R.string.toast_netconn_moreTime),Toast.LENGTH_SHORT).show();
            }
            if (error instanceof NoConnectionError) {
                Log.e("questVersionError", "NoConnectionError");
                Toast.makeText(getContext(), getString(R.string.toast_inspect_netconn), Toast.LENGTH_SHORT).show();
            }
            if (error.networkResponse != null) {
                String errorStr = new String(error.networkResponse.data);
                Log.e("questVersionError", errorStr);
            }
        }
    };

    private ConcurrentHashMap<String,UpdateDB> list = new ConcurrentHashMap<>();
    private  ConcurrentHashMap<String,UpdateDB> versions = new  ConcurrentHashMap<String,UpdateDB>();
    private static UpgradeFragment instance;

    public  ConcurrentHashMap<String,UpdateDB> getList() {
        return list;
    }
    public UpgradeFragment() {
        instance = this;
    }

    public static UpgradeFragment newInstance() {

        Bundle args = new Bundle();
        UpgradeFragment fragment = new UpgradeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static UpgradeFragment getInstance(){
        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.e("Upgrade", "onCreateView");
        View inflate = inflater.inflate(R.layout.fragment_upgrade, container, false);
        initView(inflate);
        initLlistener();
        initData();
        return inflate;
    }


    private void initLlistener() {
        quickSideBarView.setOnQuickSideBarTouchListener(this);
        //q全选监听
        checkBoxAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (allItemflag){
                    allItemflag = false;
                    return;
                }
                boolean flag = checkBoxAll.isChecked();
                adapter.setAllChecked(flag);
                if (flag) {
                    if (adapter.getSelectCount() == 0){
                        menu_title.setText(R.string.main_upgrade);
                    }else {
                        int delete = 0;
                        int count = 0;
                        for (int i = 0; i < adapter.getCount() ; i++) {
                            if (adapter.getItem(i).isChecked()){
                                if(adapter.getItem(i).isDelete()){
                                    delete++;
                                }
                                else {
                                    count++;
                                }
                            }
                        }
                        String text = "";
                        if(count != 0){
                            text =  getResources().getString(R.string.upgrade_select) + " "+ count + " " + getResources().getString(R.string.upgrade_select_num);
                            if(delete != 0){
                                text += " , ";
                            }
                        }
                        if(delete != 0){
                            text += getString(R.string.delete_select) + " " + delete + "" + getResources().getString(R.string.upgrade_select_num);
                        }
                        menu_title.setText(text);
                    }
                } else {
                    menu_title.setText(R.string.main_upgrade);
                }
                adapter.notifyDataSetChanged();

            }
        });


        /**
         * 页面滚动监听
         */
        swipeRefreshLayout.setChildScrollListener(new RefreshLayout.ChildScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if(adapter.getItem(firstVisibleItem) != null){
                    quickSideBarView.setChooseLetter(adapter.getItem(firstVisibleItem).getFirstLetter());
                    int posY = quickSideBarView.getLetterY(adapter.getItem(firstVisibleItem).getFirstLetter());

                    int scrollY = quickSideScroll.getScrollY();
                    int height = quickSideScroll.getMeasuredHeight();

                    if(posY - height > scrollY){
                        posY = posY - height;
                        quickSideScroll.scrollTo(0,posY);
                    }
                    if(posY <= scrollY){
                        quickSideScroll.scrollTo(0,posY - quickSideBarView.getItemHeight());
                    }
                }
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pullDownToRefresh();
            }
        });

        //swipeRefreshLayout.setRefreshing();

        checkBoxAll_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkBoxAll.performClick();
            }
        });
        refreshIv.setOnClickListener(this);
        menuIv.setOnClickListener(this);
        updateRadiobutton_ll.setOnClickListener(this);
    }


    private void initView(View inflate) {
        menuIv = (ImageView) inflate.findViewById(R.id.menu_iv);
        menu_title = (TextView) inflate.findViewById(R.id.menu_title);
        update_num = (TextView) inflate.findViewById(R.id.text_num);
        //主界面
        checkBoxAll = (CheckBox) inflate.findViewById(R.id.checkbox);
        checkBoxAll_ll = (LinearLayout) inflate.findViewById(R.id.checkbox_ll);
        updateRadiobutton_ll = (Button) inflate.findViewById(R.id.update_ll);
        updateRadiobutton_ll.setVisibility(View.VISIBLE);
        refreshIv = (ImageView)inflate.findViewById(R.id.refresh_Btn);


        listView = (ListView) inflate.findViewById(R.id.listview);

        quickSideScroll = (ScrollView)inflate.findViewById(R.id.quickSideScroll);
        quickSideBarView = (QuickSideBarView) inflate.findViewById(R.id.quickSideBarView);

        swipeRefreshLayout = (RefreshLayout) inflate.findViewById(R.id.swiprefresh);

        swipeRefreshLayout.setColorSchemeColors(getActivity().getResources().getColor(R.color.colorAccent));

        adapter = new UpgradeAdapter(getActivity(), new SelectItemListener() {
            //如果item全部选中时，全选按钮被选中，只要有一个item没有被选中，那么全选按钮没有被选中
            @Override
            public void select(boolean b) {
                if (!b && !checkBoxAll.isChecked()){
                    menu_title.setText(R.string.main_upgrade);
                    int delete = 0;
                    int count = 0;
                    for (int i = 0; i < adapter.getCount() ; i++) {
                        if (adapter.getItem(i).isChecked()){
                            if(adapter.getItem(i).isDelete()){
                                delete++;
                            }
                            else {
                                count++;
                            }
                        }
                    }
                    String text = "";
                    if(count != 0){
                        text =  getResources().getString(R.string.upgrade_select) + " "+ count + " " + getResources().getString(R.string.upgrade_select_num);
                        if(delete != 0){
                            text += " , ";
                        }
                    }
                    if(delete != 0){
                        text += getString(R.string.delete_select) + " " + delete + "" + getResources().getString(R.string.upgrade_select_num);
                    }
                    menu_title.setText(text);
                    return;
                }else if (!b && checkBoxAll.isChecked()){
                    allItemflag = true;
                    checkBoxAll.setChecked(false);
                    int delete = 0;
                    int count = 0;
                    for (int i = 0; i < adapter.getCount() ; i++) {
                        if (adapter.getItem(i).isChecked()){
                            if(adapter.getItem(i).isDelete()){
                                delete++;
                            }
                            else {
                                count++;
                            }
                        }
                    }
                    String text = "";
                    if(count != 0){
                        text =  getResources().getString(R.string.upgrade_select) + " "+ count + " " + getResources().getString(R.string.upgrade_select_num);
                        if(delete != 0){
                            text += " , ";
                        }
                    }
                    if(delete != 0){
                        text += getString(R.string.delete_select) + " " + delete + "" + getResources().getString(R.string.upgrade_select_num);
                    }
                    menu_title.setText(text);
                }else if (b){
                    allItemflag = true;
                    checkBoxAll.setChecked(true);
                    int delete = 0;
                    int count = 0;
                    for (int i = 0; i < adapter.getCount() ; i++) {
                        if (adapter.getItem(i).isChecked()){
                            if(adapter.getItem(i).isDelete()){
                                delete++;
                            }
                            else {
                                count++;
                            }
                        }
                    }
                    String text = "";
                    if(count != 0){
                        text =  getResources().getString(R.string.upgrade_select) + " "+ count + " " + getResources().getString(R.string.upgrade_select_num);
                        if(delete != 0){
                            text += " , ";
                        }
                    }
                    if(delete != 0){
                        text += getString(R.string.delete_select) + " " + delete + "" + getResources().getString(R.string.upgrade_select_num);
                    }
                    menu_title.setText(text);
                }
            }
        });

        listView.setAdapter(adapter);
    }


    private void initData() {
        menu_title.setText(R.string.main_upgrade);
        mHandler.sendEmptyMessage(MSG_FINDVERSION);
        getUpdateVersion();
        mHandler.sendEmptyMessage(MSG_SHOW_LOADING_DIALOG);
        listView.scrollBy(0, 1);
    }

    private void getUpdateVersion() {
        if (Config.getInstance(getContext()).getBondDevice() != null){
            VolleyUtil.jsonRequest(URLConstant.CAR_UPDATE_VERSION + Config.getInstance(getContext()).getBondDevice().getDeviceSN(), getContext(), callback);
        }
    }


    /**
     * 刷新
     */
    public void refresh() {
        swipeRefreshLayout.setRefreshing(isLoading);

        if (!isLoading) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        ArrayList<UpdateDB> listup = MDBHelper.getInstance(getContext()).getUpdateList();

                        Set<String> set = adapter.getmSet();
                        Set<String> set1 =new HashSet<>();
                        if (set != null) {
                            Iterator it = set.iterator();
                            while (it.hasNext()) {
                                String name = (String) it.next();
                                for (int i = 0; i < listup.size(); i++) {
                                    if (listup.get(i).getProgramName().equals(name)) {
                                        listup.get(i).setChecked(true);
                                        set1.add(listup.get(i).getProgramName());
                                    }
                                }
                            }
                        }

                        adapter.clear();
                        adapter.setmSet(set1);
                        adapter.addAll(listup);
                        //  adapter.notifyDataSetChanged();
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();
                            }
                        });
                        mHandler.sendEmptyMessage(MSG_REFRESH);
                    } catch (Exception e) {
                        Log.e("upgraderefresh", "run: " + e.toString());
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    /**
     * 版本比较
     */
    private void compareVersion() {
        if (list == null || list.isEmpty()) {
            return;
        }

        if(CompareVersionThread.getInstance() == null) {
            /**
             * 开启比较线程
             */
            CompareVersionThread.newInstance(getContext(), versions, list, adapter.getList(), new CompareVersionThread.CompareCallback() {
                @Override
                public void onFinish(List<UpdateDB> displayList,List<UpdateDB> deleteList) {
                    if (displayList != null && !displayList.isEmpty()) {
                        adapter.clear();
                        adapter.addAll(displayList);
                    }
                    adapter.addDeleteAll(deleteList);
                    mHandler.sendEmptyMessage(MSG_REFRESH);
                }

                @Override
                public void onStart() {
                    if(mDialog != null){
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mDialog.setTitle(getString(R.string.compare_Version));
                            }
                        });
                    }
                }
            }).start();
        }

    }


    private boolean isLoading = false;
    private void pullDownToRefresh() {
        if (!isLoading) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mHandler.sendEmptyMessage(MSG_FINDVERSION);
                    getUpdateVersion();
                    listView.scrollBy(0, 1);
                }
            }).start();
            isLoading = true;
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RequestCodeConstant.CODE_ONE_KEY_ACTIVITY) {
            refresh();
        }
    }

    @Override
    public void onLetterChanged(char letter, int position, float y) {
        //有此key则获取位置并滚动到该位置
        int posit = adapter.getPosition(letter);
        if(posit >= 0){
            listView.setSelection(posit);
        }
    }

    @Override
    public void onLetterTouching(boolean touching) {
        //可以自己加入动画效果渐显渐隐
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.update_ll:
                /**
                 * 设备未激活
                 */
                if(!Config.getInstance(getContext()).isSigned() && Config.getInstance(getContext()).isConfigured()){
                    TipDialog dialog = new TipDialog.Builder(getContext())
                            .setTitle(getString(R.string.tip_title))
                            .setMessage(getString(R.string.tip_UnSign))
                            .setNegativeClickListener(getString(R.string.cancel), new TipDialog.OnClickListener() {
                                @Override
                                public void onClick(Dialog dialogInterface, int index, String label) {
                                    dialogInterface.dismiss();
                                }
                            })
                            .setPositiveClickListener(getString(R.string.tip_yes), new TipDialog.OnClickListener() {
                                @Override
                                public void onClick(Dialog dialogInterface, int index, String label) {
                                    dialogInterface.dismiss();
                                    SignDialog.newInstance(getContext())
                                            .setNegativeClickListener(new SignDialog.OnClickListener() {
                                                @Override
                                                public void onClick(Dialog dialog) {
                                                    dialog.dismiss();
                                                }
                                            })
                                            .setSignCallback(new SignDialog.SignCallback() {
                                                @Override
                                                public void onFinish(boolean success, SignDialog dialogInterface, String message) {
                                                    if (success) {
                                                        checkRegCount();
                                                        //addSelectProgram();
                                                    }
                                                    dialogInterface.dismiss();
                                                }
                                            })
                                            .show();
                                    if (UsbService.getInstance().getSerialPorts() != null) {
                                        final Handler handler = new Handler();
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                while (SignDialog.getInstance() != null && !SignDialog.getInstance().waitSign()) {
                                                    //Log.e("signUsb",UsbService.getInstance().getSerialPorts().size() + "");
                                                    if (UsbService.getInstance().getSerialPorts() == null || UsbService.getInstance().getSerialPorts().size() <= 0) {
                                                        return;
                                                    }
                                                    handler.post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            List<UsbSerialPort> mUsbPorts = UsbService.getInstance().getSerialPorts();
                                                            if (mUsbPorts != null && mUsbPorts.size() > 0) {
                                                                for (int i = 0; i < mUsbPorts.size(); i++) {
                                                                    SignDialog.getInstance().signWithPort(mUsbPorts.get(i));
                                                                }
                                                            }
                                                        }
                                                    });
                                                }
                                                handler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        for (int i = 0; i < UsbService.getInstance().getSerialPorts().size(); i++) {
                                                            SignDialog.getInstance().signWithPort(UsbService.getInstance().getSerialPorts().get(i));
                                                        }
                                                    }
                                                });
                                            }
                                        }).start();

                                    }
                                }
                            })
                            .build();
                    dialog.show();

                    break;
                }

                checkRegCount();

                break;
            case R.id.menu_iv:
                if(getContext() instanceof MainActivity){
                    ((MainActivity)getActivity()).onFragSwitch(MainActivity.HOME_ID);
                }
                break;
            case R.id.refresh_Btn:
                if(!swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(true);
                    pullDownToRefresh();
                }
            default:
                break;
        }
    }

    /**
     * 检查可用次数
     */
    private void checkRegCount(){
        int setRegCount = Config.getInstance(getContext()).getSetRegCount();
        int RegCount = Config.getInstance(getContext()).getRegCount();
        if(setRegCount <= RegCount){
            //使用次数已用完
            String tipStr = getResources().getString(R.string.regCount_1) + " "+ RegCount + " " +
                    getResources().getString(R.string.regCount_Over);
            TipDialog dialog = new TipDialog.Builder(getContext())
                    .setTitle(getResources().getString(R.string.tip_title))
                    .setMessage(tipStr)
                    .setPositiveClickListener(getResources().getString(R.string.Sure), new TipDialog.OnClickListener() {
                        @Override
                        public void onClick(Dialog dialogInterface, int index, String label) {
                            dialogInterface.dismiss();

                        }
                    })
                    .requestSystemAlert(true)
                    .build();
            dialog.show();
        }
        else if(setRegCount > RegCount && (setRegCount - RegCount) <= Config.TIP_REGCOUNT){
            //使用次数到达提示数
            String tipStr = getResources().getString(R.string.regCount_1) + " "+ RegCount + " " +
                    getResources().getString(R.string.regCount_2) + " " +
                    (setRegCount - RegCount) + " " +
                    getResources().getString(R.string.regCount_msg);
            TipDialog dialog = new TipDialog.Builder(getContext())
                    .setTitle(getResources().getString(R.string.tip_title))
                    .setMessage(tipStr)
                    .setPositiveClickListener(getResources().getString(R.string.Sure), new TipDialog.OnClickListener() {
                        @Override
                        public void onClick(Dialog dialogInterface, int index, String label) {
                            dialogInterface.dismiss();
                            addSelectProgram();
                        }
                    })
                    .requestSystemAlert(true)
                    .build();
            dialog.show();

        }
        else{
            addSelectProgram();
        }
    }


    /**
     * 添加下载任务
     */
    private void addSelectProgram(){

        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    ArrayList<String> updateUrlList = new ArrayList<>();

                    Intent intent = new Intent(getActivity(), DownloadTaskActivity.class);

                    for (int i = 0; i < adapter.getCount(); i++) {
                        if (!adapter.getItem(i).isChecked()) {
                            continue;
                        }

                        final UpdateDB updateDB = adapter.getItem(i);
                        if(updateDB != null) {
                            final String url = updateDB.getProgramName();//adapter.getSelectURL(adapter.getItem(i).getProgramName());
                            if(updateDB.isDelete()){
                                /**
                                 * 删除程序
                                 */
                                FileUtil.deleteProgram(getContext(),url);
                                adapter.remove(updateDB);
                                i--;
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getContext(),getString(R.string.delete_select) + url + getString(R.string.success),Toast.LENGTH_SHORT).show();
                                        mHandler.sendEmptyMessage(MSG_REFRESH);
                                    }
                                });

                            }
                            else {
                                updateUrlList.add(url);
                            }
                        }
                    }

                    if(updateUrlList.size() > 0) {
                        Gson gson = new Gson();
                        String listStr = gson.toJson(updateUrlList);
                        listStr = listStr.replace("\\", "/");
                        listStr = listStr.replace("//", "/");
                        intent.putExtra(DownloadTaskActivity.kURLList, listStr);
                        startActivityForResult(intent, RequestCodeConstant.CODE_ONE_KEY_ACTIVITY);
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.e("Upgrade", "Hidden-" + hidden);

        if (hidden) {
            if (mDialog != null) {
                mDialog.dismiss();
            }
            mDialog = null;
        }
    }

    @Override
    public boolean onBackPressed() {
        if(getContext() instanceof MainActivity){
            ((MainActivity)getActivity()).onFragSwitch(MainActivity.HOME_ID);
        }
        return true;
    }


    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REFRESH:
                    isLoading = false;
                    int authCount = adapter.getAuthenCount();
                    update_num.setText(authCount+"");
                    if (adapter.getSelectCount() == 0){
                        menu_title.setText(R.string.main_upgrade);
                    }else {
                        int delete = 0;
                        int count = 0;
                        for (int i = 0; i < adapter.getCount() ; i++) {
                            if (adapter.getItem(i).isChecked()){
                                if(adapter.getItem(i).isDelete()){
                                    delete++;
                                }
                                else {
                                    count++;
                                }
                            }
                        }
                        String text = "";
                        if(count != 0){
                            text =  getResources().getString(R.string.upgrade_select) + " "+ count + " " + getResources().getString(R.string.upgrade_select_num);
                            if(delete != 0){
                                text += " , ";
                            }
                        }
                        if(delete != 0){
                            text += getString(R.string.delete_select) + " " + delete + "" + getResources().getString(R.string.upgrade_select_num);
                        }
                        menu_title.setText(text);
                    }

                    if (swipeRefreshLayout.isRefreshing()){
                        swipeRefreshLayout.setRefreshing(false);
                    }

                    if (mDialog != null && mDialog.isShowing()) {
                        mDialog.dismiss();
                    }
                    adapter.notifyDataSetChanged();
                    break;
                case MSG_COMPARE:
                    compareVersion();
                    break;
                case MSG_FINDVERSION:
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            versions.clear();
                            String path = FileUtil.getProgramRoot(getContext());
                            FileUtil.getPathBinVer(versions, path);

                            sendEmptyMessage(MSG_COMPARE);
                        }
                    }).start();
                    break;
                case MSG_SHOW_LOADING_DIALOG:
                    if (mDialog != null) {
                        mDialog.dismiss();
                    }
                    mDialog = null;
                    mDialog = new LoadDialog.Builder(getContext())
                            .setTitle(getResources().getString(R.string.tip_message_get)).build();
                    mDialog.show();
                    break;
                default:
                    break;
            }

        }
    };


}
