package com.example.myapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.myapplication.meteo.MeteoApi;
import com.example.myapplication.meteo.MeteoResponse;
import com.example.myapplication.meteo.DailyForecast;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WeatherActivity extends AppCompatActivity {
    private static final String TAG = "WeatherActivity";

    private FusedLocationProviderClient locClient;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ListView listForecast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        listForecast = findViewById(R.id.listForecast);
        locClient    = LocationServices.getFusedLocationProviderClient(this);

        requestPermissionLauncher = registerForActivityResult(
                new RequestPermission(),
                granted -> {
                    if (granted) {
                        fetchLastLocation();
                    } else {
                        Toast.makeText(
                                this,
                                "Location permission required for weather data",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );

        // Check / request
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fetchLastLocation();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void fetchLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locClient.getLastLocation()
                .addOnSuccessListener(loc -> {
                    if (loc != null) {
                        fetchForecast(loc.getLatitude(), loc.getLongitude());
                    } else {
                        Toast.makeText(
                                this,
                                "Unable to determine location",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(
                            this,
                            "Location error: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }


    private void fetchForecast(double lat, double lon) {
        // Add a logging interceptor so you see the full URL & response in Logcat
        HttpLoggingInterceptor logger = new HttpLoggingInterceptor(
                msg -> Log.d("OkHttp", msg)
        );
        logger.setLevel(HttpLoggingInterceptor.Level.BASIC);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logger)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.open-meteo.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        MeteoApi api = retrofit.create(MeteoApi.class);

        String dailyParams =
                "temperature_2m_max,temperature_2m_min," +
                        "windgusts_10m_max,uv_index_max,precipitation_sum";

        Call<MeteoResponse> call = api.getForecast(lat, lon, dailyParams, "auto");
        Log.d(TAG, "Request URL → " + call.request().url());
        call.enqueue(new Callback<MeteoResponse>() {
            @Override
            public void onResponse(
                    @NonNull Call<MeteoResponse> call,
                    @NonNull Response<MeteoResponse> response
            ) {
                if (response.isSuccessful() && response.body() != null) {
                    List<DailyForecast> list = mapToDailyForecasts(response.body());
                    computeSeverity(list);
                    showInListView(list);
                } else {
                    Toast.makeText(
                            WeatherActivity.this,
                            "Forecast error: " + response.code(),
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }
            @Override
            public void onFailure(
                    @NonNull Call<MeteoResponse> call,
                    @NonNull Throwable t
            ) {
                Toast.makeText(
                        WeatherActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private List<DailyForecast> mapToDailyForecasts(MeteoResponse resp) {
        List<DailyForecast> out = new ArrayList<>();
        MeteoResponse.Daily d = resp.daily;
        for (int i = 0; i < d.time.size(); i++) {
            DailyForecast f = new DailyForecast();
            f.date          = d.time.get(i);
            f.tempMax       = d.temperature2mMax.get(i);
            f.tempMin       = d.temperature2mMin.get(i);
            f.windGust      = d.windgusts10mMax.get(i);
            f.uvIndex       = d.uvIndexMax.get(i);
            f.precipitation = d.precipitationSum.get(i);
            out.add(f);
        }
        return out;
    }

    private void computeSeverity(List<DailyForecast> list) {
        for (DailyForecast f : list) {
            double score = 0;
            if      (f.windGust   > 15) score += 4;
            else if (f.windGust   > 10) score += 2;
            if      (f.uvIndex    >  8) score += 2;
            else if (f.uvIndex    >  5) score += 1;
            if      (f.precipitation >  5) score -= 3;
            else if (f.precipitation == 0) score += 3;
            if      (f.tempMax > 30 || f.tempMin <  5) score += 2;
            f.severity = (int)Math.max(0, Math.min(10, Math.round(score)));
        }
    }

    /** Push our results into the ListView */
    private void showInListView(List<DailyForecast> list) {
        List<String> display = new ArrayList<>();
        for (DailyForecast f : list) {
            display.add(String.format(
                    "%s: %.0f°/%.0f°  wind %.1f m/s  risk %d/10",
                    f.date, f.tempMax, f.tempMin, f.windGust, f.severity
            ));
        }
        listForecast.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                display
        ));
    }
}
