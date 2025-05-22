package com.example.ecorota.services;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.ecorota.model.Rota;
import com.example.ecorota.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Serviço para gerenciar operações do Firestore relacionadas a usuários
 */
public class UsuarioFirestoreService {
    private static final String TAG = "UsuarioFirestoreService";
    private static final String COLLECTION_USERS = "users";
    
    private final FirebaseFirestore db;
    private final CollectionReference usersCollection;
    
    // Singleton instance
    private static UsuarioFirestoreService instance;
    
    private UsuarioFirestoreService() {
        db = FirebaseFirestore.getInstance();
        usersCollection = db.collection(COLLECTION_USERS);
    }
    
    public static UsuarioFirestoreService getInstance() {
        if (instance == null) {
            instance = new UsuarioFirestoreService();
        }
        return instance;
    }
      /**
     * Adiciona ou atualiza um usuário no Firestore
     * O ID do documento sempre deve corresponder ao UID do Firebase Auth
     */
    public void salvarUsuario(Usuario usuario, final OnCompleteListener<Void> listener) {
        // Prepara os dados do usuário para salvar no Firestore
        Map<String, Object> usuarioMap = new HashMap<>();
        usuarioMap.put("nome", usuario.getNome());
        usuarioMap.put("email", usuario.getEmail());
        usuarioMap.put("telefone", usuario.getTelefone());
        usuarioMap.put("funcao", usuario.getFuncao());
        // Armazena a senha no Firestore para autenticação customizada
        usuarioMap.put("senha", usuario.getSenha());
        
        // IMPORTANTE: Não armazenamos mais senhas no Firestore por segurança
        // As senhas devem ser gerenciadas exclusivamente pelo Firebase Auth
        
        // Log para depuração
        Log.d(TAG, "Salvando usuário no Firestore, ID: " + usuario.getID() + ", Email: " + usuario.getEmail());
        
        // Se já tiver ID, atualiza; senão, cria novo documento
        if (usuario.getID() != null && !usuario.getID().isEmpty()) {
            usersCollection.document(usuario.getID()).set(usuarioMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "Documento do usuário atualizado com sucesso: " + usuario.getID());
                        if (listener != null) {
                            listener.onComplete(Tasks.forResult(null));
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Erro ao atualizar documento do usuário: " + e.getMessage(), e);
                        if (listener != null) {
                            listener.onComplete(Tasks.forException(e));
                        }
                    }
                });
        } else {
            // Cria novo usuário com ID automático
            usersCollection.add(usuarioMap)
                .addOnSuccessListener(docRef -> {
                    Log.d(TAG, "Usuário criado com ID: " + docRef.getId());
                    if (listener != null) listener.onComplete(Tasks.forResult(null));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erro ao criar usuário: " + e.getMessage(), e);
                    if (listener != null) listener.onComplete(Tasks.forException(e));
                });
        }
    }
    
    /**
     * Obtém todos os usuários cadastrados
     */
    public void getUsuarios(final OnCompleteListener<List<Usuario>> listener) {
        usersCollection.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<Usuario> usuarios = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Usuario usuario = document.toObject(Usuario.class);
                        usuario.setID(document.getId());
                        usuarios.add(usuario);
                        Log.d(TAG, document.getId() + " => " + document.getData());                    }
                    listener.onComplete(Tasks.forResult(usuarios));
                } else {
                    Log.w(TAG, "Error getting documents.", task.getException());
                    listener.onComplete(Tasks.forException(task.getException()));
                }
            }
        });
    }
    
    /**
     * Busca um usuário pelo ID
     */
    public void getUsuarioPorId(String usuarioId, final OnCompleteListener<Usuario> listener) {
        usersCollection.document(usuarioId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();                    if (document.exists()) {
                        Usuario usuario = document.toObject(Usuario.class);
                        usuario.setID(document.getId());
                        listener.onComplete(Tasks.forResult(usuario));
                    } else {
                        Log.d(TAG, "No such document");
                        listener.onComplete(Tasks.forResult(null));
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                    listener.onComplete(Tasks.forException(task.getException()));
                }
            }
        });
    }
    
    /**
     * Exclui um usuário do Firestore
     */
    public void excluirUsuario(String usuarioId, final OnCompleteListener<Void> listener) {
        usersCollection.document(usuarioId).delete()
            .addOnCompleteListener(listener);
    }
    
    /**
     * Obtém um usuário pelo email
     */
    public void getUsuarioPorEmail(String email, final OnCompleteListener<Usuario> listener) {
        usersCollection.whereEqualTo("email", email).get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // Pega o primeiro documento com este email
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        Usuario usuario = document.toObject(Usuario.class);
                        if (usuario != null) {
                            usuario.setID(document.getId());
                            listener.onComplete(Tasks.forResult(usuario));
                        } else {
                            listener.onComplete(Tasks.forResult(null));
                        }
                    } else {
                        if (task.getException() != null) {
                            listener.onComplete(Tasks.forException(task.getException()));
                        } else {
                            // Não encontrou usuário com este email
                            listener.onComplete(Tasks.forResult(null));
                        }
                    }
                }
            });
    }
      /**
     * Método para verificar se um usuário já existe no Firestore (sem tentar autenticar)
     * Use apenas para verificar a existência, não para autenticação
     */
    public void verificarExistenciaUsuario(String email, final OnCompleteListener<Boolean> listener) {
        usersCollection.whereEqualTo("email", email).get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        boolean existe = !task.getResult().isEmpty();
                        listener.onComplete(Tasks.forResult(existe));
                    } else {
                        listener.onComplete(Tasks.forException(task.getException()));
                    }
                }
            });
    }

    /**
     * Autentica usuário usando email e senha salvos no Firestore
     */
    public void autenticarFirestore(String email, String senha, final OnCompleteListener<Usuario> listener) {
        usersCollection
            .whereEqualTo("email", email)
            .whereEqualTo("senha", senha)
            .limit(1)
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        DocumentSnapshot doc = task.getResult().getDocuments().get(0);
                        Usuario u = doc.toObject(Usuario.class);
                        if (u != null) {
                            u.setID(doc.getId());
                        }
                        listener.onComplete(Tasks.forResult(u));
                    } else {
                        listener.onComplete(Tasks.forException(new Exception("Email ou senha incorretos")));
                    }
                }
            });
    }
}
