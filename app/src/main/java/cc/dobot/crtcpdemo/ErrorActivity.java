package cc.dobot.crtcpdemo;

import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import cc.dobot.crtcpdemo.adapter.TextItemAdapter;
import cc.dobot.crtcpdemo.data.AlarmData;

public class ErrorActivity extends MainActivityEpsilon {
    private RecyclerView errorRecyclerView;
    private TextItemAdapter errorListAdapter;
    private List<String> errorList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_errors);

        errorRecyclerView = findViewById(R.id.error_info_list);
        errorRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        if (getIntent() != null && getIntent().hasExtra("errorList")) {
            errorList = getIntent().getStringArrayListExtra("errorList");
        }
        errorListAdapter = new TextItemAdapter(errorList);
        errorRecyclerView.setAdapter(errorListAdapter);

        findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ErrorActivity.this, MainActivityEpsilon.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.button_clear_error_info_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                errorList.clear();
                errorListAdapter.notifyDataSetChanged();
                present.clearAlarm();
            }
        });
    }
}
