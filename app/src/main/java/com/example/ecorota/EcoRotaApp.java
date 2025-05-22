package com.example.ecorota;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.security.ProviderInstaller;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

public class EcoRotaApp extends Application {
    private static final String TAG = "EcoRotaApp";
      @Override
    public void onCreate() {
        super.onCreate();
        
        try {
            // Atualiza o Provider do Security para evitar problemas SSL em dispositivos antigos
            updateAndroidSecurityProvider(this);
            
            // Verifica a disponibilidade do Google Play Services
            checkGooglePlayServices();
            
            // Inicializa o Firebase de forma explícita
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseOptions options = new FirebaseOptions.Builder()
                        .setApplicationId("1:813166123897:android:f4ebdc8dd18373f6b2eda8")
                        .setProjectId("ecorota-2025")
                        .setApiKey("AIzaSyCjZzn4bja_CcqplA7avKskztgFKOLb6DQ")
                        .build();
                FirebaseApp.initializeApp(this, options);
                Log.d(TAG, "Firebase inicializado com sucesso");
            } else {
                Log.d(TAG, "Firebase já estava inicializado");
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao inicializar Firebase: " + e.getMessage(), e);
        }
    }    // Método para atualizar o Security Provider para evitar problemas com SSL
    private void updateAndroidSecurityProvider(Context context) {
        try {
            ProviderInstaller.installIfNeeded(context);
            Log.d(TAG, "Security Provider atualizado com sucesso");
        } catch (Exception e) {
            Log.e(TAG, "Erro ao atualizar o security provider: " + e.getMessage(), e);
            // Não tentamos chamar makeGooglePlayServicesAvailable pois precisa de uma Activity
            // e pode causar problemas em uma Application
        }
    }
    
    /**
     * Verifica se o Google Play Services está disponível e registra o resultado no log
     * Não podemos mostrar diálogos aqui pois estamos em uma Application, não uma Activity
     */
    private void checkGooglePlayServices() {
        try {
            GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
            int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
            if (resultCode == ConnectionResult.SUCCESS) {
                Log.d(TAG, "Google Play Services está disponível");
            } else {
                Log.e(TAG, "Google Play Services não está disponível: " + 
                      apiAvailability.getErrorString(resultCode));
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao verificar Google Play Services: " + e.getMessage(), e);
        }
    }
}
