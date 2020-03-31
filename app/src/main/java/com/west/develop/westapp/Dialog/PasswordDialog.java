package com.west.develop.westapp.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.west.develop.westapp.Tools.Utils.HexUtil;
import com.west.develop.westapp.Tools.Diagnosis.KeyEvent;
import com.west.develop.westapp.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Develop0 on 2017/9/9.
 */

public class PasswordDialog extends Dialog implements View.OnClickListener {
    public static final int TYPE_PASS_NUMBER = 1;
    public static final int TYPE_PASS_TEXT = 2;
    public static final int TYPE_PASS_HEX = 3;
    public static final int TYPE_PASS = 4;
    public static final int TYPE_NUMBER = 5;

    private Context mContext;

    private TextView mMessage_TV;
    private TextView mCount_TV;
    private TextView mType_TV;
    private EditText mValue_ET;
    private TextView mTip_TV;
    private TextView mPass_TV;
    private TextView mBit_TV;
    private Button mREInput_BTN;
    private Button mCommit_BTN;

    private String mMessage;
    private int mPassCount;
    private int mType;
    private int mInputType; //往输入框中输入的是密码还是次数
    private String mTip_text;

    private ClickListener mClickListner;

    private PasswordDialog(Context context){
        super(context);
        mContext = context;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //设置 Dialog 布局
        setContentView(R.layout.dialog_password);
        //点击边缘无效
        setCanceledOnTouchOutside(false);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN|WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        setCancelable(false);
        initView();
        initData();
    }


    /**
     * 设置类型
     * 数字密码     字母数字组合密码
     * @param type
     */
    public void setType(int type){
        if(type == TYPE_PASS_TEXT || type == TYPE_PASS_NUMBER || type == TYPE_PASS_HEX){
            mType = type;
        }
    }

    /**
     * 设置 密码位数
     * @param count
     */
    public void setPasswordCount(int count){
        if(count >= 0) {
            this.mPassCount = count;
        }
    }

    /**
     * 设置输入的类型
     */
    public void setInputType(int mInputType) {
        if (mInputType == TYPE_PASS || mInputType == TYPE_NUMBER) {
            this.mInputType = mInputType;
        }
    }

    /**
     * 设置提示文字
     * @param mTip_text
     */
    public void setTip_text(String mTip_text) {
        this.mTip_text = mTip_text;
    }

    /**
     * 设置标题信息
     * @param message
     */
    public void setMessage(String message){
        mMessage = message;
    }

    private void initView(){
        mMessage_TV = (TextView)findViewById(R.id.input_Message_TV);
        mCount_TV = (TextView)findViewById(R.id.input_Count_TV);
        mType_TV = (TextView)findViewById(R.id.input_Type_TV);
        mValue_ET = (EditText)findViewById(R.id.input_Edit_ET);
        mTip_TV = (TextView) findViewById(R.id.input_please);
        mPass_TV = (TextView) findViewById(R.id.input_password_TV);
        mBit_TV = (TextView) findViewById(R.id.textView);
        mREInput_BTN = (Button)findViewById(R.id.input_ReInput_BTN);
        mCommit_BTN = (Button)findViewById(R.id.input_Commit_BTN);

        mREInput_BTN.setOnClickListener(this);
        mCommit_BTN.setOnClickListener(this);
    }

    private void initData(){
        mMessage_TV.setText(mMessage == null?"":mMessage);
        mCount_TV.setText("" + mPassCount);
        if (mTip_text != ""){
            mTip_TV.setText(mTip_text);
            mCount_TV.setVisibility(View.GONE);
            mPass_TV.setVisibility(View.GONE);
            mType_TV.setVisibility(View.GONE);
            mBit_TV.setVisibility(View.GONE);
            mValue_ET.setHint(mContext.getString(R.string.hint_input_times));
        }

        if(mType == TYPE_PASS_NUMBER){
            mType_TV.setText(mContext.getString(R.string.type_Number));
            mValue_ET.setInputType(InputType.TYPE_NUMBER_VARIATION_PASSWORD);
            //mValue_ET.setTransformationMethod(PasswordTransformationMethod.getInstance());
        }
        if(mType == TYPE_PASS_TEXT){
            mType_TV.setText(mContext.getString(R.string.type_Text));
            mValue_ET.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
           // mValue_ET.setTransformationMethod(PasswordTransformationMethod.getInstance());
        }
        mValue_ET.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);

        mValue_ET.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(mType == TYPE_PASS_HEX && !hasFocus) {
                    int selection = mValue_ET.getSelectionStart();
                    String str = mValue_ET.getText().toString();
                    if(selection % 3 == 1){
                        char inputC = str.charAt(selection);

                        if(inputC == ' '){
                            String prev = str.substring(0, selection - 1);
                            String end = str.substring(selection - 1, str.length());
                            str = prev + "0" + end;
                        }
                        else {
                            String prev = str.substring(0, selection - 1);
                            String end = str.substring(selection, str.length());
                            str = prev + "0" + inputC + " " + end;
                        }
                    }
                    if(selection % 3 == 2){
                        String prev = str.substring(0, selection);
                        String end = str.substring(selection, str.length());
                        str = prev + " " + end;
                    }
                    if(str.indexOf("  ") >= 0) {
                        str = str.replaceAll("  ", " ");
                    }
                    mValue_ET.setText(str);
                }
            }
        });

        mValue_ET.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    if(mType == TYPE_PASS_HEX){
                        int selection = mValue_ET.getSelectionStart();
                        String str = mValue_ET.getText().toString();
                        if(selection % 3 == 1){
                            String prev = str.substring(0, selection - 1);
                            String end = str.substring(selection - 1, str.length());
                            str = prev + "0" + end;
                        }
                        if(selection % 3 == 2){
                            String prev = str.substring(0, selection);
                            String end = str.substring(selection, str.length());
                            str = prev + " " + end;
                        }
                        if(str.indexOf("  ") >= 0) {
                            str = str.replaceAll("  ", " ");
                        }
                        mValue_ET.setText(str);

                    }
                }

                return false;
            }
        });


        mValue_ET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selection = mValue_ET.getSelectionStart();
                Log.e("EditSelection-Click",selection + "");

                if(mType == TYPE_PASS_HEX) {
                    if (selection % 3 == 1) {
                        selection++;
                        mValue_ET.setSelection(selection);
                    }

                }
            }
        });
        mValue_ET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String editable = mValue_ET.getText().toString();
                int sStart = mValue_ET.getSelectionStart();
                String regEx = "[^a-zA-Z0-9]";
                if (mType == TYPE_PASS_NUMBER) {
                    regEx = "[^0-9]";
                }
                if (mType == TYPE_PASS_HEX) {
                    regEx = "[^0-9A-F ]";
                }
                Pattern p = Pattern.compile(regEx);
                Matcher m = p.matcher(editable);
                String str = m.replaceAll("");    //删掉不是字母或数字的字符

                if (!editable.equals(str)) {
                    mValue_ET.setText(str);  //设置EditText的字符
                    mValue_ET.setSelection(start);
                    //mValue_ET.setSelection(str.length());
                    return;
                }

                int selection = mValue_ET.getSelectionStart();

                try {

                    if (mType == TYPE_PASS_HEX) {
                        if (before <= 0) {
                            char inputC = str.charAt(selection - 1);

                            if (selection % 3 == 0) {
                                String prev = str.substring(0, selection - 1);
                                String end = str.substring(selection - 1, str.length());
                                str = prev + " " + end;
                                selection++;
                                str = str.replaceAll("  ", " ");
                                mValue_ET.setText(str);
                            } else if (selection % 3 == 1) {
                                if (inputC == ' ') {
                                    String prev = str.substring(0, selection - 1);
                                    String end = str.substring(selection, str.length());
                                    str = prev + end;
                                    selection--;
                                    str = str.replaceAll("  ", " ");
                                    mValue_ET.setText(str);
                                } else {
                                    String prev = str.substring(0, selection);
                                    String end = str.substring(selection, str.length());
                                    str = prev + " " + end;
                                    str = str.replaceAll("  ", " ");
                                    mValue_ET.setText(str);
                                }
                            } else if (selection % 3 == 2) {
                                if (inputC == ' ') {
                                    String prev = str.substring(0, selection - 2);
                                    String end = str.substring(selection - 2, str.length());
                                    str = prev + "0" + end;
                                    selection++;
                                } else {
                                    String prev = str.substring(0, selection);
                                    String end = str.substring(selection, str.length());
                                    str = prev + " " + end;
                                    selection++;
                                }
                                str = str.replaceAll("  ", " ");
                                mValue_ET.setText(str);
                            }
                        }
                        mValue_ET.setSelection(selection);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void afterTextChanged(Editable s) {
                String str = s.toString();
                if(mPassCount > 0) {
                    if (str.length() > mPassCount) {
                        str = str.substring(0, mPassCount);
                        int selection = mValue_ET.getSelectionStart();
                        mValue_ET.setText(str);
                        if(selection >= mPassCount) {
                            selection = mPassCount;
                        }
                        mValue_ET.setSelection(selection);
                        /*int selection = mValue_ET.getSelectionStart();
                        */
                    }
                }
            }
        });
    }


    public void setClickListner(ClickListener listner){
        mClickListner = listner;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() != mValue_ET.getId()) {
            mValue_ET.clearFocus();
        }
        switch (v.getId()){
            case R.id.input_ReInput_BTN:
                mValue_ET.setText("");
                break;
            case R.id.input_Commit_BTN:
                onCommitClick();
                break;
            default:
                break;
        }
    }

    /**
     * 确认按钮 点击
     */
    public void onCommitClick(){
        String password = mValue_ET.getText().toString().trim();
        if (mType == TYPE_PASS_HEX){ //将输入的数值转换成十六进制，并且数值之间用空格隔开
            String[] bufStr_Array = password.split(" ");
            byte[] buffer = HexUtil.getBufferByArrayStr(bufStr_Array);
        }

        //位数与要求不符
        if(password.length() != mPassCount && mPassCount > 0){
            Toast.makeText(mContext,mContext.getString(R.string.length_unmatch),Toast.LENGTH_SHORT).show();
            return;
        }

        //监听获取输入的值
        if(mClickListner != null) {
            if (mInputType == TYPE_NUMBER) {//输入次数
                try {
                    if (Integer.valueOf(password) < 16777215) {
                        mClickListner.onClick(KeyEvent.KEY_ENTER, password);
                        dismiss();
                    } else {
                        mClickListner.onClick(KeyEvent.KEY_ENTER, password);
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    mClickListner.onClick(KeyEvent.KEY_ENTER, password);
                    return;
                }
            }
            else if (mInputType == TYPE_PASS){ //输入密码
                mClickListner.onClick(KeyEvent.KEY_ENTER, password);
            }
        }
        else {
            Toast.makeText(mContext,"Commit",Toast.LENGTH_SHORT).show();
            //KeyEvent.onPasswordInputFinish(password);
        }
        dismiss();
    }

    public static class Builder{
        PasswordDialog dialog ;
        public Builder(Context context){
            dialog = new PasswordDialog(context);
        }

        public Builder setType(int type){
            dialog.setType(type);
            return this;
        }

        public Builder setCount(int count){
            dialog.setPasswordCount(count);
            return this;
        }

        public Builder setMessage(String message){
            dialog.setMessage(message);
            return this;
        }

        public Builder setTip_text(String mTiptext){
            dialog.setTip_text(mTiptext);
            return this;
        }
        public Builder setInputType(int mInputType){
            dialog.setInputType(mInputType);
            return this;
        }

        public Builder setClickListener(ClickListener listener){
            dialog.setClickListner(listener);
            return this;
        }

        public PasswordDialog build(){
            //dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            return dialog;
        }
    }

    public interface ClickListener{
        void onClick(int keyValue, String inputValue);
    }
}
