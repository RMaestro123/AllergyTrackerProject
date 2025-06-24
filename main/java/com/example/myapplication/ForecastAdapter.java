package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.meteo.DailyForecast;
import java.util.List;

public class ForecastAdapter
        extends RecyclerView.Adapter<ForecastAdapter.VH> {

    private final List<DailyForecast> data;
    public ForecastAdapter(List<DailyForecast> data) {
        this.data = data;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvDate, tvScore;
        VH(View item) {
            super(item);
            tvDate  = item.findViewById(android.R.id.text1);
            tvScore = item.findViewById(android.R.id.text2);
        }
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int i) {
        DailyForecast f = data.get(i);
        h.tvDate.setText(f.date);
        h.tvScore.setText("Risk: " + f.severity + "/10");
    }

    @Override public int getItemCount() {
        return data.size();
    }
}
