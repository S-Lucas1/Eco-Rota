package com.example.ecorota;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class GerarRotaActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "GerarRotaActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gerar_rota);

        // Configurar o botão para gerar rota
        Button btnGerarRota = findViewById(R.id.btnGerarRota);
        btnGerarRota.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ir para a MapaActivity
                Intent intent = new Intent(GerarRotaActivity.this, Mapa.class);
                startActivity(intent);
            }
        });
        
        // Configurar BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        // Definimos o item selecionado adequado dependendo da tela
        if (this instanceof GerarRotaActivity) {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }
    }
    
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        
        if (itemId == R.id.nav_home) {
            startActivity(new Intent(this, InicioActivity.class));
            return true;
        } else if (itemId == R.id.nav_mapa) {
            // Navegar para a tela de mapa
            startActivity(new Intent(this, Mapa.class));
            return true;
        } else if (itemId == R.id.nav_lixeiras) {
            // Navegar para a tela de lixeiras
            startActivity(new Intent(this, LixeirasActivity.class));
            return true;
        } else if (itemId == R.id.nav_reciclagem) {
            // Navegação para tela de reciclagem
            try {
                Class<?> reciclagemActivityClass = Class.forName("com.example.ecorota.ReciclagemActivity");
                startActivity(new Intent(this, reciclagemActivityClass));
            } catch (ClassNotFoundException e) {
                Log.e(TAG, "Classe ReciclagemActivity não encontrada", e);
                // Fallback para a tela inicial se ReciclagemActivity não existir
                startActivity(new Intent(this, InicioActivity.class));
            }
            return true;
        }
        
        return false;
    }
}
