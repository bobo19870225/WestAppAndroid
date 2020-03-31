package com.west.develop.westapp.UI.Activity;

import android.view.View;
import android.widget.TextView;

import com.west.develop.westapp.UI.base.BaseActivity;
import com.west.develop.westapp.Tools.Utils.ReportUntil;
import com.west.develop.westapp.R;

public class DetailsReportActivity extends BaseActivity {

    private TextView back;
    private TextView title;

    private TextView reportName ;
    private TextView unsn ;
    private TextView hardType ;
    private TextView function ;
    private TextView fault ;

    private String functions = "";

    @Override
    protected View getContentView() {
        return getLayoutInflater().inflate(R.layout.activity_details_report,null);
    }

    @Override
    protected void initView() {
        back = (TextView) findViewById(R.id.car_back);
        title = (TextView) findViewById(R.id.car_title);
        reportName = (TextView) findViewById(R.id.detail_name);
        unsn = (TextView) findViewById(R.id.detail_unsn);
        hardType = (TextView) findViewById(R.id.detail_hardType);
        function = (TextView) findViewById(R.id.detail_function);
        fault = (TextView) findViewById(R.id.detail_fault);
    }

    @Override
    protected void initData() {
        title.setText(R.string.report_details);

        String name = getIntent().getStringExtra("name");
        String str =  ReportUntil.readReports(DetailsReportActivity.this,name);
        if(str != null) {
            String[] strs = str.split("\n");

            for (int i = 0; i < strs.length; i++) {
                if (strs[i].contains(ReportUntil.REPORT_FILENAME)) {
                    reportName.setText(strs[i].substring(strs[i].indexOf(ReportUntil.REPORT_FILENAME) + ReportUntil.REPORT_FILENAME.length()));
                    continue;
                }
                if (strs[i].contains(ReportUntil.REPORT_UNSN)) {
                    unsn.setText(strs[i].substring(strs[i].indexOf(ReportUntil.REPORT_UNSN) + ReportUntil.REPORT_UNSN.length()));
                    continue;
                }
                if (strs[i].contains(ReportUntil.REPORT_HARDTYPE)) {
                    hardType.setText(strs[i].substring(strs[i].indexOf(ReportUntil.REPORT_HARDTYPE) + ReportUntil.REPORT_HARDTYPE.length()));
                    continue;
                }

                if (strs[i].contains(ReportUntil.REPORT_FUNCTION)) {
                    functions += strs[i].substring(strs[i].indexOf(ReportUntil.REPORT_FUNCTION) + ReportUntil.REPORT_FUNCTION.length());
                }
                functions += "\n";
            }
            function.setText(functions);
            fault.setText(str);
        }

    }

    @Override
    protected void initListener() {
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
}
