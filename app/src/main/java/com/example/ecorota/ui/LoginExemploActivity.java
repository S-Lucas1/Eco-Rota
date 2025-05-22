package com.example.ecorota.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ecorota.R;
import com.example.ecorota.model.Usuario;
import com.example.ecorota.services.UsuarioFirestoreService;
import com.example.ecorota.utils.AutenticacaoManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

/**
 * Exemplo de Activity de Login utilizando o Firebase Firestore
 * Substitua com sua implementação real
 */
public class LoginExemploActivity extends AppCompatActivity {
    
    private EditText editTextEmail;
    private EditText editTextSenha;
    private Button buttonLogin;
    private Button buttonRegistrar;
    private ProgressBar progressBar;
    
    private AutenticacaoManager autenticacaoManager;
    private UsuarioFirestoreService usuarioService;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_login); // Descomente quando tiver o layout
        
        // Inicializa os serviços
        autenticacaoManager = AutenticacaoManager.getInstance();
        usuarioService = UsuarioFirestoreService.getInstance();
        
        // Inicializa as views
        // Substitua por suas próprias referências
        /*
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextSenha = findViewById(R.id.editTextSenha);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonRegistrar = findViewById(R.id.buttonRegistrar);
        progressBar = findViewById(R.id.progressBar);
        */
        
        // Verifica se o usuário já está logado
        if (autenticacaoManager.isUsuarioLogado(this)) {
            autenticacaoManager.recuperarUsuarioLogado(this, new AutenticacaoManager.LoginCallback() {
                @Override
                public void onSucesso(Usuario usuario) {
                    // Navega para a próxima tela
                    Toast.makeText(LoginExemploActivity.this, "Bem-vindo de volta, " + usuario.getNome(), Toast.LENGTH_SHORT).show();
                    // Implemente a navegação para sua tela principal
                    // Intent intent = new Intent(LoginExemploActivity.this, MainActivity.class);
                    // startActivity(intent);
                    // finish();
                }
                
                @Override
                public void onFalha(String mensagem) {
                    // Se não conseguiu recuperar, limpa as preferências
                    autenticacaoManager.logout(LoginExemploActivity.this);
                    Toast.makeText(LoginExemploActivity.this, mensagem, Toast.LENGTH_SHORT).show();
                }
            });
        }
        
        // Configura o botão de login
        /*
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editTextEmail.getText().toString().trim();
                String senha = editTextSenha.getText().toString().trim();
                
                if (email.isEmpty() || senha.isEmpty()) {
                    Toast.makeText(LoginExemploActivity.this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                progressBar.setVisibility(View.VISIBLE);
                realizarLogin(email, senha);
            }
        });
        
        // Configura o botão de registro
        buttonRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navegue para a tela de registro
                // Intent intent = new Intent(LoginExemploActivity.this, RegistroActivity.class);
                // startActivity(intent);
            }
        });
        */
    }
    
    /**
     * Realiza o login do usuário usando o Firebase Firestore
     */
    private void realizarLogin(String email, String senha) {
        autenticacaoManager.login(this, email, senha, new AutenticacaoManager.LoginCallback() {
            @Override
            public void onSucesso(Usuario usuario) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(LoginExemploActivity.this, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show();
                
                // Navega para a tela principal
                // Intent intent = new Intent(LoginExemploActivity.this, MainActivity.class);
                // startActivity(intent);
                // finish();
            }
            
            @Override
            public void onFalha(String mensagem) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(LoginExemploActivity.this, mensagem, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Exemplo de como criar um novo usuário
     */
    private void registrarNovoUsuario(Usuario novoUsuario) {
        usuarioService.salvarUsuario(novoUsuario, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(LoginExemploActivity.this, "Usuário registrado com sucesso!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginExemploActivity.this, "Erro ao registrar: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
