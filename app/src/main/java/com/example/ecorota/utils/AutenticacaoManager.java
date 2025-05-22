package com.example.ecorota.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.ecorota.model.Usuario;
import com.example.ecorota.services.UsuarioFirestoreService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Classe para gerenciar a autenticação do usuário usando Firestore
 */
public class AutenticacaoManager {
    private static final String TAG = "AutenticacaoManager";
    private static final String PREF_NAME = "EcoRotaPrefs";
    private static final String PREF_USER_ID = "userId";
    
    private static AutenticacaoManager instance;
    private final UsuarioFirestoreService usuarioService;
    private final FirebaseAuth mAuth;
    private Usuario usuarioLogado;
    
    private AutenticacaoManager() {
        usuarioService = UsuarioFirestoreService.getInstance();
        
        // Certifique-se de que o FirebaseAuth seja inicializado corretamente
        try {
            mAuth = FirebaseAuth.getInstance();
            if (mAuth == null) {
                Log.e(TAG, "FirebaseAuth não pôde ser inicializado");
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao inicializar FirebaseAuth: " + e.getMessage(), e);
            throw new RuntimeException("Erro crítico ao inicializar FirebaseAuth", e);
        }
    }
    
    public static AutenticacaoManager getInstance() {
        if (instance == null) {
            instance = new AutenticacaoManager();
        }
        return instance;
    }
      /**
     * Realiza login de usuário usando Firebase Auth
     */    /**
     * Realiza login de usuário usando Firebase Auth e recupera informações adicionais do Firestore
     */
    public void login(Context context, String email, String senha, final LoginCallback callback) {
        Log.d(TAG, "Tentando login com email: " + email);
        
        mAuth.signInWithEmailAndPassword(email, senha)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Login com Firebase Auth bem-sucedido
                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                    if (firebaseUser != null) {
                        Log.d(TAG, "Firebase Auth login bem-sucedido: " + firebaseUser.getUid());
                        
                        // Busca as informações adicionais do usuário no Firestore
                        // Usando o UID como chave primária para garantir correspondência
                        usuarioService.getUsuarioPorId(firebaseUser.getUid(), new OnCompleteListener<Usuario>() {
                            @Override
                            public void onComplete(@NonNull Task<Usuario> userTask) {
                                if (userTask.isSuccessful() && userTask.getResult() != null) {
                                    // Encontrou informações adicionais no Firestore
                                    usuarioLogado = userTask.getResult();
                                    Log.d(TAG, "Dados do usuário recuperados do Firestore: " + usuarioLogado.getNome());
                                } else {
                                    // Tenta buscar por email como fallback
                                    usuarioService.getUsuarioPorEmail(email, new OnCompleteListener<Usuario>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Usuario> emailTask) {
                                            if (emailTask.isSuccessful() && emailTask.getResult() != null) {
                                                usuarioLogado = emailTask.getResult();
                                                // Atualiza o ID para corresponder ao Firebase Auth
                                                if (!usuarioLogado.getID().equals(firebaseUser.getUid())) {
                                                    usuarioLogado.setID(firebaseUser.getUid());
                                                    // Atualiza o documento no Firestore
                                                    usuarioService.salvarUsuario(usuarioLogado, null);
                                                }
                                                Log.d(TAG, "Dados do usuário recuperados por email: " + usuarioLogado.getNome());
                                            } else {
                                                // Se não encontrou por email, cria um usuário básico
                                                Usuario novoUsuario = new Usuario();
                                                novoUsuario.setID(firebaseUser.getUid());
                                                novoUsuario.setEmail(firebaseUser.getEmail());
                                                novoUsuario.setNome(firebaseUser.getDisplayName() != null ? 
                                                                  firebaseUser.getDisplayName() : "Usuário");
                                                usuarioLogado = novoUsuario;
                                                
                                                // Salva o novo usuário no Firestore
                                                usuarioService.salvarUsuario(usuarioLogado, null);
                                                Log.d(TAG, "Novo usuário básico criado com ID: " + novoUsuario.getID());
                                            }
                                            
                                            // Salva o ID e retorna sucesso
                                            salvarIdUsuario(context, usuarioLogado.getID());
                                            callback.onSucesso(usuarioLogado);
                                        }
                                    });
                                    return;
                                }
                                
                                // Salva o ID do usuário nas preferências
                                salvarIdUsuario(context, usuarioLogado.getID());
                                callback.onSucesso(usuarioLogado);
                            }
                        });
                    } else {
                        callback.onFalha("Falha na autenticação: usuário Firebase nulo após login");
                    }
                } else {
                    Log.w(TAG, "Falha ao fazer login com Firebase Auth", task.getException());
                    String erro = "Email ou senha incorretos";
                    if (task.getException() != null) {
                        String errorMsg = task.getException().getMessage();
                        Log.e(TAG, "Erro detalhado: " + errorMsg);
                        
                        if (errorMsg != null && errorMsg.contains("credential is incorrect")) {
                            erro = "Email ou senha incorretos";
                        } else if (errorMsg != null && errorMsg.contains("no user record")) {
                            erro = "Usuário não encontrado";
                        } else if (errorMsg != null && errorMsg.contains("badly formatted")) {
                            erro = "Formato de email inválido";
                        }
                    }
                    callback.onFalha(erro);
                }
            });
    }
    
    /**
     * Salva ID do usuário logado nas preferências compartilhadas
     */
    private void salvarIdUsuario(Context context, String userId) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREF_USER_ID, userId);
        editor.apply();
    }
      /**
     * Recupera o usuário logado do Firebase Auth e as preferências locais
     */
    public void recuperarUsuarioLogado(Context context, final LoginCallback callback) {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        
        if (firebaseUser != null) {
            // Usuário já está logado no Firebase Auth
            String userId = firebaseUser.getUid();
            
            // Verifica se temos mais informações no Firestore
            usuarioService.getUsuarioPorId(userId, new OnCompleteListener<Usuario>() {
                @Override
                public void onComplete(@NonNull Task<Usuario> task) {
                    if (task.isSuccessful() && task.getResult() != null) {
                        usuarioLogado = task.getResult();
                        callback.onSucesso(usuarioLogado);
                    } else {
                        // Cria objeto de usuário básico com informações do Firebase Auth
                        Usuario usuario = new Usuario();
                        usuario.setID(firebaseUser.getUid());
                        usuario.setEmail(firebaseUser.getEmail());
                        usuario.setNome(firebaseUser.getDisplayName() != null ? 
                                      firebaseUser.getDisplayName() : "Usuário");
                        usuarioLogado = usuario;
                        callback.onSucesso(usuarioLogado);
                    }
                }
            });
        } else {
            // Tenta pegar o ID das preferências locais como fallback
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            String userId = prefs.getString(PREF_USER_ID, null);
            
            if (userId != null) {
                usuarioService.getUsuarioPorId(userId, new OnCompleteListener<Usuario>() {
                    @Override
                    public void onComplete(@NonNull Task<Usuario> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            usuarioLogado = task.getResult();
                            callback.onSucesso(usuarioLogado);
                        } else {
                            callback.onFalha("Sessão expirada, faça login novamente");
                            logout(context); // Limpa preferências locais
                        }
                    }
                });
            } else {
                callback.onFalha("Nenhum usuário logado");
            }
        }
    }
    
    /**
     * Realiza logout do usuário
     */
    public void logout(Context context) {
        // Desconecta do Firebase Auth
        mAuth.signOut();
        
        // Limpa preferências locais
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(PREF_USER_ID);
        editor.apply();
        
        usuarioLogado = null;
    }
    
    /**
     * Verifica se há usuário logado
     */
    public boolean isUsuarioLogado(Context context) {
        // Verifica primeiro no Firebase Auth
        if (mAuth.getCurrentUser() != null) {
            return true;
        }
        
        // Verifica nas preferências locais como fallback
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.contains(PREF_USER_ID);
    }
      /**
     * Obtém o usuário logado
     */
    public Usuario getUsuarioLogado() {
        return usuarioLogado;
    }
    
    /**
     * Cria um novo usuário com Firebase Auth
     */    /**
     * Cria um novo usuário com Firebase Auth e salva dados adicionais no Firestore
     */
    public void criarUsuario(Context context, String email, String senha, Usuario usuario, final LoginCallback callback) {
        // Registra a tentativa de criação para depuração
        Log.d(TAG, "Tentando criar usuário com email: " + email);
        
        mAuth.createUserWithEmailAndPassword(email, senha)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Usuário criado com sucesso no Auth
                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                    if (firebaseUser != null) {
                        // Atualiza o ID do usuário com o do Firebase Auth para garantir consistência
                        usuario.setID(firebaseUser.getUid());
                        Log.d(TAG, "Usuário criado no Firebase Auth com ID: " + firebaseUser.getUid());
                        
                        // Não armazenamos mais a senha no Firestore por segurança
                        usuario.setSenha(null);
                        
                        // Salva os dados adicionais no Firestore usando o UID como ID do documento
                        usuarioService.salvarUsuario(usuario, new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> saveTask) {
                                if (saveTask.isSuccessful()) {
                                    Log.d(TAG, "Dados do usuário salvos no Firestore com sucesso");
                                    // Salva o ID nas preferências locais
                                    salvarIdUsuario(context, usuario.getID());
                                    usuarioLogado = usuario;
                                    callback.onSucesso(usuario);
                                } else {
                                    Log.w(TAG, "Falha ao salvar dados do usuário no Firestore", saveTask.getException());
                                    
                                    // Mesmo que falhe ao salvar os dados adicionais, o usuário foi criado com sucesso
                                    // no Firebase Auth, então ainda retornamos sucesso
                                    salvarIdUsuario(context, usuario.getID());
                                    usuarioLogado = usuario;
                                    callback.onSucesso(usuario);
                                }
                            }
                        });
                    } else {
                        callback.onFalha("Erro ao criar usuário: resultado nulo após criação");
                    }
                } else {
                    Log.w(TAG, "Falha ao criar usuário com Firebase Auth", task.getException());
                    String erro = "Erro ao criar usuário";
                    if (task.getException() != null) {
                        String errorMsg = task.getException().getMessage();
                        Log.e(TAG, "Erro detalhado: " + errorMsg);
                        
                        if (errorMsg != null && errorMsg.contains("email address is already in use")) {
                            erro = "Este email já está cadastrado";
                        } else if (errorMsg != null && errorMsg.contains("password is invalid")) {
                            erro = "A senha deve ter pelo menos 6 caracteres";
                        } else if (errorMsg != null && errorMsg.contains("badly formatted")) {
                            erro = "Formato de email inválido";
                        } else {
                            erro += ": " + errorMsg;
                        }
                    }
                    callback.onFalha(erro);
                }
            });
    }
    
    /**
     * Interface para callback de login
     */
    public interface LoginCallback {
        void onSucesso(Usuario usuario);
        void onFalha(String mensagem);
    }
}
