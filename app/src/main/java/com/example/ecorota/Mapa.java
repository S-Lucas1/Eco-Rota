package com.example.ecorota;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ecorota.model.Lixeira;
import com.example.ecorota.service.LixeiraService;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;
import java.text.DecimalFormat;

public class Mapa extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "Mapa";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa);
        
        // Configurar BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.nav_mapa);
        
        // Configurar o botão de gerar rota para lixeiras cheias
        Button btnGerarRota = findViewById(R.id.btnGerarRotaLixeirasChecias);
        btnGerarRota.setOnClickListener(v -> {
            gerarRotaLixeirasChecias();
        });
    }
    
    /**
     * Gera uma rota no Google Maps com as lixeiras que estão com nível "CHEIA"
     */
    private void gerarRotaLixeirasChecias() {
        // Obter a lista de lixeiras com nível alto
        LixeiraService lixeiraService = LixeiraService.getInstance();
        List<Lixeira> lixeirasCheia = lixeiraService.getLixeirasComNivelAlto();
        
        if (lixeirasCheia.isEmpty()) {
            Toast.makeText(this, "Não há lixeiras cheias para coletar no momento!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Construir a URL para o Google Maps
        StringBuilder mapUrl = new StringBuilder("https://www.google.com/maps/dir/?api=1");
        
        // O primeiro ponto é a origem (usamos a primeira lixeira como origem)
        Lixeira primeiraLixeira = lixeirasCheia.get(0);
        mapUrl.append("&origin=").append(primeiraLixeira.getLatitude()).append(",").append(primeiraLixeira.getLongitude());
        
        // Adicionar um destino (última lixeira)
        Lixeira ultimaLixeira = lixeirasCheia.get(lixeirasCheia.size() - 1);
        mapUrl.append("&destination=").append(ultimaLixeira.getLatitude()).append(",").append(ultimaLixeira.getLongitude());
        
        // Adicionar pontos intermediários (waypoints)
        if (lixeirasCheia.size() > 2) {
            StringBuilder waypoints = new StringBuilder();
            DecimalFormat df = new DecimalFormat("0.000000");
            
            for (int i = 1; i < lixeirasCheia.size() - 1; i++) {
                Lixeira lixeira = lixeirasCheia.get(i);
                if (i > 1) waypoints.append("|");
                waypoints.append(df.format(lixeira.getLatitude())).append(",").append(df.format(lixeira.getLongitude()));
            }
            
            if (waypoints.length() > 0) {
                mapUrl.append("&waypoints=").append(Uri.encode(waypoints.toString()));
            }
        }
        
        // Definir o modo de transporte como dirigindo
        mapUrl.append("&travelmode=driving");
        
        // Criar a intent para abrir o Google Maps
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mapUrl.toString()));
        intent.setPackage("com.google.android.apps.maps");
        
        // Verificar se o Google Maps está instalado
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            // Se não tiver o app, abra no navegador
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mapUrl.toString()));
            startActivity(intent);
        }
        
        Log.d(TAG, "URL do Google Maps: " + mapUrl.toString());
        Toast.makeText(this, "Abrindo rota para " + lixeirasCheia.size() + " lixeiras cheias", Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        
        if (itemId == R.id.nav_home) {
            startActivity(new Intent(this, InicioActivity.class));
            return true;
        } else if (itemId == R.id.nav_mapa) {
            // Já estamos na tela do Mapa
            return true;
        } else if (itemId == R.id.nav_lixeiras) {
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
