package com.example.myapplication.meteo;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MeteoResponse {
    public Daily daily;

    public static class Daily {
        public List<String> time;
        @SerializedName("temperature_2m_max")
        public List<Double> temperature2mMax;
        @SerializedName("temperature_2m_min")
        public List<Double> temperature2mMin;
        @SerializedName("windgusts_10m_max")
        public List<Double> windgusts10mMax;
        @SerializedName("relativehumidity_2m_max")
        public List<Double> relativehumidity2mMax;
        @SerializedName("uv_index_max")
        public List<Double> uvIndexMax;
        @SerializedName("precipitation_sum")
        public List<Double> precipitationSum;
    }
}
