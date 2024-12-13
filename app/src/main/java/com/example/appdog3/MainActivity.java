package com.example.appdog3;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.InputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ImageView dogImageView;
    private Spinner spinnerBreeds;
    private Button playButton;
    private Button stopButton;
    private MediaPlayer mediaPlayer;
    private static final String BASE_URL = "https://api.thedogapi.com/v1/";
    private static final String API_KEY = "live_EJ9eQ26ZtCvJGTJ9zObzOTsznylR28szUlSJiD2EsIsrZ72tooGPOwINMu047Meb";
    private static final String DOG_SOUND_URL = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3";

    private Retrofit retrofit;
    private DogApi dogApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dogImageView = findViewById(R.id.dogImageView);
        spinnerBreeds = findViewById(R.id.spinnerBreeds);
        playButton = findViewById(R.id.playButton);
        stopButton = findViewById(R.id.stopButton);  // Inicialización del botón de detener
        mediaPlayer = new MediaPlayer();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        dogApi = retrofit.create(DogApi.class);

        loadDogBreeds();
        playButton.setOnClickListener(v -> playDogSound());
        stopButton.setOnClickListener(v -> stopDogSound());  // Configurar el listener para el botón de detener
    }

    private void loadDogBreeds() {
        dogApi.getDogBreeds("api_key=" + API_KEY).enqueue(new Callback<List<DogBreed>>() {
            @Override
            public void onResponse(Call<List<DogBreed>> call, Response<List<DogBreed>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<DogBreed> breeds = response.body();
                    String[] breedNames = new String[breeds.size()];

                    for (int i = 0; i < breeds.size(); i++) {
                        breedNames[i] = breeds.get(i).getName();
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, breedNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerBreeds.setAdapter(adapter);

                    spinnerBreeds.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                            loadDogImage(breeds.get(position).getId());
                        }

                        @Override
                        public void onNothingSelected(android.widget.AdapterView<?> parent) {
                            // Manejar el caso donde no se selecciona nada
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<List<DogBreed>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error al cargar las razas", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadDogImage(String breedId) {
        dogApi.getDogImage("api_key=" + API_KEY, breedId).enqueue(new Callback<List<Dog>>() {
            @Override
            public void onResponse(Call<List<Dog>> call, Response<List<Dog>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    String imageUrl = response.body().get(0).getUrl();
                    new DownloadImageTask(dogImageView).execute(imageUrl);
                }
            }

            @Override
            public void onFailure(Call<List<Dog>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void playDogSound() {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(DOG_SOUND_URL);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "Error al reproducir el sonido", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopDogSound() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.prepareAsync();  // Preparar el MediaPlayer para una nueva reproducción
            Toast.makeText(MainActivity.this, "Sonido detenido", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    interface DogApi {
        @GET("breeds")
        Call<List<DogBreed>> getDogBreeds(@Query("api_key") String apiKey);

        @GET("images/search")
        Call<List<Dog>> getDogImage(@Query("api_key") String apiKey, @Query("breed_ids") String breedId);
    }

    public static class DogBreed {
        private String id;
        private String name;

        public String getId() { return id; }
        public String getName() { return name; }
    }

    public static class Dog {
        private String url;

        public String getUrl() { return url; }
    }

    private static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        private ImageView imageView;

        public DownloadImageTask(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(String... urls) {
            try {
                InputStream in = new java.net.URL(urls[0]).openStream();
                return BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (result != null && imageView != null) {
                imageView.setImageBitmap(result);
            }
        }
    }
}
