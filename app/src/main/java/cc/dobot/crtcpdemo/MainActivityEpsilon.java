package cc.dobot.crtcpdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import cc.dobot.crtcpdemo.adapter.TextItemAdapter;
import cc.dobot.crtcpdemo.data.AlarmData;
import cc.dobot.crtcpdemo.message.constant.Robot;

public class MainActivityEpsilon extends AppCompatActivity implements MainContract.View, View.OnTouchListener {
    MainActivity.PermissionListener mListener;
    public static final String[] permissionArrays = new String[]{
            Manifest.permission.INTERNET,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    Handler handler=new Handler();

    String currentIP = "192.168.1.6";
    int dashPort = 29999;
    int movePort = 30003;
    int feedBackPort = 30004;
    ImageButton enableRobot, powerBTN, resetRobot, emergencyStop;
    Button connectRobot, clearAlarm, speedRatioBTN;
    EditText speedRatioEdit;
    Button getPosBTN;
    TextView DIText, DOText;
    TabLayout jogMoveTab;
    Button jogPlusBtn[];
    Button jogMinusBtn[];
    TextView jogText[];
    MainContract.Present present;

    private void changeViewStats(boolean b) {
        enableRobot.setEnabled(b);
        resetRobot.setEnabled(b);
        clearAlarm.setEnabled(b);
        speedRatioBTN.setEnabled(b);
        speedRatioEdit.setEnabled(b);
        getPosBTN.setEnabled(b);


        // TabLayout jogMoveTab;
        for (Button btn:jogMinusBtn)
            btn.setEnabled(b);
        for (Button btn:jogPlusBtn)
            btn.setEnabled(b);

//        findViewById(R.id.button_clear_error_info_list).setEnabled(b);
//TODO() Вернуться когда у меня будут куда-то выводиться ошибки робота
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
        present = new MainPresent(this);
        initView();
        changeViewStats(false);
    }

    private void initView() {
        connectRobot = findViewById(R.id.button_connect_robot);
        connectRobot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (present.isConnected()) {
                    present.disconnectRobot();
                    changeViewStats(false);
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

        powerBTN = findViewById(R.id.button_change_power);
        powerBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                present.setRobotPower(true);
            }
        });

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

        jogText = new TextView[6];
        jogText[0] = findViewById(R.id.jog_j1_text);
        jogText[1] = findViewById(R.id.jog_j2_text);
        jogText[2] = findViewById(R.id.jog_j3_text);
        jogText[3] = findViewById(R.id.jog_j4_text);
        jogText[4] = findViewById(R.id.jog_j5_text);
        jogText[5] = findViewById(R.id.jog_j6_text);

        jogPlusBtn = new Button[6];
        jogPlusBtn[0] = findViewById(R.id.jog_j1_plus_button);
        jogPlusBtn[1] = findViewById(R.id.jog_j2_plus_button);
        jogPlusBtn[2] = findViewById(R.id.jog_j3_plus_button);
        jogPlusBtn[3] = findViewById(R.id.jog_j4_plus_button);
        jogPlusBtn[4] = findViewById(R.id.jog_j5_plus_button);
        jogPlusBtn[5] = findViewById(R.id.jog_j6_plus_button);

        jogMinusBtn = new Button[6];
        jogMinusBtn[0] = findViewById(R.id.jog_j1_minus_button);
        jogMinusBtn[1] = findViewById(R.id.jog_j2_minus_button);
        jogMinusBtn[2] = findViewById(R.id.jog_j3_minus_button);
        jogMinusBtn[3] = findViewById(R.id.jog_j4_minus_button);
        jogMinusBtn[4] = findViewById(R.id.jog_j5_minus_button);
        jogMinusBtn[5] = findViewById(R.id.jog_j6_minus_button);

        jogPlusBtn[0].setOnTouchListener(this);
        jogPlusBtn[1].setOnTouchListener(this);
        jogPlusBtn[2].setOnTouchListener(this);
        jogPlusBtn[3].setOnTouchListener(this);
        jogPlusBtn[4].setOnTouchListener(this);
        jogPlusBtn[5].setOnTouchListener(this);

        jogMinusBtn[0].setOnTouchListener(this);
        jogMinusBtn[1].setOnTouchListener(this);
        jogMinusBtn[2].setOnTouchListener(this);
        jogMinusBtn[3].setOnTouchListener(this);
        jogMinusBtn[4].setOnTouchListener(this);
        jogMinusBtn[5].setOnTouchListener(this);

        jogMoveTab=findViewById(R.id.jog_move_tab);
        jogMoveTab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition()==0)
                {
                    jogText[0].setText("J1");
                    jogText[1].setText("J2");
                    jogText[2].setText("J3");
                    jogText[3].setText("J4");
                    jogText[4].setText("J5");
                    jogText[5].setText("J6");
                }else   {
                    jogText[0].setText("x");
                    jogText[1].setText("y");
                    jogText[2].setText("z");
                    jogText[3].setText("rx");
                    jogText[4].setText("ry");
                    jogText[5].setText("rz");
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    int pos = -1;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        boolean isPosTouch = true;

        switch (v.getId()) {
            case R.id.jog_j1_plus_button:
                pos = 0;
                break;
            case R.id.jog_j2_plus_button:
                pos = 1;
                break;
            case R.id.jog_j3_plus_button:
                pos = 2;
                break;
            case R.id.jog_j4_plus_button:
                pos = 3;
                break;
            case R.id.jog_j5_plus_button:
                pos = 4;
                break;
            case R.id.jog_j6_plus_button:
                pos = 5;
                break;
            case R.id.jog_j1_minus_button:
                pos = 6;
                break;
            case R.id.jog_j2_minus_button:
                pos = 7;
                break;
            case R.id.jog_j3_minus_button:
                pos = 8;
                break;
            case R.id.jog_j4_minus_button:
                pos = 9;
                break;
            case R.id.jog_j5_minus_button:
                pos = 10;
                break;
            case R.id.jog_j6_minus_button:
                pos = 11;
                break;
            default:
                isPosTouch = false;
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN && isPosTouch) {
            if (pos<12)
                present.setJogMove(false, pos);
            else
                present.setJogMove(true, pos-12);
        } else if (event.getAction() == MotionEvent.ACTION_POINTER_2_DOWN && pos != -1 && !isPosTouch) {
            pos = -1;
            present.stopMove();
        } else if (event.getAction() == MotionEvent.ACTION_UP && pos != -1) {
            pos = -1;
            present.stopMove();
        } else if (event.getAction() == MotionEvent.ACTION_POINTER_UP && pos != -1) {
            pos = -1;
            present.stopMove();
        }
        return false;
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
    }

    @Override
    public void refreshPowerState(boolean isPowerOn) {
        if (isPowerOn) {
            powerBTN.setImageResource(R.drawable.ic_power_button_red);
        } else {
            powerBTN.setImageResource(R.drawable.ic_power);
        }
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
//        robotModeText.setText(getString(R.string.robot_mode_text) + mode);
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
    public void refreshQActual(double[] getqActual) {

        int i = 0;
        NumberFormat nf = NumberFormat.getInstance();
        nf.setGroupingUsed(false);
        for (double data : getqActual) {
            i++;
            data = (double) (Math.round(data * 10000)) / 10000;
            getqActual[i - 1] = data;
            jogText[i-1].setText("J"+i+":"+nf.format(getqActual[i-1]));
        }
        //System.out.println("j data "+i+" 1:"+data);


        /*qActualText.setText(
                "j1:" + nf.format(getqActual[0]) +
                        " j2:" + nf.format(getqActual[1]) +
                        " j3:" + nf.format(getqActual[2]) +
                        "\nj4:" + nf.format(getqActual[3]) +
                        " j5:" + nf.format(getqActual[4]) +
                        " j6:" + nf.format(getqActual[5]));*/
    }

    @Override
    public void refreshToolVectorActual(double[] toolVectorActual) {
//        int i = 0;
//        for (double data : toolVectorActual) {
//            i++;
//            data = (double) (Math.round(data * 10000)) / 10000;
//            toolVectorActual[i - 1] = data;
//        }
//        NumberFormat nf = NumberFormat.getInstance();
//        nf.setGroupingUsed(false);
//        coordText[0].setText(  "X:" + toolVectorActual[0]);
//        coordText[1].setText(  "Y:" + toolVectorActual[1]);
//        coordText[2].setText(  "Z:" + toolVectorActual[2]);
//        coordText[3].setText(  "RX:" + toolVectorActual[3]);
//        coordText[4].setText(  "RY:" + toolVectorActual[4]);
//        coordText[5].setText(  "RZ:" + toolVectorActual[5]);
//TODO() Нужно сделать переменную, когда будешь переключаться с пользователя на инструмент
    }

    @Override
    public void refreshProgramState(int programState) {
        //   programStateText.setText("Program state:" + programState);
    }

    @Override
    public void refreshErrorList(String errorInfo) {
//        errorList.add(errorInfo);
//        errorListAdapter.notifyDataSetChanged();
//TODO() Добавить вывод ошибок
    }

    @Override
    public void refreshLogList(final boolean isSend, final String log) {
//        handler.post(new Runnable() {
//            @Override
//            public void run() {
//
//                logList.add((isSend?getString(R.string.send_msg):getString(R.string.receive_msg))+log);
//                logListAdapter.notifyDataSetChanged();
//            }
//        });
//TODO() Можно сделать вывод логов
    };

    @Override
    public void refreshAlarmList(List<AlarmData> dataList) {
//        errorList.clear();
//        for (AlarmData data:dataList)
//            errorList.add(data.toString());
//        errorListAdapter.notifyDataSetChanged();
//TODO() Перезапуск ошибок
    }
}
