package com.example.ecorota;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.ecorota.model.Lixeira;
import com.example.ecorota.model.Sensor;
import com.example.ecorota.repository.LixeiraRepository;
import com.example.ecorota.repository.LixeiraFirebaseRepository;
import com.example.ecorota.util.LixeiraFileUtil;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Date;

public class CadastroLixeiraActivity extends AppCompatActivity {

    private static final String TAG = "CadastroLixeiraActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private TextInputEditText etLatitude, etLongitude, etNivel, etID;    private ImageView ivLixeiraImagem;
    private Button btnLocalizacaoAtual, btnSalvarLixeira;

    private LixeiraRepository lixeiraRepository;
    private LixeiraFirebaseRepository lixeiraFirebaseRepository;
    private String lixeiraId = null; // null para nova lixeira, ID específico para edição

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_lixeira);        // Inicializar repositories
        lixeiraRepository = LixeiraRepository.getInstance();
        lixeiraRepository.inicializar(this);
        
        // Inicializar repository do Firebase
        lixeiraFirebaseRepository = LixeiraFirebaseRepository.getInstance();
        lixeiraFirebaseRepository.setContext(this);

        // Inicializar elementos de UI
        etID = findViewById(R.id.etID);
        etLatitude = findViewById(R.id.etLatitude);
        etLongitude = findViewById(R.id.etLongitude);
        etNivel = findViewById(R.id.etNivel);
        ivLixeiraImagem = findViewById(R.id.ivLixeiraImagem);
        btnLocalizacaoAtual = findViewById(R.id.btnLocalizacaoAtual);
        btnSalvarLixeira = findViewById(R.id.btnSalvarLixeira);

        // Verificar se é para editar uma lixeira existente
        if (getIntent().hasExtra("lixeira_id")) {
            lixeiraId = getIntent().getStringExtra("lixeira_id");
            carregarDadosLixeira();
        } else {
            // Gerar um ID padrão automático quando for uma nova lixeira
            etID.setText("L" + System.currentTimeMillis() % 10000);
        }

        // Configurar botão de localização atual
        btnLocalizacaoAtual.setOnClickListener(v -> {
            verificarPermissaoLocalizacao();
        });

        // Configurar botão de salvar
        btnSalvarLixeira.setOnClickListener(v -> {
            salvarLixeira();
        });
    }    private void carregarDadosLixeira() {
        if (lixeiraId != null) {
            // Primeiro tentar carregar do Firebase
            lixeiraFirebaseRepository.getLixeira(lixeiraId, new LixeiraFirebaseRepository.LixeiraCallback() {
                @Override
                public void onLixeiraCarregada(Lixeira lixeira) {
                    // Preencher os campos com os dados da lixeira do Firebase
                    preencherCamposComDadosLixeira(lixeira);
                    Log.d(TAG, "Lixeira carregada do Firebase: " + lixeira.getID());
                      // Atualizar o repositório local com os dados mais recentes
                    lixeiraRepository.salvarLixeira(lixeira);
                }
                
                @Override
                public void onErro(String erro) {
                    Log.e(TAG, "Erro ao carregar lixeira do Firebase: " + erro);
                    
                    // Se falhar, tenta carregar da base local
                    Lixeira lixeira = lixeiraRepository.getLixeiraPorId(lixeiraId);
                    if (lixeira != null) {
                        preencherCamposComDadosLixeira(lixeira);
                        Log.d(TAG, "Lixeira carregada do repositório local: " + lixeira.getID());
                    } else {
                        Toast.makeText(CadastroLixeiraActivity.this, "Não foi possível encontrar a lixeira com ID: " + lixeiraId, Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
    
    private void preencherCamposComDadosLixeira(Lixeira lixeira) {
        runOnUiThread(() -> {
            etID.setText(lixeira.getID());
            etLatitude.setText(String.valueOf(lixeira.getLatitude()));
            etLongitude.setText(String.valueOf(lixeira.getLongitude()));
            etNivel.setText(String.valueOf(lixeira.getNivelEnchimento()));
            
            // Carregar a imagem (neste momento, sempre usamos a imagem padrão)
            // Se quisermos carregar imagens diferentes no futuro, podemos usar:
            // int resourceId = getResources().getIdentifier(lixeira.getImagemPath(), "drawable", getPackageName());
            // ivLixeiraImagem.setImageResource(resourceId);
        });
    }

    private void verificarPermissaoLocalizacao() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                LOCATION_PERMISSION_REQUEST_CODE
            );
        } else {
            obterLocalizacaoAtual();
        }
    }

    @SuppressLint("MissingPermission")
    private void obterLocalizacaoAtual() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        
        if (locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            
            if (location != null) {
                etLatitude.setText(String.valueOf(location.getLatitude()));
                etLongitude.setText(String.valueOf(location.getLongitude()));
                Toast.makeText(this, "Localização obtida com sucesso!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Não foi possível obter a localização. Tente novamente.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "GPS não está habilitado. Por favor, habilite o GPS nas configurações.", Toast.LENGTH_LONG).show();
        }
    }

    private void salvarLixeira() {
        // Validar dados
        if (TextUtils.isEmpty(etID.getText()) ||
            TextUtils.isEmpty(etLatitude.getText()) || 
            TextUtils.isEmpty(etLongitude.getText()) || 
            TextUtils.isEmpty(etNivel.getText())) {
            Toast.makeText(this, "Todos os campos são obrigatórios!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String id = etID.getText().toString().trim();
            float latitude = Float.parseFloat(etLatitude.getText().toString());
            float longitude = Float.parseFloat(etLongitude.getText().toString());
            float nivel = Float.parseFloat(etNivel.getText().toString());

            // Validar valores
            if (nivel < 0 || nivel > 1) {
                Toast.makeText(this, "O nível deve estar entre 0.0 e 1.0", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Verificar se o ID começa com 'L'
            if (!id.startsWith("L")) {
                Toast.makeText(this, "O ID da lixeira deve começar com 'L'", Toast.LENGTH_SHORT).show();
                return;
            }

            // Criar ou atualizar lixeira
            Lixeira lixeira;
            
            if (lixeiraId != null) {
                // Atualizar lixeira existente
                lixeira = lixeiraRepository.getLixeiraPorId(lixeiraId);
                
                if (lixeira != null) {
                    lixeira.setID(id); // Atualizar o ID também
                    lixeira.setLatitude(latitude);
                    lixeira.setLongitude(longitude);
                    lixeira.atualizarNivel(nivel);
                } else {
                    // Caso a lixeira não seja encontrada, criar uma nova
                    lixeira = new Lixeira(id, latitude, longitude, nivel);
                    gerarSensor(lixeira);
                }
            } else {
                // Criar nova lixeira
                lixeira = new Lixeira(id, latitude, longitude, nivel);
                gerarSensor(lixeira);
            }            // Salvar no repository local
            Log.d(TAG, "Salvando lixeira: " + lixeira.getID() + ", Lat: " + lixeira.getLatitude() + 
                  ", Long: " + lixeira.getLongitude() + ", Nível: " + lixeira.getNivelEnchimento());
                  
            lixeiraRepository.salvarLixeira(lixeira);

            // Salvar no arquivo JSON
            boolean sucessoLocal = LixeiraFileUtil.adicionarLixeira(this, lixeira);
            
            if (sucessoLocal) {
                // Também salvar no Firebase
                lixeiraFirebaseRepository.salvarLixeira(lixeira, new LixeiraFirebaseRepository.OperacaoCallback() {
                    @Override
                    public void onSucesso() {
                        runOnUiThread(() -> {
                            Toast.makeText(CadastroLixeiraActivity.this, "Lixeira salva localmente e no Firebase com sucesso!", Toast.LENGTH_SHORT).show();
                            finish(); // Retornar para a tela de listagem
                        });
                    }
                    
                    @Override
                    public void onErro(String erro) {
                        runOnUiThread(() -> {
                            Log.e(TAG, "Erro ao salvar lixeira no Firebase: " + erro);
                            Toast.makeText(CadastroLixeiraActivity.this, 
                                "Lixeira salva localmente, mas ocorreu um erro ao sincronizar com o Firebase: " + erro, 
                                Toast.LENGTH_LONG).show();
                            finish(); // Retornar para a tela de listagem mesmo com o erro no Firebase
                        });
                    }
                });
            } else {
                Toast.makeText(this, "Erro ao salvar lixeira no armazenamento local. Verifique os logs.", Toast.LENGTH_LONG).show();
            }
            
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Valores inválidos! Use números para latitude, longitude e nível.", Toast.LENGTH_LONG).show();
        }
    }
    
    private void gerarSensor(Lixeira lixeira) {
        // Gerar um novo sensor para a lixeira
        String sensorId;
        if (lixeira.getID() != null && !lixeira.getID().isEmpty()) {
            // Usar ID basedo no ID da lixeira
            sensorId = "S" + lixeira.getID().substring(1);
        } else {
            // Gerar um ID aleatório para o sensor
            sensorId = "S" + System.currentTimeMillis() % 10000;
        }
        
        // Criar um sensor aleatório (ultrassônico ou infravermelho)
        String[] tiposSensor = {"ULTRASSONICO", "INFRAVERMELHO", "PESO"};
        String tipoSensor = tiposSensor[(int) (Math.random() * tiposSensor.length)];
        
        Sensor sensor = new Sensor(sensorId, tipoSensor, "ATIVO", lixeira.getNivelEnchimento());
        lixeira.setSensor(sensor);
        sensor.setLixeira(lixeira);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            obterLocalizacaoAtual();
        } else {
            Toast.makeText(this, "Permissão de localização é necessária para usar a localização atual.", 
                    Toast.LENGTH_LONG).show();
        }
    }
}
