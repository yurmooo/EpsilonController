package cc.dobot.crtcpdemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cc.dobot.crtcpdemo.client.StateMessageClient;
import cc.dobot.crtcpdemo.data.AlarmData;
import cc.dobot.crtcpdemo.message.constant.Robot;

public class CountActivity extends AppCompatActivity implements MainContract.View {

    private ImageButton drag_mode, btn_back;
    private Button get_pos_btn1, get_pos_btn2, get_pos_btn3;
    private TextView pos1, pos2, pos3;
    private Map<Integer, LinearLayout> taskViews = new HashMap<>();
    private int currentTaskIndex = 0;
    private double x1, y1, z1, rx1, ry1, rz1;
    private double x2, y2, z2, rx2, ry2, rz2;
    private double x3, y3, z3, rx3, ry3, rz3;
    private boolean isDragMode = false;
    private LayoutInflater inflater;
    private TaskViewModel viewModel;
    private MainContract.Present present;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_count);

        viewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()))
                .get(TaskViewModel.class);
        restorePositions();
        inflater = LayoutInflater.from(this);
        TabLayout tabLayout = findViewById(R.id.task_tabs);
        FrameLayout taskContainer = findViewById(R.id.task_container);

        // Создаём 3 вкладки
        for (int i = 1; i <= 3; i++) {
            tabLayout.addTab(tabLayout.newTab().setText("Задача " + i));
        }

        // Инициализируем первую задачу
        LinearLayout firstTaskLayout = new LinearLayout(this);
        firstTaskLayout.setOrientation(LinearLayout.VERTICAL);
        taskViews.put(0, firstTaskLayout);
        taskContainer.addView(firstTaskLayout);

        // Обработка вкладок
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int newIndex = tab.getPosition();
                switchTaskView(newIndex);
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        initView();
        initListeners();
        present = new MainPresent(this);
    }

    private void initView() {
        drag_mode = findViewById(R.id.drag_mode);
        btn_back = findViewById(R.id.btn_back);

        get_pos_btn1 = findViewById(R.id.get_pos_btn1);
        get_pos_btn2 = findViewById(R.id.get_pos_btn2);
        get_pos_btn3 = findViewById(R.id.get_pos_btn3);

        pos1 = findViewById(R.id.pos1);
        pos2 = findViewById(R.id.pos2);
        pos3 = findViewById(R.id.pos3);
    }

    private void initListeners() {
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Сохраняем позиции перед переходом
                savePositions();

                Intent intent = new Intent(CountActivity.this, MainActivityEpsilon.class);
                startActivity(intent);
            }
        });

        drag_mode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isDragMode = !isDragMode;
                if (present != null) {
                    present.setDragModeCommand(isDragMode);
                }

                resetDragModeIcon(isDragMode);

                Toast.makeText(CountActivity.this,
                        isDragMode ? "Режим Drag включён" : "Режим Drag выключен",
                        Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.add_block_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View block = inflater.inflate(R.layout.task_element, null);
                taskViews.get(currentTaskIndex).addView(block);
                viewModel.addElementType(currentTaskIndex, R.layout.task_element);
            }
        });

        get_pos_btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double[] coords = StateMessageClient.getInstance().getState().getToolVectorActual();
                if (coords != null && coords.length >= 6) {
                    x1 = coords[0];
                    y1 = coords[1];
                    z1 = coords[2];
                    rx1 = coords[3];
                    ry1 = coords[4];
                    rz1 = coords[5];
                    String text = String.format("X=%.4f, Y=%.4f\n Z=%.4f, Rx=%.4f\n Ry=%.4f, Rz=%.4f", x1, y1, z1, rx1, ry1, rz1);
                    pos1.setText(text);
                } else {
                    pos1.setText("Данные недоступны");
                }
            }
        });

        get_pos_btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double[] coords = StateMessageClient.getInstance().getState().getToolVectorActual();
                if (coords != null && coords.length >= 6) {
                    x2 = coords[0];
                    y2 = coords[1];
                    z2 = coords[2];
                    rx2 = coords[3];
                    ry2 = coords[4];
                    rz2 = coords[5];
                    String text = String.format("X=%.4f, Y=%.4f\n Z=%.4f, Rx=%.4f\n Ry=%.4f, Rz=%.4f", x2, y2, z2, rx2, ry2, rz2);
                    pos2.setText(text);
                } else {
                    pos2.setText("Данные недоступны");
                }
            }
        });

        get_pos_btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double[] coords = StateMessageClient.getInstance().getState().getToolVectorActual();
                if (coords != null && coords.length >= 6) {
                    x3 = coords[0];
                    y3 = coords[1];
                    z3 = coords[2];
                    rx3 = coords[3];
                    ry3 = coords[4];
                    rz3 = coords[5];
                    String text = String.format("X=%.4f, Y=%.4f\n Z=%.4f, Rx=%.4f\n Ry=%.4f, Rz=%.4f", x3, y3, z3, rx3, ry3, rz3);
                    pos3.setText(text);
                } else {
                    pos3.setText("Данные недоступны");
                }
            }
        });
    }

    private void savePositions() {
        if (pos1.getText() != null) {
            viewModel.savePosition(1, pos1.getText().toString());
        }
        if (pos2.getText() != null) {
            viewModel.savePosition(2, pos2.getText().toString());
        }
        if (pos3.getText() != null) {
            viewModel.savePosition(3, pos3.getText().toString());
        }
    }

    private void restorePositions() {
        String pos1Data = viewModel.getPosition(1);
        if (pos1Data != null) {
            pos1.setText(pos1Data);
        }

        String pos2Data = viewModel.getPosition(2);
        if (pos2Data != null) {
            pos2.setText(pos2Data);
        }

        String pos3Data = viewModel.getPosition(3);
        if (pos3Data != null) {
            pos3.setText(pos3Data);
        }
    }

    private void switchTaskView(int newIndex) {
        FrameLayout container = findViewById(R.id.task_container);
        container.removeAllViews();

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        taskViews.put(newIndex, layout);

        // Восстановление сохранённых элементов с правильными типами
        List<Integer> elementTypes = viewModel.getElementTypes(newIndex);
        if (elementTypes != null) {
            for (Integer type : elementTypes) {
                View block = inflater.inflate(type, null); // Используйте сохранённый тип
                layout.addView(block);
            }
        }

        container.addView(layout);
        currentTaskIndex = newIndex;
    }

    @Override
    protected void onDestroy() {
        savePositions();
        super.onDestroy();
    }

    // Реализация MainContract.View (можно оставить пустыми)
    @Override public void refreshConnectionState(boolean isConnected) {}
    @Override public void refreshPowerState(boolean isPowerOn) {}
    @Override public void refreshEnableState(boolean isEnable) {}
    @Override public void resetDragModeIcon(boolean isDrag) {}
    @Override public void refreshSpeedScaling(int speedScaling) {}
    @Override public void refreshRobotMode(Robot.Mode mode) {}
    @Override public void refreshDI(byte[] DI) {}
    @Override public void refreshDO(byte[] DO) {}
    @Override public void refreshQActual(double[] getqActual) {}
    @Override public void refreshToolVectorActual(double[] toolVectorActual) {}
    @Override public void refreshProgramState(int programState) {}
    @Override public void refreshErrorList(String errorInfo) {}
    @Override public void refreshLogList(boolean isSend, String log) {}
    @Override public void refreshAlarmList(List<AlarmData> dataList) {}
}