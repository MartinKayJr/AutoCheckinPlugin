package cn.martinkay.autocheckinplugin;

import androidx.appcompat.app.AppCompatActivity;

import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TimePicker;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import cn.martinkay.autocheckinplugin.adapter.AutoConfigAdapter;
import cn.martinkay.autocheckinplugin.entity.AutoConfig;

public class MainActivityV2 extends AppCompatActivity {

    ListView checkinList;

    FloatingActionButton addCheckinButton;

    private boolean[] weekTimeChecked = new boolean[7];

    List<AutoConfig> autoConfigList;

    public SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_v2);

        sharedPreferences = getSharedPreferences("auto_checkin", MODE_PRIVATE);

        initViews();
        initData();
    }

    public void initData() {
        // 读取配置
        String autoConfigString = sharedPreferences.getString("auto_config_list", "");
        // 判断是否为空
        if (StringUtils.isEmpty(autoConfigString)){
            autoConfigList = new ArrayList<>();

        }else {
            // Gson解析 List<AutoConfig>
            autoConfigList = new Gson().fromJson(autoConfigString, new TypeToken<List<AutoConfig>>(){}.getType());
            AutoConfigAdapter autoConfigAdapter = new AutoConfigAdapter(MainActivityV2.this, autoConfigList);
            checkinList.setAdapter(autoConfigAdapter);
        }
    }

    public void initViews() {
        checkinList = findViewById(R.id.check_in_list);
        addCheckinButton = findViewById(R.id.add_rule_button);

        checkinList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // 确定删除吗
                MaterialAlertDialogBuilder confirmDialog = new MaterialAlertDialogBuilder(MainActivityV2.this);
                confirmDialog.setTitle("确定删除吗？");
                confirmDialog.setPositiveButton("确定删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        autoConfigList.remove(position);
                        AutoConfigAdapter autoConfigAdapter = new AutoConfigAdapter(MainActivityV2.this, autoConfigList);
                        checkinList.setAdapter(autoConfigAdapter);
                        // 保存配置
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("auto_config_list", new Gson().toJson(autoConfigList));
                        editor.commit();
                    }
                });
                confirmDialog.show();
                return false;
            }
        });

        addCheckinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View dialogView = LayoutInflater.from(MainActivityV2.this).inflate(R.layout.add_rule_layout, null);
                // 生效星期
                Button activeWeekTimeButton = dialogView.findViewById(R.id.active_week_time_button);
                activeWeekTimeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MaterialAlertDialogBuilder weekDialog = new MaterialAlertDialogBuilder(MainActivityV2.this);
                        weekDialog.setTitle("生效星期");
                        weekDialog.setMultiChoiceItems(R.array.week_time, weekTimeChecked, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                weekTimeChecked[which] = isChecked;
                            }
                        });
                        weekDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                StringBuilder stringBuilder = new StringBuilder();
                                for (int i = 0; i < weekTimeChecked.length; i++) {
                                    if (weekTimeChecked[i]) {
                                        stringBuilder.append(i + 1).append(",");
                                    }
                                }
                                if (stringBuilder.length() > 0) {
                                    stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                                }
                                activeWeekTimeButton.setText(stringBuilder.toString());
                            }
                        });
                        weekDialog.show();
                    }
                });

                // 签到时间
                Button activeTimeButton = dialogView.findViewById(R.id.active_time_button);
                activeTimeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 时间选择器 单位秒
                        TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivityV2.this, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(@NotNull TimePicker view, int hourOfDay, int minute) {
                                activeTimeButton.setText(hourOfDay + ":" + minute);
                            }
                        }, 0, 0, true);
                        timePickerDialog.show();
                    }
                });
                TextInputEditText nameEditText = dialogView.findViewById(R.id.name);
                TextInputEditText activeCountEditText = dialogView.findViewById(R.id.active_count);
                TextInputEditText nextActiveSecondsEditText = dialogView.findViewById(R.id.next_active_seconds);

                Button submitButton = dialogView.findViewById(R.id.submit);
                submitButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 判断是否为空 Toast提示
                        if (nameEditText.getText().toString().isEmpty()) {
                            nameEditText.setError("名称不能为空");
                            return;
                        }
                        if (activeCountEditText.getText().toString().isEmpty()) {
                            activeCountEditText.setError("触发次数不能为空");
                            return;
                        }
                        if (nextActiveSecondsEditText.getText().toString().isEmpty()) {
                            nextActiveSecondsEditText.setError("下一次触发间隔不能为空");
                            return;
                        }
                        if (activeWeekTimeButton.getText().toString().equals("生效星期")) {
                            activeWeekTimeButton.setError("生效星期不能为空");
                            return;
                        }
                        if (activeTimeButton.getText().toString().equals("触发时间")) {
                            activeTimeButton.setError("触发时间不能为空");
                            return;
                        }


                        // 名称
                        String name = nameEditText.getText().toString();
                        // 触发次数
                        Integer activeCount = Integer.valueOf(activeCountEditText.getText().toString());
                        // 下一次触发间隔
                        Integer nextActiveSeconds = Integer.valueOf(nextActiveSecondsEditText.getText().toString());
                        Log.i("MainActivity", "onClick: " + activeWeekTimeButton.getText().toString());

                        AutoConfig autoConfig = new AutoConfig();
                        autoConfig.setName(name);
                        autoConfig.setActiveCount(activeCount);
                        autoConfig.setNextActiveCount(nextActiveSeconds);
                        // weekTimeChecked 转为字符串
                        StringBuilder stringBuilder = new StringBuilder();
                        for (int i = 0; i < weekTimeChecked.length; i++) {
                            if (weekTimeChecked[i]) {
                                stringBuilder.append(i + 1).append(",");
                            }
                        }
                        autoConfig.setActiveWeek(stringBuilder.toString());
                        autoConfig.setActiveTime(activeTimeButton.getText().toString());
                        autoConfigList.add(autoConfig);
                        AutoConfigAdapter autoConfigAdapter = new AutoConfigAdapter(MainActivityV2.this, autoConfigList);
                        checkinList.setAdapter(autoConfigAdapter);

                        // 将list转为json字符串存储到本地
                        Gson gson = new Gson();
                        String json = gson.toJson(autoConfigList);
                        Log.i("MainActivity", "onClick: " + json);
                        sharedPreferences.edit().putString("auto_config_list", json).apply();

                    }
                });

                new MaterialAlertDialogBuilder(MainActivityV2.this)
                        .setTitle("添加签到规则")
                        .setView(dialogView)
                        .setNegativeButton("取消", null)
                        .show();
            }
        });
    }


}