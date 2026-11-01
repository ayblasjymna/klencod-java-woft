package com.example.wifiseclab;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView listViewNetworks;
    private ArrayAdapter<String> adapter;
    private List<String> networkNames;
    private ProgressBar mainProgressBar;

    // شبكات تجريبية (محاكاة فقط): الاسم، العنوان، التشفير، كلمة المرور
    private final String[][] mockNetworks = {
        {"Home_Network_5G", "A1:B2:C3:D4:E5:F6", "WPA2", "password123"},
        {"CoffeeShop_Guest", "12:34:56:78:9A:BC", "WPA2", "admin"},
        {"Secure_Office_Lab", "FF:EE:DD:CC:BB:AA", "WPA3", "secret_key_2026"}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // بناء الواجهة الرئيسية برمجياً لتجنب أي أخطاء في ملفات الـ XML
        LinearLayout rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setBackgroundColor(Color.parseColor("#0F0F13"));
        rootLayout.setPadding(30, 30, 30, 30);
        rootLayout.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        TextView title = new TextView(this);
        title.setText("محطة فحص وأمان الويفاي");
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 20, 0, 20);
        rootLayout.addView(title);

        Button btnScan = new Button(this);
        btnScan.setText("بحث عن شبكات الويفاي المحيطة");
        btnScan.setBackgroundColor(Color.parseColor("#6200EE"));
        btnScan.setTextColor(Color.WHITE);
        rootLayout.addView(btnScan);

        mainProgressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        mainProgressBar.setIndeterminate(true);
        mainProgressBar.setVisibility(View.GONE);
        rootLayout.addView(mainProgressBar);

        listViewNetworks = new ListView(this);
        rootLayout.addView(listViewNetworks);

        setContentView(rootLayout);

        networkNames = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, networkNames);
        listViewNetworks.setAdapter(adapter);

        btnScan.setOnClickListener(v -> startNetworkScan());

        listViewNetworks.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < mockNetworks.length) {
                showAttackDialog(mockNetworks[position]);
            }
        });
    }

    private void startNetworkScan() {
        mainProgressBar.setVisibility(View.VISIBLE);
        networkNames.clear();
        adapter.notifyDataSetChanged();
        Toast.makeText(this, "جاري فحص الشبكات المحيطة...", Toast.LENGTH_SHORT).show();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isFinishing() || isDestroyed()) return;
            mainProgressBar.setVisibility(View.GONE);
            for (String[] net : mockNetworks) {
                networkNames.add(net[0] + " | التشفير: " + net[2]);
            }
            adapter.notifyDataSetChanged();
            Toast.makeText(this, "تم العثور على " + mockNetworks.length + " شبكات", Toast.LENGTH_SHORT).show();
        }, 2000);
    }

    private void showAttackDialog(String[] network) {
        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setBackgroundColor(Color.parseColor("#1A1A24"));
        dialogLayout.setPadding(40, 40, 40, 40);
        dialogLayout.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        TextView tvTarget = new TextView(this);
        tvTarget.setText("الهدف: " + network[0]);
        tvTarget.setTextColor(Color.WHITE);
        tvTarget.setTextSize(18);
        dialogLayout.addView(tvTarget);

        TextView tvStatus = new TextView(this);
        tvStatus.setText("جاهز لبدء الهجوم المعجمي (Dictionary Attack)");
        tvStatus.setTextColor(Color.parseColor("#03DAC5"));
        tvStatus.setPadding(0, 20, 0, 10);
        dialogLayout.addView(tvStatus);

        ProgressBar attackProgress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        dialogLayout.addView(attackProgress);

        TextView tvResult = new TextView(this);
        tvResult.setTextColor(Color.parseColor("#03DAC5"));
        tvResult.setTextSize(16);
        tvResult.setPadding(0, 20, 0, 10);
        dialogLayout.addView(tvResult);

        Button btnStartAttack = new Button(this);
        btnStartAttack.setText("بدء اختبار التخمين");
        btnStartAttack.setBackgroundColor(Color.parseColor("#03DAC5"));
        btnStartAttack.setTextColor(Color.BLACK);
        dialogLayout.addView(btnStartAttack);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogLayout)
                .create();

        String targetPassword = network[3];
        String[] wordlist = {
                "12345678", "qwertyuiop", "admin123", "letmein",
                "password", "welcome1", "admin", "password123", "secret_key_2026"
        };

        final boolean[] isAttacking = {false};
        final Handler handler = new Handler(Looper.getMainLooper());
        final Runnable[] attackRunnable = new Runnable[1];

        // إيقاف المحاكاة تلقائياً عند إغلاق النافذة لتجنب تسرب الذاكرة
        dialog.setOnDismissListener(d -> {
            isAttacking[0] = false;
            if (attackRunnable[0] != null) {
                handler.removeCallbacks(attackRunnable[0]);
            }
        });

        dialog.show();

        btnStartAttack.setOnClickListener(v -> {
            if (!isAttacking[0]) {
                isAttacking[0] = true;
                btnStartAttack.setText("إيقاف الهجوم");
                attackProgress.setMax(wordlist.length);
                attackProgress.setProgress(0);
                tvResult.setText("");

                final int[] index = {0};
                attackRunnable[0] = new Runnable() {
                    @Override
                    public void run() {
                        if (!isAttacking[0]) return;

                        if (index[0] < wordlist.length) {
                            String currentWord = wordlist[index[0]];
                            tvStatus.setText("جاري التجربة: " + currentWord);
                            attackProgress.setProgress(index[0] + 1);

                            if (currentWord.equals(targetPassword)) {
                                isAttacking[0] = false;
                                tvStatus.setText("تم العثور على كلمة المرور بنجاح!");
                                tvResult.setText("الباسورد الصحيح: " + currentWord);
                                btnStartAttack.setText("بدء اختبار التخمين");
                                return;
                            }

                            index[0]++;
                            handler.postDelayed(this, 400);
                        } else {
                            isAttacking[0] = false;
                            tvStatus.setText("انتهت القائمة، لم يتم العثور على تطابق.");
                            btnStartAttack.setText("بدء اختبار التخمين");
                        }
                    }
                };
                handler.post(attackRunnable[0]);
            } else {
                isAttacking[0] = false;
                handler.removeCallbacks(attackRunnable[0]);
                btnStartAttack.setText("بدء اختبار التخمين");
                tvStatus.setText("تم إيقاف الهجوم يدوياً.");
            }
        });
    }
}
