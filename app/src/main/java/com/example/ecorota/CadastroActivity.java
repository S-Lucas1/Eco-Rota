package com.example.ecorota;

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

import com.example.ecorota.model.Usuario;
import com.example.ecorota.services.UsuarioFirestoreService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class CadastroActivity extends AppCompatActivity {

    private static final String TAG = "CadastroActivity";
    private EditText edtNome, edtEmail, edtTelefone, edtSenha, edtConfirmarSenha;
    private Button btnCadastrar, btnVoltar;
    private ProgressBar progressBar;
    private UsuarioFirestoreService usuarioService;    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        // Inicialização do serviço Firestore
        usuarioService = UsuarioFirestoreService.getInstance();

        // Inicialização dos componentes da UI
        edtNome = findViewById(R.id.edtNomeCadastro);
        edtEmail = findViewById(R.id.edtEmailCadastro);
        edtTelefone = findViewById(R.id.edtTelefoneCadastro);
        edtSenha = findViewById(R.id.edtSenhaCadastro);
        edtConfirmarSenha = findViewById(R.id.edtConfirmarSenhaCadastro);
        btnCadastrar = findViewById(R.id.btnCadastrar);
        btnVoltar = findViewById(R.id.btnVoltar);
        progressBar = findViewById(R.id.progressBarCadastro);

        // Configuração dos listeners
        btnCadastrar.setOnClickListener(v -> realizarCadastro());
        btnVoltar.setOnClickListener(v -> voltarParaLogin());
    }    private void realizarCadastro() {
        // Obtenção e validação dos dados de entrada
        String nome = edtNome.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String telefone = edtTelefone.getText().toString().trim();
        String senha = edtSenha.getText().toString();
        String confirmarSenha = edtConfirmarSenha.getText().toString();

        // Validações básicas
        if (TextUtils.isEmpty(nome)) {
            edtNome.setError("Nome é obrigatório");
            return;
        }

        if (TextUtils.isEmpty(email)) {
            edtEmail.setError("Email é obrigatório");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Email inválido");
            return;
        }

        if (TextUtils.isEmpty(telefone)) {
            edtTelefone.setError("Telefone é obrigatório");
            return;
        }

        if (TextUtils.isEmpty(senha)) {
            edtSenha.setError("Senha é obrigatória");
            return;
        }

        if (TextUtils.isEmpty(confirmarSenha)) {
            edtConfirmarSenha.setError("Confirme sua senha");
            return;
        }

        if (!senha.equals(confirmarSenha)) {
            edtConfirmarSenha.setError("As senhas não conferem");
            return;
        }

        // Mostrar o indicador de progresso e desabilitar o botão
        progressBar.setVisibility(View.VISIBLE);
        btnCadastrar.setEnabled(false);

        // Criação do novo usuário (por padrão como usuário comum)
        Usuario novoUsuario = new Usuario();
        novoUsuario.setNome(nome);
        novoUsuario.setEmail(email);
        novoUsuario.setTelefone(telefone);
        novoUsuario.setSenha(senha);
        novoUsuario.setFuncao("USUARIO"); // Função padrão para novos cadastros

        // Verifica se já existe usuário com o mesmo email
        usuarioService.verificarExistenciaUsuario(email, new OnCompleteListener<Boolean>() {
            @Override
            public void onComplete(@NonNull Task<Boolean> taskExiste) {
                if (taskExiste.isSuccessful() && taskExiste.getResult()) {
                    // Email já cadastrado
                    progressBar.setVisibility(View.GONE);
                    btnCadastrar.setEnabled(true);
                    edtEmail.setError("Email já cadastrado");
                    edtEmail.requestFocus();
                } else {
                    // Não existe, prossegue cadastro
                    usuarioService.salvarUsuario(novoUsuario, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            progressBar.setVisibility(View.GONE);
                            btnCadastrar.setEnabled(true);
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Usuário cadastrado com sucesso!");
                                Toast.makeText(CadastroActivity.this, "Cadastro realizado com sucesso!", Toast.LENGTH_LONG).show();
                                finish();
                            } else {
                                Log.e(TAG, "Erro ao cadastrar usuário", task.getException());
                                Toast.makeText(CadastroActivity.this,
                                        "Erro ao cadastrar: " + (task.getException() != null ? task.getException().getMessage() : "Erro desconhecido"),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    private void voltarParaLogin() {
        finish();
    }
}