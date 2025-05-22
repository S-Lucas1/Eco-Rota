package com.example.ecorota;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ecorota.model.Usuario;
import com.example.ecorota.services.UsuarioFirestoreService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.security.ProviderInstaller;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private EditText edtEmail, edtSenha;
    private Button btnEntrar;
    private ProgressBar progressBar;
    private UsuarioFirestoreService usuarioService;

    // Código de solicitação para resolver problemas do Google Play Services
    private static final int PLAY_SERVICES_REQUEST_CODE = 9000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Verifica e atualiza o Google Play Services
        checkAndUpdateGooglePlayServices();

        // Inicializa o serviço de usuários no Firestore
        usuarioService = UsuarioFirestoreService.getInstance();

        // Inicializar elementos da UI
        edtEmail = findViewById(R.id.edtEmail);
        edtSenha = findViewById(R.id.edtSenha);
        btnEntrar = findViewById(R.id.btnEntrar);
        progressBar = findViewById(R.id.progressBar);
        Button btnQueroMeCadastrar = findViewById(R.id.btnQueroMeCadastrar);

        // (Opcional) implementar sessão salva com SharedPreferences, se necessário

        // Configurar o listener para o botão de cadastro
        btnQueroMeCadastrar.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, CadastroActivity.class);
            startActivity(intent);
        });

        btnEntrar.setOnClickListener(view -> {
            String email = edtEmail.getText().toString().trim();
            String senha = edtSenha.getText().toString().trim();
            
            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Mostra o indicador de progresso e desabilita o botão
            progressBar.setVisibility(View.VISIBLE);
            btnEntrar.setEnabled(false);
            
            Log.d(TAG, "Tentando login com email: " + email);

            // Autentica usando Firestore (coleção users)
            usuarioService.autenticarFirestore(email, senha, new OnCompleteListener<Usuario>() {
                @Override
                public void onComplete(@NonNull Task<Usuario> task) {
                    progressBar.setVisibility(View.GONE);
                    btnEntrar.setEnabled(true);
                    if (task.isSuccessful() && task.getResult() != null) {
                        Usuario usuario = task.getResult();
                        Log.d(TAG, "Login bem-sucedido como " + usuario.getNome());
                        Toast.makeText(LoginActivity.this, "Bem-vindo, " + usuario.getNome() + "!", Toast.LENGTH_SHORT).show();
                        irParaTelaInicial();
                    } else {
                        String erro = task.getException() != null
                            ? task.getException().getMessage() : "Email ou senha incorretos";
                        Log.w(TAG, "Login falhou: " + erro);
                        Toast.makeText(LoginActivity.this, erro, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });
    }

    /**
     * Navega para a tela inicial do app após login bem-sucedido
     */
    private void irParaTelaInicial() {
        Intent intent = new Intent(LoginActivity.this, GerarRotaActivity.class);
        // Adicionando flags para limpar a pilha de atividades e evitar o problema de voltar
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    /**
     * Verifica e atualiza o Google Play Services
     * Este método irá garantir que o Google Play Services esteja disponível e atualizado
     */
    private void checkAndUpdateGooglePlayServices() {
        Log.d(TAG, "Verificando status do Google Play Services");
        
        // Primeiro, tentamos atualizar o provider de segurança
        try {
            ProviderInstaller.installIfNeeded(this);
            Log.d(TAG, "Provider de segurança atualizado com sucesso");
        } catch (Exception e) {
            Log.e(TAG, "Erro ao instalar provider: " + e.getMessage());
            
            // Se falhou, verificamos o status do Google Play Services
            GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
            int resultCode = googleAPI.isGooglePlayServicesAvailable(this);
            
            if (resultCode != ConnectionResult.SUCCESS) {
                if (googleAPI.isUserResolvableError(resultCode)) {
                    // Mostra diálogo para resolver o problema
                    Log.d(TAG, "Erro resolvível pelo usuário: " + resultCode);
                    googleAPI.getErrorDialog(this, resultCode, PLAY_SERVICES_REQUEST_CODE).show();
                } else {
                    Log.e(TAG, "Este dispositivo não é suportado pelo Google Play Services");
                    Toast.makeText(this, 
                        "Este dispositivo não é compatível com os serviços necessários",
                        Toast.LENGTH_LONG).show();
                    finish();
                }
            } else {
                Log.d(TAG, "Google Play Services está disponível e atualizado");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == PLAY_SERVICES_REQUEST_CODE) {
            // O usuário retornou da resolução do Google Play Services
            GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
            int resultCodeAfter = googleAPI.isGooglePlayServicesAvailable(this);
            
            if (resultCodeAfter == ConnectionResult.SUCCESS) {
                Log.d(TAG, "Google Play Services foi atualizado");
                // Reinicia a atividade para garantir que tudo seja inicializado corretamente
                recreate();
            } else {
                Log.e(TAG, "Usuário não completou a atualização do Google Play Services");
                Toast.makeText(this, 
                    "Não foi possível atualizar os serviços necessários",
                    Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Limpar campos ao retornar para a tela de login
        edtEmail.setText("");
        edtSenha.setText("");
        progressBar.setVisibility(View.GONE);
        btnEntrar.setEnabled(true);
    }
}
