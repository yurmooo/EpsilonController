package cc.dobot.crtcpdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;

import com.google.android.material.tabs.TabLayout;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import cc.dobot.crtcpdemo.data.AlarmData;
import cc.dobot.crtcpdemo.message.constant.Robot;

public class MainActivityEpsilon extends AppCompatActivity implements MainContract.View {
// TODO() Если будут ошибки, вернуть , View.OnTouchListener
    MainActivity.PermissionListener mListener;
    public static final String[] permissionArrays = new String[]{
            Manifest.permission.INTERNET,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    Handler handler=new Handler();
    Button[] userButtons = new Button[6];
    TextView[] userTexts = new TextView[6];
    Button[] toolButtons = new Button[6];
    EditText ipAddressEdit, dashPortEdit, movePortEdit, feedBackPortEdit;
    Button saveSettingsBtn;
    ImageButton enableRobot, powerBTN, resetRobot, emergencyStop, error_btn, moreBTN;
    ImageButton settings, script, block_script, log_act, drag_mode, closeButton;
    Button connectRobot, clearAlarm, speedRatioBTN;
    EditText speedRatioEdit;
    Button getPosBTN;
    TextView DIText, DOText;
    TabLayout jogMoveTab;
    Button j1_btn, j2_btn, j3_btn, j4_btn, j5_btn, j6_btn;
//    Button jogPlusBtn[];
//    Button jogMinusBtn[];
    TextView jogText[];
    RelativeLayout leftPanel;
    LinearLayout centerPanel, rightPanel, scriptPanel, dropdownPanel;
    ImageButton collapseButton, expandButton, plus_btn, minus_btn;
    ImageView connection_icon;
    TextView[] toolTexts;
    private int selectedToolAxis = -1;
    private LinearLayout logPanel;
    private ImageButton closeLogPanelButton;
    private boolean isLogPanelVisible = false;
    private boolean isDragMode = false;
    private ListView logListView;
    private ArrayAdapter<String> logListAdapter;
    private ArrayList<String> logList = new ArrayList<>();
    private Toast singleToast;
    MainContract.Present present;
    private List<String> errorList = new ArrayList<>();
//    private TextItemAdapter errorListAdapter;
    private int selectedAxis = -1;
    private int selectedToolCoordAxis = -1;
    private boolean isPanelVisible = true;
    private boolean isSettingsPanelVisible = false;
    private int leftPanelWidth;
    private String currentIP;
    private int dashPort;
    private int movePort;
    private int feedBackPort;
    private Button[] optionButtons = new Button[16];
    private boolean[] digitalOutputStatus = new boolean[16]; // по умолчанию false (выключены)
    private void changeViewStats(boolean b) {
        enableRobot.setEnabled(b);
        resetRobot.setEnabled(b);
        clearAlarm.setEnabled(b);
        speedRatioBTN.setEnabled(b);
        speedRatioEdit.setEnabled(b);
        getPosBTN.setEnabled(b);
        error_btn.setEnabled(b);
        moreBTN.setEnabled(b);
        resetRobot.setEnabled(b);
        emergencyStop.setEnabled(b);
//        settings.setEnabled(b);
        script.setEnabled(b);
        block_script.setEnabled(b);
        log_act.setEnabled(b);
        drag_mode.setEnabled(b);
        plus_btn.setEnabled(b);
        minus_btn.setEnabled(b); //TODO() можно сделать оповещение о просьбе подключиться к роботу

        // TabLayout jogMoveTab;
//        for (Button btn:jogMinusBtn)
//            btn.setEnabled(b);
//        for (Button btn:jogPlusBtn)
//            btn.setEnabled(b);

//        findViewById(R.id.button_clear_error_info_list).setEnabled(b);
    }

    private void showSingleToast(String message) {
        if (singleToast != null) {
            singleToast.cancel();
        }
        singleToast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        singleToast.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_epsilon);
        requestRuntimePermission(permissionArrays, new MainActivity.PermissionListener() {
            @Override
            public void onGranted() {
                /*System.out.println("on permission granted");*/
            }

            @Override
            public void onDenied(List<String> deniedPermission) {
                new AlertDialog.Builder(MainActivityEpsilon.this).setCancelable(false)
                        .setTitle(R.string.notice_permission)
                        .setMessage(R.string.permission_text_camera)
                        .setPositiveButton(R.string.exit_app, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                System.exit(0);
                            }
                        }).show();
            }
        });

        scriptPanel = findViewById(R.id.script_panel);
        closeButton = findViewById(R.id.close_script_panel);

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideScriptPanel();
            }
        });

        View rootView = findViewById(android.R.id.content);
        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (scriptPanel.getVisibility() == View.VISIBLE) {
                    hideScriptPanel();
                }
            }
        });

        present = new MainPresent(this);
        initView();
        changeViewStats(false);

        if (present.isConnected()) {
            refreshEnableState(present.isEnable());
        }
    }

    private void initView() {
        connectRobot = findViewById(R.id.button_connect_robot);
        connectRobot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ip = ipAddressEdit.getText().toString();
                if (!isValidIpAddress(ip)) {
                    showSingleToast(
                            "Неверный формат IP-адреса");
                    return;
                }
                currentIP = ip;

                try {
                    dashPort = Integer.parseInt(dashPortEdit.getText().toString());
                    movePort = Integer.parseInt(movePortEdit.getText().toString());
                    feedBackPort = Integer.parseInt(feedBackPortEdit.getText().toString());
                } catch (NumberFormatException e) {
                    showSingleToast(
                            "Неверный формат порта");
                    return;
                }

                Log.d("ConnectionParams", "IP: " + currentIP +
                        ", dashPort: " + dashPort +
                        ", movePort: " + movePort +
                        ", feedBackPort: " + feedBackPort);

                if (present.isConnected()) {
                    present.disconnectRobot();
                    changeViewStats(false);
                    refreshEnableState(false);
                } else {
                    present.connectRobot(currentIP, dashPort, movePort, feedBackPort);
                    changeViewStats(true);
                }
            }
        });

        enableRobot = findViewById(R.id.button_change_enable);
        enableRobot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                present.setRobotEnable(!present.isEnable());
            }
        });

        resetRobot=findViewById(R.id.button_reset_robot);
        resetRobot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                present.resetRobot();
            }
        });

        logListView = findViewById(R.id.log_list);

        logListAdapter = new ArrayAdapter<>(
                this,
                R.layout.item_log,
                R.id.log_item_text,
                logList
        );
        logListView.setAdapter(logListAdapter);

        logListView.setClickable(false);
        logListView.setFocusable(false);

        connection_icon = findViewById(R.id.connection_icon);
        dropdownPanel = findViewById(R.id.dropdown_panel);

        moreBTN = findViewById(R.id.more_button);
        moreBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dropdownPanel.getVisibility() == View.VISIBLE) {
                    dropdownPanel.setVisibility(View.GONE);
                } else {
                    dropdownPanel.setVisibility(View.VISIBLE);
                }
            }
        });

//        powerBTN = findViewById(R.id.button_change_power);
//        powerBTN.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                present.setRobotPower(true);
//            }
//        });

        speedRatioEdit = findViewById(R.id.edit_speed_ratio);
        speedRatioBTN = findViewById(R.id.button_speed_ratio);
        speedRatioBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    present.setSpeedRatio(Integer.parseInt(speedRatioEdit.getText().toString()));
                } catch (Exception e) {
                    present.setSpeedRatio(1);
                }

            }
        });

        error_btn = findViewById(R.id.error_btn);
        error_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivityEpsilon.this, ErrorActivity.class);
                intent.putStringArrayListExtra("Ошибки:", new ArrayList<>(errorList));
                startActivity(intent);
            }
        });

        clearAlarm = findViewById(R.id.button_clear_alarm);
        clearAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                present.clearAlarm();
            }
        });

        emergencyStop = findViewById(R.id.button_emergency_stop);
        emergencyStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                present.emergencyStop();
            }
        });

        scriptPanel = findViewById(R.id.script_panel);

        settings = findViewById(R.id.settings);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleSettingsPanel();
            }
        });

        ipAddressEdit = findViewById(R.id.ip_address_edit);
        ipAddressEdit.setText("192.168.0.21");
        ipAddressEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String ip = s.toString();
                if (!isValidIpAddress(ip)) {
                    ipAddressEdit.setError("Неверный формат IP-адреса");
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            private boolean isValidIpAddress(String ip) {
                String[] parts = ip.split("\\.");
                if (parts.length != 4) return false;

                try {
                    for (String part : parts) {
                        int num = Integer.parseInt(part);
                        if (num < 0 || num > 255) return false;
                    }
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        });

        dashPortEdit = findViewById(R.id.dash_port_edit);
        dashPortEdit.setText("29999");
        movePortEdit = findViewById(R.id.move_port_edit);
        movePortEdit.setText("30003");
        feedBackPortEdit = findViewById(R.id.feedback_port_edit);
        feedBackPortEdit.setText("30004");

        currentIP = "192.168.0.21";
        dashPort = 29999;
        movePort = 30003;
        feedBackPort = 30004;

        saveSettingsBtn = findViewById(R.id.save_settings_btn);
        saveSettingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newIp = ipAddressEdit.getText().toString();
                if (isValidIpAddress(newIp)) {
                    currentIP = newIp;
                } else {
                    showSingleToast(
                            "Неверный формат IP-адреса");
                    return;
                }

                try {
                    dashPort = Integer.parseInt(dashPortEdit.getText().toString());
                    movePort = Integer.parseInt(movePortEdit.getText().toString());
                    feedBackPort = Integer.parseInt(feedBackPortEdit.getText().toString());

                    showSingleToast(
                            "Настройки сохранены");
                } catch (NumberFormatException e) {
                    showSingleToast(
                            "Неверный формат порта");
                }
            }
        });

        script = findViewById(R.id.script);
        script.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSingleToast(
                        "Функция в разработке");
            }
        });

        block_script = findViewById(R.id.block_script);
        block_script.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivityEpsilon.this, CountActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        logPanel = findViewById(R.id.log_panel);
        closeLogPanelButton = findViewById(R.id.close_log_panel);

        log_act = findViewById(R.id.log_act);
        log_act.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogPanel();
            }
        });
        closeLogPanelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideLogPanel();
            }
        });

        drag_mode = findViewById(R.id.drag_mode);
        drag_mode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isDragMode = !isDragMode;
                present.setDragModeCommand(isDragMode);
                resetDragModeIcon(isDragMode);

                Toast.makeText(MainActivityEpsilon.this,
                        isDragMode ? "Режим Drag включён" : "Режим Drag выключен",
                        Toast.LENGTH_SHORT).show();
            }
        });

        getPosBTN = findViewById(R.id.get_pos_btn);
        getPosBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double toolVectorActual[] = present.getCurrentCoordinate();
                NumberFormat nf = NumberFormat.getInstance();
                nf.setGroupingUsed(false);
                int i = 0;
                for (double data : toolVectorActual) {
                    i++;
                    data = (double) (Math.round(data * 10000)) / 10000;
                    toolVectorActual[i - 1] = data;
                    jogText[i - 1].setText(nf.format(data));
                }
            }
        });

        DIText = findViewById(R.id.digital_input_text);
        DOText = findViewById(R.id.digital_output_text);

        findViewById(R.id.j1_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedAxis = 0;
            }
        });
        findViewById(R.id.j2_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedAxis = 1;
            }
        });
        findViewById(R.id.j3_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedAxis = 2;
            }
        });
        findViewById(R.id.j4_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedAxis = 3;
            }
        });
        findViewById(R.id.j5_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedAxis = 4;
            }
        });
        findViewById(R.id.j6_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedAxis = 5;
            }
        });

        plus_btn = findViewById(R.id.plus_btn);
        findViewById(R.id.plus_btn).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int tabIndex = jogMoveTab.getSelectedTabPosition();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (tabIndex == 0) { // Общие
                            if (selectedAxis >= 0) {
                                present.setJogMove(false, selectedAxis);
                            } else {
                                showSingleToast(
                                        "Сначала выберите ось");
                            }
                        } else if (tabIndex == 1) { // Пользователь
                            if (selectedToolAxis >= 0) {
                                present.setJogMove(true, selectedToolAxis);
                            } else {
                                showSingleToast(
                                        "Сначала выберите ось пользователя");
                            }
                        } else if (tabIndex == 2) { // Инструмент
                            if (selectedToolAxis >= 0) {
                                present.setToolJogMove(selectedToolAxis, true);
                            } else {
                                showSingleToast(
                                        "Сначала выберите ось инструмента");
                            }
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                        present.stopMove();
                        return true;
                }
                return false;
            }
        });

        minus_btn = findViewById(R.id.minus_btn);
        findViewById(R.id.minus_btn).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int tabIndex = jogMoveTab.getSelectedTabPosition();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (tabIndex == 0) {
                            if (selectedAxis >= 0) {
                                present.setJogMove(false, selectedAxis + 6);
                            } else {
                                showSingleToast(
                                        "Сначала выберите ось");
                            }
                        } else if (tabIndex == 1) {
                            if (selectedToolAxis >= 0) {
                                present.setJogMove(true, selectedToolAxis + 6);
                            } else {
                                showSingleToast(
                                        "Сначала выберите ось пользователя");
                            }
                        } else if (tabIndex == 2) {
                            if (selectedToolAxis >= 0) {
                                present.setToolJogMove(selectedToolAxis, false);
                            } else {
                                showSingleToast(
                                        "Сначала выберите ось инструмента");
                            }
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                        present.stopMove();
                        return true;
                }
                return false;
            }
        });

        View.OnTouchListener axisTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (v.isSelected()) {
                        v.setSelected(false);
                        selectedAxis = -1;
                        return true;
                    }

                    for (int i = 1; i <= 6; i++) {
                        int resId = getResources().getIdentifier("j" + i + "_btn", "id", getPackageName());
                        findViewById(resId).setSelected(false);
                    }

                    v.setSelected(true);

                    switch (v.getId()) {
                        case R.id.j1_btn:
                            selectedAxis = 0;
                            break;
                        case R.id.j2_btn:
                            selectedAxis = 1;
                            break;
                        case R.id.j3_btn:
                            selectedAxis = 2;
                            break;
                        case R.id.j4_btn:
                            selectedAxis = 3;
                            break;
                        case R.id.j5_btn:
                            selectedAxis = 4;
                            break;
                        case R.id.j6_btn:
                            selectedAxis = 5;
                            break;
                        default:
                            return false;
                    }
                    return true;
                }
                return false;
            }
        };

        findViewById(R.id.j1_btn).setOnTouchListener(axisTouchListener);
        findViewById(R.id.j2_btn).setOnTouchListener(axisTouchListener);
        findViewById(R.id.j3_btn).setOnTouchListener(axisTouchListener);
        findViewById(R.id.j4_btn).setOnTouchListener(axisTouchListener);
        findViewById(R.id.j5_btn).setOnTouchListener(axisTouchListener);
        findViewById(R.id.j6_btn).setOnTouchListener(axisTouchListener);

        jogText = new TextView[6];
        jogText[0] = findViewById(R.id.jog_j1_text);
        jogText[1] = findViewById(R.id.jog_j2_text);
        jogText[2] = findViewById(R.id.jog_j3_text);
        jogText[3] = findViewById(R.id.jog_j4_text);
        jogText[4] = findViewById(R.id.jog_j5_text);
        jogText[5] = findViewById(R.id.jog_j6_text);

        j1_btn = findViewById(R.id.j1_btn);
        j2_btn = findViewById(R.id.j2_btn);
        j3_btn = findViewById(R.id.j3_btn);
        j4_btn = findViewById(R.id.j4_btn);
        j5_btn = findViewById(R.id.j5_btn);
        j6_btn = findViewById(R.id.j6_btn);

        final Button[] jointButtons = new Button[] {
                j1_btn, j2_btn, j3_btn, j4_btn, j5_btn, j6_btn
        };

        toolTexts = new TextView[6];
        toolTexts[0] = findViewById(R.id.tool_x_text);
        toolTexts[1] = findViewById(R.id.tool_y_text);
        toolTexts[2] = findViewById(R.id.tool_z_text);
        toolTexts[3] = findViewById(R.id.tool_rx_text);
        toolTexts[4] = findViewById(R.id.tool_ry_text);
        toolTexts[5] = findViewById(R.id.tool_rz_text);

        // Инициализация элементов пользовательских координат
        userButtons[0] = findViewById(R.id.x_user_btn);
        userButtons[1] = findViewById(R.id.y_user_btn);
        userButtons[2] = findViewById(R.id.z_user_btn);
        userButtons[3] = findViewById(R.id.rx_user_btn);
        userButtons[4] = findViewById(R.id.ry_user_btn);
        userButtons[5] = findViewById(R.id.rz_user_btn);

        userTexts[0] = findViewById(R.id.user_x_text);
        userTexts[1] = findViewById(R.id.user_y_text);
        userTexts[2] = findViewById(R.id.user_z_text);
        userTexts[3] = findViewById(R.id.user_rx_text);
        userTexts[4] = findViewById(R.id.user_ry_text);
        userTexts[5] = findViewById(R.id.user_rz_text);

        // Инициализация кнопок инструмента (они уже есть как toolTexts)
        toolButtons[0] = findViewById(R.id.x_btn);
        toolButtons[1] = findViewById(R.id.y_btn);
        toolButtons[2] = findViewById(R.id.z_btn);
        toolButtons[3] = findViewById(R.id.rx_btn);
        toolButtons[4] = findViewById(R.id.ry_btn);
        toolButtons[5] = findViewById(R.id.rz_btn);

        for (int i = 0; i < toolButtons.length; i++) {
            final int index = i;
            toolButtons[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedToolAxis = index;

                    // Сброс выделения
                    for (Button btn : toolButtons) {
                        btn.setSelected(false);
                    }

                    // Выделение текущей
                    v.setSelected(true);
                }
            });
        }

        jogMoveTab=findViewById(R.id.jog_move_tab);
        jogMoveTab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                switch (position) {
                    case 0: // Общие координаты
                        findViewById(R.id.axes_joints_group).setVisibility(View.VISIBLE);
                        findViewById(R.id.axes_user_group).setVisibility(View.GONE);
                        findViewById(R.id.axes_tool_group).setVisibility(View.GONE);
                        selectedAxis = -1;
                        selectedToolAxis = -1;
                        break;

                    case 1: // Пользовательские координаты
                        findViewById(R.id.axes_joints_group).setVisibility(View.GONE);
                        findViewById(R.id.axes_user_group).setVisibility(View.VISIBLE);
                        findViewById(R.id.axes_tool_group).setVisibility(View.GONE);
                        selectedAxis = -1;
                        selectedToolAxis = -1;
                        break;

                    case 2: // Координаты инструмента
                        findViewById(R.id.axes_joints_group).setVisibility(View.GONE);
                        findViewById(R.id.axes_user_group).setVisibility(View.GONE);
                        findViewById(R.id.axes_tool_group).setVisibility(View.VISIBLE);
                        selectedAxis = -1;
                        selectedToolAxis = -1;
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Снятие выделения с кнопок осей
                for (Button btn : jointButtons) {
                    btn.setSelected(false);
                }
                for (Button btn : userButtons) {
                    btn.setSelected(false);
                }
                for (Button btn : toolButtons) {
                    btn.setSelected(false);
                }

                // Сброс переменных выбранной оси
                selectedAxis = -1;
                selectedToolAxis = -1;
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Можно добавить логику при повторном выборе той же вкладки
            }
        });

        leftPanel = findViewById(R.id.left_panel_wrapper);
        leftPanel.post(new Runnable() {
            @Override
            public void run() {
                leftPanelWidth = leftPanel.getWidth();
            }
        });
        centerPanel = findViewById(R.id.center_panel);
        rightPanel = findViewById(R.id.right_panel);

        collapseButton = findViewById(R.id.btn_collapse_left);
        collapseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideLeftPanel();
            }
        });

        expandButton = findViewById(R.id.btn_expand_left);
        expandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLeftPanel();}
        });

        for (int i = 0; i < 16; i++) { //TODO() РАБОЧИЙ ВАРИНАТ ВЫХОДОВ
            int resId = getResources().getIdentifier("btn_option" + (i + 1), "id", getPackageName());
            optionButtons[i] = findViewById(resId);
            final int outputIndex = i; // 0–15

            optionButtons[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleOptionButtonClick(outputIndex); // передаём 0–15
                }
            });
        }

        for (int i = 0; i < userButtons.length; i++) {
            final int axisIndex = i;
            userButtons[i].setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        if (v.isSelected()) {
                            v.setSelected(false);
                            selectedToolAxis = -1;
                            return true;
                        }

                        for (Button btn : userButtons) {
                            btn.setSelected(false);
                        }

                        v.setSelected(true);
                        selectedToolAxis = axisIndex;
                        return true;
                    }
                    return false;
                }
            });
        }

        for (int i = 0; i < toolButtons.length; i++) {
            final int axisIndex = i;
            toolButtons[i].setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        if (v.isSelected()) {
                            v.setSelected(false);
                            selectedToolAxis = -1;
                            return true;
                        }

                        for (Button btn : toolButtons) {
                            btn.setSelected(false);
                        }

                        v.setSelected(true);
                        selectedToolAxis = axisIndex;
                        return true;
                    }
                    return false;
                }
            });
        }
    }

//    public interface PermissionListener {
//
//        void onGranted();
//
//        void onDenied(List<String> deniedPermission);
//TODO() Посмотреть для чего это
//    }

    public void requestRuntimePermission(String[] permissions, MainActivity.PermissionListener listener) {
        mListener = listener;
        List<String> permissionList = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(MainActivityEpsilon.this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(permission);
            }
        }
        if (!permissionList.isEmpty()) {
            ActivityCompat.requestPermissions(MainActivityEpsilon.this, permissionList.toArray(new String[permissionList.size()]), 1);
        } else {
            mListener.onGranted();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    List<String> deniedPermissions = new ArrayList<>();
                    for (int i = 0; i < grantResults.length; i++) {
                        int grantResult = grantResults[i];
                        String permission = permissions[i];
                        if (grantResult != PackageManager.PERMISSION_GRANTED && !permission.equals(Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS)) {
                            deniedPermissions.add(permission);
                        }
                    }
                    if (deniedPermissions.isEmpty()) {
                        mListener.onGranted();
                    } else {
                        mListener.onDenied(deniedPermissions);
                    }
                }
                break;
            case 1000:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
                        Toast.makeText(this, R.string.open_permission, Toast.LENGTH_LONG).show();
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void refreshConnectionState(boolean isConnected) {
        connectRobot.setText(isConnected ? R.string.disconnect_robot : R.string.connect_robot);
        if (isConnected) {
            connection_icon.setImageResource(R.drawable.ic_wifi_green);
        } else {
            connection_icon.setImageResource(R.drawable.ic_wifi);
        }
    }

    @Override
    public void refreshPowerState(boolean isPowerOn) {
//        if (isPowerOn) {
//            powerBTN.setImageResource(R.drawable.ic_power_button_red);
//        } else {
//            powerBTN.setImageResource(R.drawable.ic_power);
//        }
    }

    @Override
    public void refreshEnableState(boolean isEnable) {
        if (isEnable) {
            enableRobot.setImageResource(R.drawable.ic_pause);
        } else {
            enableRobot.setImageResource(R.drawable.ic_play);
        }
    }

    @Override
    public void refreshSpeedScaling(int speedScaling) {
//        speedScalingText.setText(getString(R.string.speed_ratio_) + String.valueOf(speedScaling));
    }

    @Override
    public void refreshRobotMode(Robot.Mode mode) {
//        robotModeText.setText(getString(R.string.robot_mode_text) + mode); TODO(Вывод состояния робота)
    }

    @Override
    public void refreshDI(byte[] DI) {
        String strDI =getString(R.string.digital_input_text) + String.valueOf(DI[0]) + " " +
                String.valueOf(DI[1]) + " " +
                String.valueOf(DI[2]) + " " +
                String.valueOf(DI[3]) + " " +
                String.valueOf(DI[4]) + " " +
                String.valueOf(DI[5]) + " " +
                String.valueOf(DI[6]) + " " +
                String.valueOf(DI[7]);
        DIText.setText(strDI);
    }

    @Override
    public void refreshDO(byte[] DO) {
        DOText.setText(
                getString(R.string.digital_outputs_text) + String.valueOf(DO[0]) + " " +
                        String.valueOf(DO[1]) + " " +
                        String.valueOf(DO[2]) + " " +
                        String.valueOf(DO[3]) + " " +
                        String.valueOf(DO[4]) + " " +
                        String.valueOf(DO[5]) + " " +
                        String.valueOf(DO[6]) + " " +
                        String.valueOf(DO[7]));
    }

    @Override
    public void refreshQActual(final double[] getqActual) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                NumberFormat nf = NumberFormat.getInstance();
                nf.setGroupingUsed(false);
                nf.setMaximumFractionDigits(4);

                for (int i = 0; i < getqActual.length; i++) {
                    getqActual[i] = (double) (Math.round(getqActual[i] * 10000)) / 10000;
                    jogText[i].setText(nf.format(getqActual[i]));
                }
            }
        });
    }

    @Override
    public void refreshToolVectorActual(final double[] toolVectorActual) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                NumberFormat nf = NumberFormat.getInstance();
                nf.setGroupingUsed(false);
                nf.setMaximumFractionDigits(4);

                for (int i = 0; i < toolVectorActual.length; i++) {
                    toolVectorActual[i] = (double) (Math.round(toolVectorActual[i] * 10000)) / 10000;

                    // Обновляем текст для всех типов координат
                    if (toolTexts[i] != null) {
                        toolTexts[i].setText(nf.format(toolVectorActual[i]));
                    }
                    if (userTexts[i] != null) {
                        userTexts[i].setText(nf.format(toolVectorActual[i]));
                    }
                }
            }
        });
    }

    @Override
    public void refreshProgramState(int programState) {
        //   programStateText.setText("Program state:" + programState);
    }

    @Override
    public void refreshErrorList(String errorInfo) {
        errorList.add(errorInfo);
//        errorListAdapter.notifyDataSetChanged();
    }

    @Override
    public void refreshLogList(final boolean isSend, final String log) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                String direction = isSend ? getString(R.string.send_msg) : getString(R.string.receive_msg);
                logList.add(direction + log);
                logListAdapter.notifyDataSetChanged();

                // Автопрокрутка вниз
                logListView.setSelection(logListAdapter.getCount() - 1);
            }
        });
    };

    @Override
    public void refreshAlarmList(final List<AlarmData> dataList) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                errorList.clear();
                for (AlarmData data : dataList) {
                    errorList.add(data.toString());
                }
//                errorListAdapter.notifyDataSetChanged();
            }
        });
    }

    private String getAxisName(int index) {
        switch (index) {
            case 0: return "X";
            case 1: return "Y";
            case 2: return "Z";
            case 3: return "RX";
            case 4: return "RY";
            case 5: return "RZ";
            default: return "";
        }
    }

    // АНИМАЦИИ
    private void hideLeftPanel() {
        leftPanel.animate()
                .translationX(-leftPanelWidth)
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        updateWeights(0f, 2.7f, 0.25f);
                        leftPanel.setVisibility(View.GONE);
                        collapseButton.setVisibility(View.GONE);
                        expandButton.setVisibility(View.VISIBLE);
                        isPanelVisible = false;
                    }
                });
    }

    private void showLeftPanel() {
        updateWeights(0.3f, 2.7f, 0.25f);
        leftPanel.setVisibility(View.VISIBLE);
        collapseButton.setVisibility(View.VISIBLE);
        leftPanel.setTranslationX(-leftPanelWidth);

        leftPanel.animate()
                .translationX(0)
                .setDuration(300)
                .setListener(null);

        expandButton.setVisibility(View.GONE);
        isPanelVisible = true;
    }

    private void updateWeights(float leftWeight, float centerWeight, float rightWeight) {
        LinearLayout.LayoutParams leftParams = (LinearLayout.LayoutParams) leftPanel.getLayoutParams();
        LinearLayout.LayoutParams centerParams = (LinearLayout.LayoutParams) centerPanel.getLayoutParams();
        LinearLayout.LayoutParams rightParams = (LinearLayout.LayoutParams) rightPanel.getLayoutParams();

        leftParams.weight = leftWeight;
        centerParams.weight = centerWeight;
        rightParams.weight = rightWeight;

        leftPanel.setLayoutParams(leftParams);
        centerPanel.setLayoutParams(centerParams);
        rightPanel.setLayoutParams(rightParams);

        leftPanel.requestLayout();
        centerPanel.requestLayout();
        rightPanel.requestLayout();
    }

    private void toggleSettingsPanel() {
        final LinearLayout settingsPanel = findViewById(R.id.script_panel);
        final RelativeLayout leftPanel = findViewById(R.id.left_panel_wrapper);

        if (isSettingsPanelVisible) {
            // Анимация закрытия
            settingsPanel.animate()
                    .translationX(-settingsPanel.getWidth())
                    .setDuration(300)
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            settingsPanel.setVisibility(View.GONE);
                            showLeftPanel(); // Показываем left_panel после закрытия
                        }
                    });
        } else {
            hideLeftPanel(); // Скрываем left_panel перед открытием
            settingsPanel.setVisibility(View.VISIBLE);
            settingsPanel.bringToFront();

            // Анимация открытия
            settingsPanel.animate()
                    .translationX(0)
                    .setDuration(300);
        }

        isSettingsPanelVisible = !isSettingsPanelVisible;
    }

    private void hideScriptPanel() {
        final LinearLayout settingsPanel = findViewById(R.id.script_panel);
        final RelativeLayout leftPanel = findViewById(R.id.left_panel_wrapper);

        if (isSettingsPanelVisible) {
            // Анимация закрытия
            settingsPanel.animate()
                    .translationX(-settingsPanel.getWidth())
                    .setDuration(300)
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            settingsPanel.setVisibility(View.GONE);
                            showLeftPanel(); // Показываем left_panel после закрытия
                        }
                    });

            isSettingsPanelVisible = false;
        }
    }

    private boolean isValidIpAddress(String ip) {
        String[] parts = ip.split("\\.");
        if (parts.length != 4) return false;

        try {
            for (String part : parts) {
                int num = Integer.parseInt(part);
                if (num < 0 || num > 255) return false;
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public void resetDragModeIcon(boolean isDrag) {
        if (isDrag) {
            drag_mode.setImageResource(R.drawable.ic_dragred);
        } else {
            drag_mode.setImageResource(R.drawable.ic_drag);
        }
    }

    private void resetOptionButtonsColor() {
        for (Button btn : optionButtons) {
            if (btn != null) {
                btn.setBackgroundResource(R.drawable.button_option_selector);
                btn.setTextColor(getResources().getColor(android.R.color.black));
            }
        }
    }

    private void handleOptionButtonClick(int index) { //TODO() РАБОЧИЙ ВАРИНАТ ВЫХОДОВ
        int outputNumber = index + 1;

        if (outputNumber < 1 || outputNumber > 16) {
            showSingleToast("Недопустимый номер выхода");
            return;
        }

        // Переключаем состояние
        digitalOutputStatus[index] = !digitalOutputStatus[index];
        present.setDigitalOutput(outputNumber, digitalOutputStatus[index]);

        Button button = optionButtons[index];
        if (digitalOutputStatus[index]) {
            button.setBackgroundResource(R.drawable.button_option_selected);
            button.setTextColor(getResources().getColor(android.R.color.white));
            button.setText(outputNumber + ":1");
        } else {
            button.setBackgroundResource(R.drawable.button_option_default);
            button.setTextColor(getResources().getColor(android.R.color.black));
            button.setText(outputNumber + ":0");
        }
    }

    private void showLogPanel() {
        if (!isLogPanelVisible) {
            logPanel.setVisibility(View.VISIBLE);
            logPanel.animate()
                    .translationX(0)
                    .setDuration(300)
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            isLogPanelVisible = true;
                            hideLeftPanel();
                        }
                    });
        }
    }

    private void hideLogPanel() {
        if (isLogPanelVisible) {
            logPanel.animate()
                    .translationX(-logPanel.getWidth()) // смещение влево на ширину панели
                    .setDuration(300)
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            logPanel.setVisibility(View.GONE);
                            isLogPanelVisible = false;
                            showLeftPanel();
                        }
                    });
        }
    }
}