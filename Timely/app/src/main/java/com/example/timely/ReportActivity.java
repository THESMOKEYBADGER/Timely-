package com.example.timely;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ReportActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        // Retrieve userId from intent extras
        String userId = getIntent().getStringExtra("userId");

        BarChart barChart = findViewById(R.id.chart);

        // Fetch accumulated time data per category
        Category.calculateTotalTime(userId, new Category.TotalTimeCallback() {
            @Override
            public void onTotalTimeCalculated(HashMap<String, Integer> totalTime) {
                // Process the fetched data and populate the chart
                populateChart(barChart, totalTime);
            }

            @Override
            public void onFailure(Exception e) {
                // Handle failure to fetch data
            }
        });
    }

    private void populateChart(BarChart barChart, HashMap<String, Integer> totalTime) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> categories = new ArrayList<>(totalTime.keySet());

        for (int i = 0; i < categories.size(); i++) {
            String category = categories.get(i);
            int accumulatedTime = totalTime.get(category);
            entries.add(new BarEntry(i, accumulatedTime));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Categories");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);

        barChart.getDescription().setEnabled(false);
        barChart.invalidate();

        // Set category labels on X-axis
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(categories));
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setGranularity(1f);
        barChart.getXAxis().setGranularityEnabled(true);
    }
}
