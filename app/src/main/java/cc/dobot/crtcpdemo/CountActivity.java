package cc.dobot.crtcpdemo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import com.google.android.material.tabs.TabLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

import cc.dobot.crtcpdemo.client.StateMessageClient;
import cc.dobot.crtcpdemo.data.AlarmData;
import cc.dobot.crtcpdemo.message.constant.Robot;
import cc.dobot.crtcpdemo.message.product.cr.CRMessageAccL;

public class CountActivity extends AppCompatActivity implements MainContract.View {

    private ImageButton drag_mode, btn_back;
    private Button get_pos_btn1, get_pos_btn2, get_pos_btn3, get_pos_btn4, get_pos_btn5, get_pos_btn6;
    private Button save_btn, goBtn, stopBtn, houseBtn, otBtn, podBtn, threeBtn;
    private TextView pos1, pos2, pos3, pos4, pos5, pos6;
    private Map<Integer, LinearLayout> taskViews = new HashMap<>();
    private LinearLayout dropdown_menu;
    private CardView card_top, card_bottom;
    private TabLayout tabLayout;
    private int currentTaskIndex = 0;
    private double[] u; // Параметры новой рабочей плоскости
    private double x1, y1, z1, rx1, ry1, rz1; // START
//    double[] start = {100, 0, 0}; //Массив START test
    double[] start = {x1, y1, z1}; //Массив START
    private double x2, y2, z2, rx2, ry2, rz2; // MID
//    double[] mid = {0, 100, 0}; //Массив MID test
    double[] mid = {x2, y2, z2}; //Массив MID
    private double x3, y3, z3, rx3, ry3, rz3; // END
//    double[] end = {0, 0, 100}; //Массив END test
    double[] end = {x3, y3, z3}; //Массив END
    private double x4, y4, z4, rx4, ry4, rz4;
    private double x5, y5, z5, rx5, ry5, rz5;
    private double x6, y6, z6, rx6, ry6, rz6;
    double Radius; // Радиус окружности
    int selectedUser, selectedTool; // Индексы пользователя и инструмента
//    private double start_coner_save;
//    private double mid_coner_save;
//    private double end_coner_save;
//    private double[] norm;
//    private double OKR;
    private boolean isDragMode = false;
    private final Map<Integer, List<View>> taskBlocksMap = new HashMap<>();
//    private int col_Step_1, col_Step_2;
//    private double delta_Rx_1, delta_Ry_1, delta_Rz_1;
//    private double delta_Rx_2, delta_Ry_2, delta_Rz_2;
//    private int CS;
//    private double Rotate;
//    private List<double[]> arcPoints; // Список точек траектории
//    private int currentStep = 0;     // Текущий шаг
//    private long stepDelay = 500;    // Задержка между шагами (мс)
    private Spinner user, tool;
    private Handler movementHandler;
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
        tabLayout = findViewById(R.id.task_tabs);
        FrameLayout taskContainer = findViewById(R.id.task_container);

//         Создаём 3 вкладки
        for (int i = 1; i <= 3; i++) {
            tabLayout.addTab(tabLayout.newTab().setText("Задача " + i));
        }

        // Инициализируем первую задачу
        LinearLayout firstTaskLayout = new LinearLayout(this);
        firstTaskLayout.setOrientation(LinearLayout.VERTICAL);
        taskViews.put(0, firstTaskLayout);
        taskContainer.addView(firstTaskLayout);

//         Обработка вкладок
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int newIndex = tab.getPosition();
                switchTaskView(newIndex);
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        user = findViewById(R.id.user);
        tool = findViewById(R.id.tool);

        // Создаем массив чисел от 0 до 9
        String[] numbers = new String[10];
        for (int i = 0; i < 10; i++) {
            numbers[i] = String.valueOf(i);
        }

        // Создаем адаптер для Spinner'ов
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.spinner_item, // ваш кастомный layout
                numbers);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        // Устанавливаем адаптеры
        user.setAdapter(adapter);
        tool.setAdapter(adapter);

        initView();
        initListeners();
        movementHandler = new Handler();
        present = new MainPresent(this);

        // Обработчики выбора для Spinner'ов
        user.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedUser = Integer.parseInt((String) parent.getItemAtPosition(position));
                if (present != null) {
                    present.setUser(selectedUser);
                    Log.d("Spinner", "User selected: " + selectedUser);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        tool.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedTool = Integer.parseInt((String) parent.getItemAtPosition(position));
                if (present != null) {
                    present.setTool(selectedTool);
                    Log.d("Spinner", "Tool selected: " + selectedTool);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void initView() {
        drag_mode = findViewById(R.id.drag_mode);
        btn_back = findViewById(R.id.btn_back);

        get_pos_btn1 = findViewById(R.id.get_pos_btn1);
        get_pos_btn2 = findViewById(R.id.get_pos_btn2);
        get_pos_btn3 = findViewById(R.id.get_pos_btn3);
        get_pos_btn4 = findViewById(R.id.get_pos_btn4);
        get_pos_btn5 = findViewById(R.id.get_pos_btn5);
        get_pos_btn6 = findViewById(R.id.get_pos_btn6);

        pos1 = findViewById(R.id.pos1);
        pos2 = findViewById(R.id.pos2);
        pos3 = findViewById(R.id.pos3);
        pos4 = findViewById(R.id.pos4);
        pos5 = findViewById(R.id.pos5);
        pos6 = findViewById(R.id.pos6);

        card_top = findViewById(R.id.card_top);
        card_bottom = findViewById(R.id.card_bottom);

        dropdown_menu = findViewById(R.id.dropdown_menu);

        threeBtn = findViewById(R.id.three);
        otBtn = findViewById(R.id.otxod);
        podBtn = findViewById(R.id.podxod);
        houseBtn = findViewById(R.id.house);
        stopBtn = findViewById(R.id.stop_btn);
        goBtn = findViewById(R.id.go_btn);
        save_btn = findViewById(R.id.save_btn);
    }

    private void initListeners() {
        threeBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: // Когда кнопка нажата
                        present.setUserTestJogMove(selectedUser, true);
                        return true;
                    case MotionEvent.ACTION_UP:   // Когда кнопка отпущена
                    case MotionEvent.ACTION_CANCEL: // Если действие отменено (например, палец ушёл за пределы кнопки)
                        present.setUserTestJogMoveStop(selectedUser, true);
                        return true;
                }
                return false;
            }
        });

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dropdown_menu.getVisibility() == View.VISIBLE) {  // Исправлено условие
                    toggleDropdownMenu();  // Закрываем выпадающее меню
                } else {
                    Intent intent = new Intent(CountActivity.this, MainActivityEpsilon.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                }
            }
        });

        goBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<View> blocks = taskBlocksMap.get(currentTaskIndex);
                double Step = -1;
                int selectedBlockIndex = -1;

                if (blocks != null) {
                    for (int i = 0; i < blocks.size(); i++) {
                        View block = blocks.get(i);
                        CheckBox checkBox = block.findViewById(R.id.checkbox_select);
                        if (checkBox != null && checkBox.isChecked()) {
                            EditText stepField = block.findViewById(R.id.input_step);
                            if (stepField != null) {
                                try {
                                    Step = Double.parseDouble(stepField.getText().toString());
                                    selectedBlockIndex = i;
                                    Log.d("GO_BTN", "Выбран блок #" + selectedBlockIndex + " со значением шага: " + Step);
                                    break; // Берём первый активный
                                } catch (NumberFormatException e) {
                                    Log.e("GO_BTN", "Некорректное значение шага в блоке #" + i);
                                    Toast.makeText(CountActivity.this, "Ошибка: неверный формат шага", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }
                        }
                    }
                }

                if (Step <= 0) {
                    Toast.makeText(CountActivity.this, "Не найден активный блок с корректным шагом", Toast.LENGTH_SHORT).show();
                    Log.w("GO_BTN", "Нет подходящего блока с установленным checkbox и корректным шагом");
                    return;
                }
//                calculations();
//                trajectory_Parameters();
//
//                executeTrajectory();
            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                present.emergencyStop();
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
                LayoutInflater inflater = LayoutInflater.from(CountActivity.this);
                final View block = inflater.inflate(R.layout.task_element, null);
                Spinner spinner = block.findViewById(R.id.spinner_type);

                String[] types = new String[]{"Линейно", "Пила", "Зиг-Заг", "По кругу", "По окружности"};
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        CountActivity.this,
                        android.R.layout.simple_spinner_item,
                        types
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);

                int idx = currentTaskIndex;
                if (!taskBlocksMap.containsKey(idx)) {
                    taskBlocksMap.put(idx, new ArrayList<View>());
                }
                taskBlocksMap.get(idx).add(block);

                Button btnAction = block.findViewById(R.id.btn_action);
                btnAction.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        List<View> blocks = taskBlocksMap.get(currentTaskIndex);
                        int blockIndex = blocks.indexOf(block);

                        // Получение данных
                        CheckBox checkBox = block.findViewById(R.id.checkbox_select);
                        Spinner spinner = block.findViewById(R.id.spinner_type);
                        EditText width = block.findViewById(R.id.input_width);
                        EditText height = block.findViewById(R.id.input_height);
                        EditText step = block.findViewById(R.id.input_step);
                        step.setText("5");
                        EditText speed = block.findViewById(R.id.input_speed);
                        speed.setText("50");
                        Button actionBtn = block.findViewById(R.id.btn_action);

                        // Вывод в лог
                        StringBuilder sb = new StringBuilder();
                        sb.append("Блок #").append(blockIndex).append("\n");
                        sb.append("  - Checked: ").append(checkBox.isChecked()).append("\n");
                        sb.append("  - Тип: ").append(spinner.getSelectedItem()).append("\n");
                        sb.append("  - Ширина: ").append(width.getText().toString()).append("\n");
                        sb.append("  - Высота: ").append(height.getText().toString()).append("\n");
                        sb.append("  - Шаг: ").append(step.getText().toString()).append("\n");
                        sb.append("  - Скорость: ").append(speed.getText().toString()).append("\n");
                        sb.append("  - Кнопка: ").append(actionBtn.getText().toString());
                        Log.d("TASK_DATA", sb.toString());

                        // Чтение скорости
                        long stepDelay = 50; // по умолчанию
                        try {
                            double s = Double.parseDouble(speed.getText().toString());
                            if (s > 0) {
                                stepDelay = (long) (1000 / s);
                            }
                        } catch (NumberFormatException e) {
                            Log.e("SPEED", "Некорректная скорость", e);
                            Toast.makeText(CountActivity.this, "Неверная скорость", Toast.LENGTH_SHORT).show();
                            return;
                        }
//                        calculations();
//                        trajectory_Parameters();
//                        executeTrajectory();
                    }
                });
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
                    start = new double[]{x1, y1, z1};
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
                    mid = new double[]{x2, y2, z2};
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
                    end = new double[]{x3, y3, z3};
                    String text = String.format("X=%.4f, Y=%.4f\n Z=%.4f, Rx=%.4f\n Ry=%.4f, Rz=%.4f", x3, y3, z3, rx3, ry3, rz3);
                    pos3.setText(text);
                } else {
                    pos3.setText("Данные недоступны");
                }
            }
        });

        get_pos_btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double[] coords = StateMessageClient.getInstance().getState().getToolVectorActual();
                if (coords != null && coords.length >= 6) {
                    x4 = coords[0];
                    y4 = coords[1];
                    z4 = coords[2];
                    rx4 = coords[3];
                    ry4 = coords[4];
                    rz4 = coords[5];
                    String text = String.format("X=%.4f, Y=%.4f\n Z=%.4f, Rx=%.4f\n Ry=%.4f, Rz=%.4f", x4, y4, z4, rx4, ry4, rz4);
                    pos4.setText(text);
                } else {
                    pos4.setText("Данные недоступны");
                }
            }
        });

        get_pos_btn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double[] coords = StateMessageClient.getInstance().getState().getToolVectorActual();
                if (coords != null && coords.length >= 6) {
                    x5 = coords[0];
                    y5 = coords[1];
                    z5 = coords[2];
                    rx5 = coords[3];
                    ry5 = coords[4];
                    rz5 = coords[5];
                    String text = String.format("X=%.4f, Y=%.4f\n Z=%.4f, Rx=%.4f\n Ry=%.4f, Rz=%.4f", x5, y5, z5, rx5, ry5, rz5);
                    pos5.setText(text);
                } else {
                    pos5.setText("Данные недоступны");
                }
            }
        });

        get_pos_btn6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double[] coords = StateMessageClient.getInstance().getState().getToolVectorActual();
                if (coords != null && coords.length >= 6) {
                    x6 = coords[0];
                    y6 = coords[1];
                    z6 = coords[2];
                    rx6 = coords[3];
                    ry6 = coords[4];
                    rz6 = coords[5];
                    String text = String.format("X=%.4f, Y=%.4f\n Z=%.4f, Rx=%.4f\n Ry=%.4f, Rz=%.4f", x6, y6, z6, rx6, ry6, rz6);
                    pos6.setText(text);
                } else {
                    pos6.setText("Данные недоступны");
                }
            }
        });

        otBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               moveToRetreatPosition();
           }
        });

        podBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToApproachPosition();
            }
        });

        houseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToHomePosition();
            }
        });

        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleDropdownMenu();
                Radius_calculation();
                Log.d("SelectedUserCoord", "Индекс пользователя:" + selectedUser);
                present.setUserCoord(selectedUser, u);
//                calculations();// Вызываем расчеты
                Toast.makeText(CountActivity.this, "Значения сохранены", Toast.LENGTH_SHORT).show();  // Исправлен контекст
            }
        });
    }

    //ДОМОЙ
    private void moveToHomePosition() {
        // Проверяем, что координаты были сохранены
        if (x6 == 0 && y6 == 0 && z6 == 0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(CountActivity.this, "Домашняя позиция не задана", Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }

        // Создаем массив с координатами
        double[] homePosition = new double[]{
                x6, y6, z6,  // X, Y, Z
                rx6, ry6, rz6  // Rx, Ry, Rz
        };

        // Отправляем команду на перемещение
        if (present != null) {
            present.doMovL(homePosition);
            present.syncRobot();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(CountActivity.this, "Перемещение в домашнюю позицию", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(CountActivity.this, "Ошибка: Presenter не инициализирован", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    //ПОДХОД
    private void moveToApproachPosition() {
        if (x5 == 0 && y5 == 0 && z5 == 0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(CountActivity.this, "Точка подхода не задана", Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }

        double[] approachPosition = new double[]{
                x5, y5, z5,
                rx5, ry5, rz5
        };

        if (present != null) {
            present.doMovL(approachPosition);
            present.syncRobot();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(CountActivity.this, "Перемещение в точку подхода", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(CountActivity.this, "Ошибка: Presenter не инициализирован", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    //ОТХОД
    private void moveToRetreatPosition() {
        if (x4 == 0 && y4 == 0 && z4 == 0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(CountActivity.this, "Точка отхода не задана", Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }

        double[] retreatPosition = new double[]{
                x4, y4, z4,
                rx4, ry4, rz4
        };

        if (present != null) {
            present.doMovL(retreatPosition);
            present.syncRobot();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(CountActivity.this, "Перемещение в отхода подхода", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(CountActivity.this, "Ошибка: Presenter не инициализирован", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void toggleDropdownMenu() {
        if (dropdown_menu.getVisibility() == View.VISIBLE) {
            dropdown_menu.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            dropdown_menu.setVisibility(View.GONE);
                        }
                    });
            setButtonsVisibility(View.VISIBLE);
            tabLayout.setVisibility(View.INVISIBLE);
        } else {
            setButtonsVisibility(View.INVISIBLE);
            tabLayout.setVisibility(View.VISIBLE);
            dropdown_menu.setAlpha(0f);
            dropdown_menu.setVisibility(View.VISIBLE);
            dropdown_menu.animate()
                    .alpha(1f)
                    .setDuration(300);
        }
    }

    private void setButtonsVisibility(int visibility) {
        card_top.setVisibility(visibility);
        card_bottom.setVisibility(visibility);
        get_pos_btn1.setVisibility(visibility);
        get_pos_btn2.setVisibility(visibility);
        get_pos_btn3.setVisibility(visibility);
        get_pos_btn4.setVisibility(visibility);
        get_pos_btn5.setVisibility(visibility);
        get_pos_btn6.setVisibility(visibility);
        save_btn.setVisibility(visibility);
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

        List<Integer> elementTypes = viewModel.getElementTypes(newIndex);
        if (elementTypes != null) {
            for (Integer type : elementTypes) {
                View block = inflater.inflate(type, null);
                layout.addView(block);
            }
        }

        container.addView(layout);
        currentTaskIndex = newIndex;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

//    private void calculations() {
//        // Получаем координаты из полей класса
//        double[] start = {x1, y1, z1};
//        double[] mid = {x2, y2, z2};
//        double[] end = {x3, y3, z3};
////        double[] start = {x1 = 10.0, y1 = 0.0, z1 = 0.0};
////        double[] mid = {x2 = 0.0, y2 = 10.0, z2 = 0.0};
////        double[] end = {x3 = 0.0, y3 = 0.0, z3 = 10.0};
//
//        boolean is_Debug_Info_Active = true;
//
//        if (is_Debug_Info_Active) {
//            Log.d("CALC", "Start: " + start[0] + " " + start[1] + " " + start[2]);
//            Log.d("CALC", "Mid: " + mid[0] + " " + mid[1] + " " + mid[2]);
//            Log.d("CALC", "End: " + end[0] + " " + end[1] + " " + end[2]);
//        }
//
//        // Вычисляем расстояния между точками
//        double c = Math.sqrt(Math.pow(start[0]-mid[0], 2) + Math.pow(start[1]-mid[1], 2) + Math.pow(start[2]-mid[2], 2));
//        double b = Math.sqrt(Math.pow(start[0]-end[0], 2) + Math.pow(start[1]-end[1], 2) + Math.pow(start[2]-end[2], 2));
//        double a = Math.sqrt(Math.pow(mid[0]-end[0], 2) + Math.pow(mid[1]-end[1], 2) + Math.pow(mid[2]-end[2], 2));
//
//        if (is_Debug_Info_Active) {
//            Log.d("CALC", "a: " + a);
//            Log.d("CALC", "b: " + b);
//            Log.d("CALC", "c: " + c);
//        }
//
//        // Вычисляем радиус окружности
//        double s = (a + b + c) / 2;
//        double R = a * b * c / (4 * Math.sqrt(s * (s - a) * (s - b) * (s - c)));
//        double Radius = R;
//        Log.d("CALC", "Диаметр окружности дуги: " + String.format("%.2f", R * 2) + " мм");
//
//        // Вычисляем центр окружности
//        double b1 = a * a * (b * b + c * c - a * a);
//        double b2 = b * b * (a * a + c * c - b * b);
//        double b3 = c * c * (a * a + b * b - c * c);
//        double b_summ = b1 + b2 + b3;
//
//        if (is_Debug_Info_Active) {
//            Log.d("CALC", "b1: " + b1);
//            Log.d("CALC", "b2: " + b2);
//            Log.d("CALC", "b3: " + b3);
//        }
//
//        double c1 = (start[0] * b1 + mid[0] * b2 + end[0] * b3) / b_summ;
//        double c2 = (start[1] * b1 + mid[1] * b2 + end[1] * b3) / b_summ;
//        double c3 = (start[2] * b1 + mid[2] * b2 + end[2] * b3) / b_summ;
//        double[] center = {c1, c2, c3};
//
//        if (is_Debug_Info_Active) {
//            Log.d("CALC", "Координаты центра: " + Arrays.toString(center));
//        }
//
//        // Вычисляем векторы от центра к точкам
//        double[] R1 = {center[0]-start[0], center[1]-start[1], center[2]-start[2]};
//        double[] R2 = {center[0]-mid[0], center[1]-mid[1], center[2]-mid[2]};
//        double[] R3 = {center[0]-end[0], center[1]-end[1], center[2]-end[2]};
//
//        if (is_Debug_Info_Active) {
//            Log.d("CALC", "R1: " + Arrays.toString(R1));
//            Log.d("CALC", "R2: " + Arrays.toString(R2));
//            Log.d("CALC", "R3: " + Arrays.toString(R3));
//        }
//
//        // Вычисляем нормаль
//        double[] norm = {
//                R1[2]*R2[1] - R1[1]*R2[2],
//                R1[0]*R2[2] - R1[2]*R2[0],
//                R1[1]*R2[0] - R1[0]*R2[1]
//        };
//
//        // Проверка внутренней/внешней поверхности трубы
//        double vip = (end[2] - start[2]) / (end[0] - start[0]);
//        double z_check = start[2] + vip * (mid[0] - start[0]);
//        boolean is_inside_tibe = z_check >= mid[2];
//
//        if (is_inside_tibe) {
//            Log.d("CALC", "Сварка внутренней поверхности трубы");
//        } else {
//            Log.d("CALC", "Сварка внешней поверхности трубы");
//        }
//
//        // Корректировка нормали
//        if (norm[1] < 0) {
//            norm[0] = -norm[0];
//            norm[1] = -norm[1];
//            norm[2] = -norm[2];
//            if (is_Debug_Info_Active) {
//                Log.d("CALC", "Перевернули нормаль");
//            }
//        }
//
//        // Нормализация вектора
//        double length_norm = Math.sqrt(norm[0]*norm[0] + norm[1]*norm[1] + norm[2]*norm[2]);
//        norm[0] /= length_norm;
//        norm[1] /= length_norm;
//        norm[2] /= length_norm;
//
//        if (is_Debug_Info_Active) {
//            Log.d("CALC", "length_norm: " + length_norm);
//            Log.d("CALC", "norm: " + Arrays.toString(norm));
//        }
//
//        // Вычисление углов
//        double coner_z = Math.asin(-norm[0]);
//        double coner_x = Math.asin(norm[2]);
//
//        if (is_Debug_Info_Active) {
//            Log.d("CALC", "coner_x: " + Math.toDegrees(coner_x));
//            Log.d("CALC", "coner_z: " + Math.toDegrees(coner_z));
//        }
//
//        // Вычисление углов для начальной точки
//        double X_len = start[0] - center[0];
//        double Y_len = start[1] - center[1];
//        double Z_high = start[2] - center[2];
//        double Y_compensation = Z_high * Math.sin(coner_x);
//        double XY_len = Math.sqrt(Math.pow(X_len - Y_compensation*Math.sin(coner_z), 2) + Math.pow(Y_len + Y_compensation, 2));
//        Z_high = Z_high / Math.cos(coner_x);
//
//        if (is_Debug_Info_Active) {
//            Log.d("CALC", "XY_len: " + XY_len);
//            Log.d("CALC", "Z_high: " + Z_high);
//        }
//
//        double start_coner;
//        if ((start[0] - center[0]) < 0) {
//            start_coner = Math.PI * 3 / 2 + Math.atan(Z_high / XY_len);
//        } else {
//            start_coner = Math.PI / 2 - Math.atan(Z_high / XY_len);
//        }
//
//        if (start_coner > Math.PI && !is_inside_tibe) {
//            start_coner = start_coner - 2 * Math.PI;
//        }
//
//        if (is_Debug_Info_Active) {
//            Log.d("CALC", "Угол начальной точки: " + String.format("%.1f", start_coner*180/Math.PI));
//        }
//
//        // Вычисление угла для средней точки (MID)
//        double X_len_mid = mid[0] - center[0];
//        double Y_len_mid = mid[1] - center[1];
//        double Z_high_mid = mid[2] - center[2];
//        double Y_compensation_mid = Z_high_mid * Math.sin(coner_x);
//
//        if (is_Debug_Info_Active) {
//            Log.d("CALC", "MID X_len: " + X_len_mid);
//            Log.d("CALC", "MID Y_len: " + Y_len_mid);
//        }
//
//        double XY_len_mid = Math.sqrt(Math.pow(X_len_mid - Y_compensation_mid * Math.sin(coner_z), 2) +
//                Math.pow(Y_len_mid + Y_compensation_mid, 2));
//
//        if (is_Debug_Info_Active) {
//            Log.d("CALC", "MID XY_len: " + XY_len_mid);
//        }
//
//        Z_high_mid = Z_high_mid / Math.cos(coner_x);
//
//        if (is_Debug_Info_Active) {
//            Log.d("CALC", "MID Z_high: " + Z_high_mid);
//        }
//
//        double mid_coner;
//        if ((mid[0] - center[0]) < 0) {
//            mid_coner = Math.PI * 3 / 2 + Math.atan(Z_high_mid / XY_len_mid);
//        } else {
//            mid_coner = Math.PI / 2 - Math.atan(Z_high_mid / XY_len_mid);
//        }
//
//        if (mid_coner > Math.PI && !is_inside_tibe) {
//            mid_coner = mid_coner - 2 * Math.PI;
//        }
//
//        if (is_Debug_Info_Active) {
//            Log.d("CALC", "Угол средней точки: " + String.format("%.1f", mid_coner*180/Math.PI));
//        }
//
//        // Вычисление угла для конечной точки (END)
//        double X_len_end = end[0] - center[0];
//        double Y_len_end = end[1] - center[1];
//        double Z_high_end = end[2] - center[2];
//        double Y_compensation_end = Z_high_end * Math.sin(coner_x);
//
//        double XY_len_end = Math.sqrt(Math.pow(X_len_end - Y_compensation_end * Math.sin(coner_z), 2) +
//                Math.pow(Y_len_end + Y_compensation_end, 2));
//
//        if (is_Debug_Info_Active) {
//            Log.d("CALC", "END XY_len: " + XY_len_end);
//        }
//
//        Z_high_end = Z_high_end / Math.cos(coner_x);
//
//        if (is_Debug_Info_Active) {
//            Log.d("CALC", "END Z_high: " + Z_high_end);
//        }
//
//        double end_coner;
//        if ((end[0] - center[0]) < 0) {
//            end_coner = Math.PI * 3 / 2 + Math.atan(Z_high_end / XY_len_end);
//        } else {
//            end_coner = Math.PI / 2 - Math.atan(Z_high_end / XY_len_end);
//        }
//
//        if (end_coner > Math.PI && !is_inside_tibe) {
//            end_coner = end_coner - 2 * Math.PI;
//        }
//
//        if (is_Debug_Info_Active) {
//            Log.d("CALC", "Угол конечной точки: " + String.format("%.1f", end_coner*180/Math.PI));
//        }
//
//        // Сохраняем результаты
//        this.start_coner_save = start_coner;
//        this.mid_coner_save = mid_coner;
//        this.end_coner_save = end_coner;
//        this.OKR = (2 * Math.PI * Radius);
//        this.norm = norm;
//
//        if (is_Debug_Info_Active) {
//            Log.d("CALC", "Сохраненные результаты:");
//            Log.d("CALC", "start_coner: " + Math.toDegrees(start_coner_save));
//            Log.d("CALC", "mid_coner: " + Math.toDegrees(mid_coner_save));
//            Log.d("CALC", "end_coner: " + Math.toDegrees(end_coner_save));
//            Log.d("CALC", "OKR (длина окружности): " + OKR);
//        }
//    }
//
//    private void R_Coordinates_Correction() {
//        boolean is_Debug_Info_Active = true; // Флаг для отладки
//
//        double Rx_Start = rx2 - rx1;
//        double Ry_Start = ry2 - ry1;
//        double Rz_Start = rz2 - rz1;
//
//        double Rx_End = rx3 - rx2;
//        double Ry_End = ry3 - ry2;
//        double Rz_End = rz3 - rz2;
//
//        // Коррекция Rx
//        if (Math.abs(Rx_Start) < 180 && Math.abs(Rx_End) > 180) {
//            if (Rx_End > 0) {
//                Rx_End -= 360;
//            } else {
//                Rx_End += 360;
//            }
//            if (is_Debug_Info_Active) {
//                Log.d("CORR", "Пересчитали координату Rx последней точки");
//            }
//        } else if (Math.abs(Rx_Start) > 180 && Math.abs(Rx_End) < 180) {
//            if (Rx_Start > 0) {
//                Rx_Start -= 360;
//            } else {
//                Rx_Start += 360;
//            }
//            if (is_Debug_Info_Active) {
//                Log.d("CORR", "Пересчитали координату Rx первой точки");
//            }
//        }
//
//        // Коррекция Ry
//        if (Math.abs(Ry_Start) < 180 && Math.abs(Ry_End) > 180) {
//            if (Ry_End > 0) {
//                Ry_End -= 360;
//            } else {
//                Ry_End += 360;
//            }
//            if (is_Debug_Info_Active) {
//                Log.d("CORR", "Пересчитали координату Ry последней точки");
//            }
//        } else if (Math.abs(Ry_Start) > 180 && Math.abs(Ry_End) < 180) {
//            if (Ry_Start > 0) {
//                Ry_Start -= 360;
//            } else {
//                Ry_Start += 360;
//            }
//            if (is_Debug_Info_Active) {
//                Log.d("CORR", "Пересчитали координату Ry первой точки");
//            }
//        }
//
//        // Коррекция Rz
//        if (Math.abs(Rz_Start) < 180 && Math.abs(Rz_End) > 180) {
//            if (Rz_End > 0) {
//                Rz_End -= 360;
//            } else {
//                Rz_End += 360;
//            }
//            if (is_Debug_Info_Active) {
//                Log.d("CORR", "Пересчитали координату Rz последней точки");
//            }
//        } else if (Math.abs(Rz_Start) > 180 && Math.abs(Rz_End) < 180) {
//            if (Rz_Start > 0) {
//                Rz_Start -= 360;
//            } else {
//                Rz_Start += 360;
//            }
//            if (is_Debug_Info_Active) {
//                Log.d("CORR", "Пересчитали координату Rz первой точки");
//            }
//        }
//
//        // Обновляем значения углов после коррекции
//        rx2 = rx1 + Rx_Start;
//        ry2 = ry1 + Ry_Start;
//        rz2 = rz1 + Rz_Start;
//
//        rx3 = rx2 + Rx_End;
//        ry3 = ry2 + Ry_End;
//        rz3 = rz2 + Rz_End;
//
//        if (is_Debug_Info_Active) {
//            Log.d("CORR", "Новые значения углов:");
//            Log.d("CORR", "Точка 1 (START): Rx=" + rx1 + " Ry=" + ry1 + " Rz=" + rz1);
//            Log.d("CORR", "Точка 2 (MID): Rx=" + rx2 + " Ry=" + ry2 + " Rz=" + rz2);
//            Log.d("CORR", "Точка 3 (END): Rx=" + rx3 + " Ry=" + ry3 + " Rz=" + rz3);
//        }
//    }
//
//    private void trajectory_Parameters() {
//        Log.d("TRAJ", "Метод trajectory_Parameters() вызван");
//        boolean is_Debug_Info_Active = true; // Флаг для отладки
//
//        double speed = 30; // Значение по умолчанию
//        List<View> blocks = taskBlocksMap.get(currentTaskIndex);
//        if (blocks != null) {
//            for (View block : blocks) {
//                CheckBox checkBox = block.findViewById(R.id.checkbox_select);
//                if (checkBox != null && checkBox.isChecked()) {
//                    EditText speedField = block.findViewById(R.id.input_speed);
//                    if (speedField != null) {
//                        try {
//                            speed = Double.parseDouble(speedField.getText().toString());
//                        } catch (NumberFormatException e) {
//                            Log.e("TRAJ", "Ошибка парсинга скорости", e);
//                        }
//                    }
//                }
//            }
//        }
//        this.stepDelay = (long) (1000 / speed);
//
//        // Поиск активного блока с чекбоксом
//        double Step = 5.0;
//
//        // Вычисляем общие параметры траектории
//        double Ugol = end_coner_save - start_coner_save;
//        double dlina_Dugi = ((Math.abs(Ugol)/Math.PI)/2)*OKR;
//
//        double col_Step = (dlina_Dugi/Step);
//        Log.d("TRAJ", "Длина шва: " + String.format("%.1f", dlina_Dugi) + " мм");
//        this.CS = (int) Math.floor(col_Step);
//        Log.d("TRAJ", "Кол-во шагов: " + String.format("%d", CS));
//        this.Rotate = (Ugol/CS);
//
//        // Вычисляем параметры для первой части траектории (START-MID)
//        double Ugol_1 = mid_coner_save - start_coner_save;
//        double dlina_Dugi_1 = ((Math.abs(Ugol_1)/Math.PI)/2)*OKR;
//        this.col_Step_1 = (int) Math.floor(dlina_Dugi_1/Step);
//        double Rotate_1 = Ugol_1/col_Step_1;
//
//        // Вычисляем параметры для второй части траектории (MID-END)
//        double Ugol_2 = end_coner_save - mid_coner_save;
//        double dlina_Dugi_2 = ((Math.abs(Ugol_2)/Math.PI)/2)*OKR;
//        this.col_Step_2 = (int) Math.floor(dlina_Dugi_2/Step);
//        double Rotate_2 = Ugol_2/col_Step_2;
//
//        R_Coordinates_Correction();
//
//        // Вычисляем дельты для углов Эйлера
//        this.delta_Rx_1 = (rx2 - rx1)/col_Step_1;  //TODO() а можешь объяснить почему в lua Rx_Start/col_Step_1 так, а у меня в коде (rx2 - rx1)/col_Step_1;
//        this.delta_Ry_1 = (ry2 - ry1)/col_Step_1;
//        this.delta_Rz_1 = (rz2 - rz1)/col_Step_1;
//
//        this.delta_Rx_2 = (rx3 - rx2)/col_Step_2;
//        this.delta_Ry_2 = (ry3 - ry2)/col_Step_2;
//        this.delta_Rz_2 = (rz3 - rz2)/col_Step_2;
//
//        if (is_Debug_Info_Active) {
//            Log.d("TRAJ", "Параметры траектории:");
//            Log.d("TRAJ", "Общий угол: " + Math.toDegrees(Ugol) + "°");
//            Log.d("TRAJ", "Длина дуги: " + dlina_Dugi + " мм");
//            Log.d("TRAJ", "Количество шагов: " + CS);
//            Log.d("TRAJ", "Угол на шаг: " + Math.toDegrees(Rotate) + "°");
//
//            Log.d("TRAJ", "Первая часть (START-MID):");
//            Log.d("TRAJ", "Угол: " + Math.toDegrees(Ugol_1) + "°");
//            Log.d("TRAJ", "Длина дуги: " + dlina_Dugi_1 + " мм");
//            Log.d("TRAJ", "Шагов: " + col_Step_1);
//            Log.d("TRAJ", "Угол на шаг: " + Math.toDegrees(Rotate_1) + "°");
//            Log.d("TRAJ", "delta Rx: " + delta_Rx_1);
//            Log.d("TRAJ", "delta Ry: " + delta_Ry_1);
//            Log.d("TRAJ", "delta Rz: " + delta_Rz_1);
//
//            Log.d("TRAJ", "Вторая часть (MID-END):");
//            Log.d("TRAJ", "Угол: " + Math.toDegrees(Ugol_2) + "°");
//            Log.d("TRAJ", "Длина дуги: " + dlina_Dugi_2 + " мм");
//            Log.d("TRAJ", "Шагов: " + col_Step_2);
//            Log.d("TRAJ", "Угол на шаг: " + Math.toDegrees(Rotate_2) + "°");
//            Log.d("TRAJ", "delta Rx: " + delta_Rx_2);
//            Log.d("TRAJ", "delta Ry: " + delta_Ry_2);
//            Log.d("TRAJ", "delta Rz: " + delta_Rz_2);
//        }
//    }
//
//    private double[] coordinates(double shift, double ugol1) {
//        double R = this.OKR / (2 * Math.PI);  // Радиус (OKR - длина окружности)
//        double coner_z = Math.asin(-norm[0]); // Из calculations()
//        double coner_x = Math.asin(norm[2]);  // Из calculations()
//
//        double x1 = R * Math.sin(ugol1);
//        double z1 = R * Math.cos(ugol1);
//
//        double x = x1 * Math.cos(coner_z) - shift * Math.sin(coner_z);
//        double y = x1 * Math.cos(coner_x) * Math.sin(coner_z)
//                + shift * Math.cos(coner_x) * Math.cos(coner_z)
//                - z1 * Math.sin(coner_x);
//        double z = x1 * Math.sin(coner_x) * Math.sin(coner_z)
//                + shift * Math.sin(coner_x) * Math.cos(coner_z)
//                + z1 * Math.cos(coner_x);
//
//        return new double[]{x, y, z};
//    }
//
//    private double[] coners(int nomer) {
//        double Rx, Ry, Rz;
//
//        if (nomer < col_Step_1) {
//            Rx = rx1 + delta_Rx_1 * nomer;
//            Ry = ry1 + delta_Ry_1 * nomer;
//            Rz = rz1 + delta_Rz_1 * nomer;
//        } else {
//            int offset = nomer - col_Step_1;
//            Rx = rx2 + delta_Rx_2 * offset;
//            Ry = ry2 + delta_Ry_2 * offset;
//            Rz = rz2 + delta_Rz_2 * offset;
//        }
//
//        return new double[]{Rx, Ry, Rz};
//    }
//
//    private List<double[]> generateArcPath() {
//        List<double[]> arcPoints = new ArrayList<>();
//
//        // Добавляем точку подхода (если нужно)
////        arcPoints.add(new double[]{x5, y5, z5, rx5, ry5, rz5});
//
//        // Генерируем точки дуги
//        for (int count = 0; count <= CS; count++) {
//            double coner = start_coner_save + Rotate * count;
//            double[] xyz = coordinates(0, coner);
//            double[] rxyz = coners(count);
//            arcPoints.add(new double[]{
//                    xyz[0], xyz[1], xyz[2],
//                    rxyz[0], rxyz[1], rxyz[2]
//            });
//        }
//
//        // Добавляем точку отхода (если нужно)
////        arcPoints.add(new double[]{x4, y4, z4, rx4, ry4, rz4});
//
//        return arcPoints;
//    }
//
//    //ДВИЖЕНИЯ
//    private void executeTrajectory() {
//        // Проверка на валидность точки ДОМ
//        if (!isValidPoint(x6, y6, z6, rx6, ry6, rz6)) {
//            Toast.makeText(this, "Точка дом не задана", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        // Проверка на валидность точки ПОДХОДА
//        if (!isValidPoint(x5, y5, z5, rx5, ry5, rz5)) {
//            Toast.makeText(this, "Точка подхода не задана", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // Проверка на валидность точки ОТХОДА
//        if (!isValidPoint(x4, y4, z4, rx4, ry4, rz4)) {
//            Toast.makeText(this, "Точка отхода не задана", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // Генерация траектории
//        final List<double[]> trajectory = generateArcPath();
//
//        // Запуск движения в фоновом потоке
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    // Перемещение в точку Дом
//                    moveToHomePosition();
//
//                    // Перемещение в точку ПОДХОД
//                    moveToApproachPosition();
//
//                    // Движение по траектории
//                    for (final double[] point : trajectory) {
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                Toast.makeText(CountActivity.this, "Выполнение движения по траектории", Toast.LENGTH_SHORT).show();
//                            }
//                        });
//                        present.doMovL(point);
//                        present.syncRobot();
//                        Thread.sleep(stepDelay);
//                    }
//
//                    // Перемещение в точку ОТХОДА
//                    moveToRetreatPosition();
//
//                    // Перемещение в точку Дом
//                    moveToHomePosition();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
//    }

    // Функция вычисления расстояния между двумя точками
    private double distance(double[] point1, double[] point2) {
        return Math.sqrt(
                Math.pow(point1[0] - point2[0], 2) +
                        Math.pow(point1[1] - point2[1], 2) +
                        Math.pow(point1[2] - point2[2], 2)
        );
    }

    // Функция для добавления угла и нормализации результата (-180..180)
    private double addAngle(double currentAngle, double deltaAngle) {
        double newAngle = currentAngle + deltaAngle;
        if (newAngle > 180) {
            newAngle -= 360;
        } else if (newAngle < -180) {
            newAngle += 360;
        }
        return newAngle;
    }

    private void Radius_calculation() {
        Log.d("CALC", "x1: " + x1 + " y1: " + y1 + " z1: " + z1);
        Log.d("CALC", "x2: " + x2 + " y2: " + y2 + " z2: " + z2);
        Log.d("CALC", "x3: " + x3 + " y3: " + y3 + " z3: " + z3);

        // Вычисление расстояний
        double c = distance(start, mid);
        Log.d("CALC", "c: " + c);
        double b = distance(start, end);
        Log.d("CALC", "b: " + b);
        double a = distance(mid, end);
        Log.d("CALC", "a: " + a);

        // Вычисление радиуса окружности
        double s = (a + b + c) / 2;
        Radius = a * b * c / (4 * Math.sqrt(s * (s - a) * (s - b) * (s - c)));
        Log.d("CALC", "Радиус: " + Radius);

        // Вычисление центра окружности
        double b1 = a * a * (b * b + c * c - a * a);
        Log.d("CALC", "b1: " + b1);
        double b2 = b * b * (a * a + c * c - b * b);
        Log.d("CALC", "b2: " + b2);
        double b3 = c * c * (a * a + b * b - c * c);
        Log.d("CALC", "b3: " + b3);
        double bSum = b1 + b2 + b3;
        Log.d("CALC", "bSum: " + bSum);

        double c1 = (start[0] * b1 + mid[0] * b2 + end[0] * b3) / bSum;
        Log.d("CALC", "c1: " + c1);
        double c2 = (start[1] * b1 + mid[1] * b2 + end[1] * b3) / bSum;
        Log.d("CALC", "c2: " + c2);
        double c3 = (start[2] * b1 + mid[2] * b2 + end[2] * b3) / bSum;
        Log.d("CALC", "c3: " + c3);
        double[] center = {c1, c2, c3};
        Log.d("CALC", "Центр окружности: " + Arrays.toString(center));

        // Векторы от центра к точкам
        double[] R1 = {center[0] - start[0], center[1] - start[1], center[2] - start[2]};
        Log.d("CALC", "R1: " + Arrays.toString(R1));
        double[] R2 = {center[0] - mid[0], center[1] - mid[1], center[2] - mid[2]};
        Log.d("CALC", "R2: " + Arrays.toString(R2));
        double[] R3 = {center[0] - end[0], center[1] - end[1], center[2] - end[2]};
        Log.d("CALC", "R3: " + Arrays.toString(R3));

        // Вектор нормали (перпендикуляр к плоскости)
        double[] normalVec = {
                R1[2] * R2[1] - R1[1] * R2[2], // X
                R1[0] * R2[2] - R1[2] * R2[0],  // Y
                R1[1] * R2[0] - R1[0] * R2[1]   // Z
        };
        Log.d("CALC", "Вектор нормали: " + Arrays.toString(normalVec));

        // Регулировка направления нормали
        int status = 0;
        if (normalVec[1] < 0) {
            normalVec[0] = -normalVec[0];
            normalVec[1] = -normalVec[1];
            normalVec[2] = -normalVec[2];
            status = 1;
        }

        // Нормализация вектора
        double lengthNorm = Math.sqrt(
                normalVec[0] * normalVec[0] +
                        normalVec[1] * normalVec[1] +
                        normalVec[2] * normalVec[2]
        );
        normalVec[0] /= lengthNorm;
        normalVec[1] /= lengthNorm;
        normalVec[2] /= lengthNorm;
        Log.d("CALC", "Нормализованный вектор: [" + normalVec[0] + ", " + normalVec[1] + ", " + normalVec[2] + "]");

        // Углы поворота плоскости
        double rxVec = Math.toDegrees(Math.asin(normalVec[2]));
        Log.d("CALC", "rxVec: " + rxVec);
        double ryVec = 0;
        Log.d("CALC", "ryVec: " + ryVec);
        double rzVec = Math.toDegrees(Math.asin(-normalVec[0]));
        Log.d("CALC", "rzVec: " + rzVec);

        //Поворот осей Rx и Ry на 180 градусов в случае, когда значение Y вектора нормали было отрицательным
        if (status == 1) {
            rxVec = addAngle(rxVec, 180);
            ryVec = addAngle(ryVec, 180);
        }

        // Результат
        u = new double[]{
                center[0], center[1], center[2], // X, Y, Z
                rxVec, ryVec, rzVec              // Rx, Ry, Rz
        };
        Log.d("CALC", "Параметры новой рабочей плоскости: " + Arrays.toString(u));
    }

    @Override public void refreshConnectionState(boolean isConnected) {}
    @Override public void refreshPowerState(boolean isPowerOn) {}
    @Override public void refreshEnableState(boolean isEnable) {}
    @Override public void resetDragModeIcon(boolean isDrag) {
        if (isDrag) {
            drag_mode.setImageResource(R.drawable.ic_dragred);
        } else {
            drag_mode.setImageResource(R.drawable.ic_drag);
        }
    }
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