package cc.dobot.crtcpdemo;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.NumberFormat;
import java.util.List;

import cc.dobot.crtcpdemo.client.StateMessageClient;
import cc.dobot.crtcpdemo.data.AlarmData;
import cc.dobot.crtcpdemo.message.constant.Robot;

public class CountActivity extends AppCompatActivity implements MainContract.View {

    private ImageButton drag_mode, enableRobot, moreBTN, resetRobot, error_btn, emergencyStop;
    private ImageView connection_icon;
    private Button get_pos_btn1, get_pos_btn2, get_pos_btn3, connectRobot;
    private TextView pos1, pos2, pos3;
    private double x1, y1, z1, rx1, ry1, rz1;
    private double x2, y2, z2, rx2, ry2, rz2;
    private double x3, y3, z3, rx3, ry3, rz3;
    private boolean isDragMode = false;

    private MainContract.Present present;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_count);

        initView();
        initListeners();
        present = new MainPresent(this);
    }

    private void initView() {
        connection_icon = findViewById(R.id.connection_icon);
        connectRobot = findViewById(R.id.button_connect_robot);

        enableRobot = findViewById(R.id.button_change_enable);

        moreBTN = findViewById(R.id.more_button);

        resetRobot = findViewById(R.id.button_reset_robot);

        error_btn = findViewById(R.id.error_btn);

        emergencyStop = findViewById(R.id.button_emergency_stop);

        drag_mode = findViewById(R.id.drag_mode);

        get_pos_btn1 = findViewById(R.id.get_pos_btn1);
        get_pos_btn2 = findViewById(R.id.get_pos_btn2);
        get_pos_btn3 = findViewById(R.id.get_pos_btn3);

        pos1 = findViewById(R.id.pos1);
        pos2 = findViewById(R.id.pos2);
        pos3 = findViewById(R.id.pos3);
    }

    private void initListeners() {
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
                    String text = String.format("X=%.4f, Y=%.4f, Z=%.4f\nRx=%.4f, Ry=%.4f, Rz=%.4f", x1, y1, z1, rx1, ry1, rz1);
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
                    String text = String.format("X=%.4f, Y=%.4f, Z=%.4f\nRx=%.4f, Ry=%.4f, Rz=%.4f", x2, y2, z2, rx2, ry2, rz2);
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
                    String text = String.format("X=%.4f, Y=%.4f, Z=%.4f\nRx=%.4f, Ry=%.4f, Rz=%.4f", x3, y3, z3, rx3, ry3, rz3);
                    pos3.setText(text);
                } else {
                    pos3.setText("Данные недоступны");
                }
            }
        });
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

    }

    @Override
    public void refreshEnableState(boolean isEnable) {
        if (isEnable) {
            enableRobot.setImageResource(R.drawable.ic_pause);
        } else {
            enableRobot.setImageResource(R.drawable.ic_play);
        }
    }

    public void resetDragModeIcon(boolean isDragEnabled) {
        if (isDragEnabled) {
            drag_mode.setImageResource(R.drawable.ic_dragred);
        } else {
            drag_mode.setImageResource(R.drawable.ic_drag);
        }
    }

    @Override
    public void refreshSpeedScaling(int speedScaling) {

    }

    @Override
    public void refreshRobotMode(Robot.Mode mode) {

    }

    @Override
    public void refreshDI(byte[] DI) {
    }

    @Override
    public void refreshDO(byte[] DO) {

    }

    @Override
    public void refreshQActual(double[] getqActual) {

    }

    @Override
    public void refreshToolVectorActual(double[] toolVectorActual) {

    }

    @Override
    public void refreshProgramState(int programState) {

    }

    @Override
    public void refreshErrorList(String errorInfo) {

    }

    @Override
    public void refreshLogList(boolean isSend, String log) {

    }

    @Override
    public void refreshAlarmList(List<AlarmData> dataList) {

    }
}
