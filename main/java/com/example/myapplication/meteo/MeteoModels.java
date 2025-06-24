package com.example.myapplication.meteo;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MeteoModels {
    public Daily daily;
    public static class Daily {
        public List<String> time;
        @SerializedName("temperature_2m_max")
        public List<Double> temperature_2m_max;
        @SerializedName("temperature_2m_min")
        public List<Double> temperature_2m_min;
        @SerializedName("windgusts_10m_max")
        public List<Double> windgusts_10m_max;
        @SerializedName("relativehumidity_2m_max")
        public List<Double> relativehumidity_2m_max;
        @SerializedName("uv_index_max")
        public List<Double> uv_index_max;
        @SerializedName("precipitation_sum")
        public List<Double> precipitation_sum;
    }

}

