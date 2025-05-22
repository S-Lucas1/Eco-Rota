package com.example.ecorota.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ecorota.R;
import com.example.ecorota.model.Usuario;
import com.example.ecorota.utils.AutenticacaoManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;

public class CadastroActivity extends AppCompatActivity {
    
    private static final String TAG = "CadastroActivity";
    
    // Elementos da UI
    private EditText edtNomeCadastro;
    private EditText edtEmailCadastro;
    private EditText edtTelefoneCadastro;
    private EditText edtSenhaCadastro;
    private EditText edtConfirmarSenhaCadastro;
    private Button btnCadastrar;
    private Button btnVoltar;
    private ProgressBar progressBar;
      // Gerenciador de autenticação
    private AutenticacaoManager autenticacaoManager;
      @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);
          // Inicialização do gerenciador de autenticação
        autenticacaoManager = AutenticacaoManager.getInstance();
        
        // Inicializa os elementos da UI
        inicializarElementos();
        
        // Configura os listeners para os botões
        configurarListeners();
    }
    
    private void inicializarElementos() {
        edtNomeCadastro = findViewById(R.id.edtNomeCadastro);
        edtEmailCadastro = findViewById(R.id.edtEmailCadastro);
        edtTelefoneCadastro = findViewById(R.id.edtTelefoneCadastro);
        edtSenhaCadastro = findViewById(R.id.edtSenhaCadastro);
        edtConfirmarSenhaCadastro = findViewById(R.id.edtConfirmarSenhaCadastro);
        btnCadastrar = findViewById(R.id.btnCadastrar);
        btnVoltar = findViewById(R.id.btnVoltar);
        progressBar = findViewById(R.id.progressBarCadastro);
    }
    
    private void configurarListeners() {
        btnCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cadastrarUsuario();
            }
        });
        
        btnVoltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Volta para a tela anterior (Login)
            }
        });
    }
      private void cadastrarUsuario() {
        // Obtém os valores dos campos
        String nome = edtNomeCadastro.getText().toString().trim();
        String email = edtEmailCadastro.getText().toString().trim();
        String telefone = edtTelefoneCadastro.getText().toString().trim();
        String senha = edtSenhaCadastro.getText().toString();
        String confirmarSenha = edtConfirmarSenhaCadastro.getText().toString();
        
        // Valida os campos
        if (!validarCampos(nome, email, telefone, senha, confirmarSenha)) {
            return;
        }
        
        // Mostrar o indicador de progresso e desabilitar o botão
        progressBar.setVisibility(View.VISIBLE);
        btnCadastrar.setEnabled(false);
        
        // Cria um novo usuário (o ID será gerado automaticamente no Firestore)
        Usuario novoUsuario = new Usuario();
        novoUsuario.setNome(nome);
        novoUsuario.setEmail(email);
        novoUsuario.setTelefone(telefone);
        novoUsuario.setSenha(senha);
        novoUsuario.setFuncao("USUARIO"); // Função padrão para novos cadastros
        
        Log.d(TAG, "Tentando salvar novo usuário no Firestore: " + nome + ", Email: " + email);
          // Cria o usuário usando o AutenticacaoManager que integra Firebase Auth e Firestore
        autenticacaoManager.criarUsuario(this, email, senha, novoUsuario, new AutenticacaoManager.LoginCallback() {
            @Override
            public void onSucesso(Usuario usuario) {
                progressBar.setVisibility(View.GONE);
                btnCadastrar.setEnabled(true);
                
                Log.d(TAG, "Usuário cadastrado com sucesso! ID: " + usuario.getID());
                Toast.makeText(CadastroActivity.this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show();
                
                // Volta para a tela de login
                finish();
            }
            
            @Override
            public void onFalha(String mensagem) {
                progressBar.setVisibility(View.GONE);
                btnCadastrar.setEnabled(true);
                
                Toast.makeText(CadastroActivity.this, mensagem, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Erro ao cadastrar usuário: " + mensagem);
            }
        });
    }
    
    private boolean validarCampos(String nome, String email, String telefone, String senha, String confirmarSenha) {
        // Verifica se todos os campos estão preenchidos
        if (TextUtils.isEmpty(nome) || TextUtils.isEmpty(email) || 
            TextUtils.isEmpty(telefone) || TextUtils.isEmpty(senha) || TextUtils.isEmpty(confirmarSenha)) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        // Valida o formato do email
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Digite um email válido", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        // Valida o formato do telefone (simplificado)
        if (telefone.length() < 10) {
            Toast.makeText(this, "Digite um telefone válido", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        // Verifica se as senhas são iguais
        if (!senha.equals(confirmarSenha)) {
            Toast.makeText(this, "As senhas não coincidem", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        // Verifica o tamanho da senha
        if (senha.length() < 6) {
            Toast.makeText(this, "A senha deve ter pelo menos 6 caracteres", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        return true;
    }
}
