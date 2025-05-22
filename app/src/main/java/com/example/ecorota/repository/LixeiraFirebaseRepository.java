package com.example.ecorota.repository;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.ecorota.model.Lixeira;
import com.example.ecorota.services.LixeiraFirestoreService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repositório para gerenciar lixeiras através do Firebase Firestore
 */
public class LixeiraFirebaseRepository {
    private static final String TAG = "LixeiraFirebaseRepo";
    private static LixeiraFirebaseRepository instance;
    private final LixeiraFirestoreService lixeiraService;
    private final Map<String, Lixeira> lixeirasCache;
    private Context context;
    private boolean carregando = false;
    
    // Interface para callback de operações assíncronas
    public interface LixeirasCallback {
        void onLixeirasCarregadas(List<Lixeira> lixeiras);
        void onErro(String erro);
    }
    
    public interface LixeiraCallback {
        void onLixeiraCarregada(Lixeira lixeira);
        void onErro(String erro);
    }
    
    public interface OperacaoCallback {
        void onSucesso();
        void onErro(String erro);
    }
    
    private LixeiraFirebaseRepository() {
        lixeiraService = LixeiraFirestoreService.getInstance();
        lixeirasCache = new HashMap<>();
    }
    
    public static LixeiraFirebaseRepository getInstance() {
        if (instance == null) {
            instance = new LixeiraFirebaseRepository();
        }
        return instance;
    }
    
    public void setContext(Context context) {
        this.context = context;
    }
    
    /**
     * Carrega todas as lixeiras do Firestore
     */
    public void carregarLixeiras(final LixeirasCallback callback) {
        if (carregando) {
            // Evita múltiplas requisições simultâneas
            return;
        }
        
        carregando = true;
        lixeiraService.getLixeiras(new OnCompleteListener<List<Lixeira>>() {
            @Override
            public void onComplete(@NonNull Task<List<Lixeira>> task) {
                carregando = false;
                if (task.isSuccessful() && task.getResult() != null) {
                    List<Lixeira> lixeiras = task.getResult();
                    
                    // Atualiza o cache
                    lixeirasCache.clear();
                    for (Lixeira lixeira : lixeiras) {
                        lixeirasCache.put(lixeira.getID(), lixeira);
                    }
                    
                    callback.onLixeirasCarregadas(lixeiras);
                    Log.d(TAG, "Lixeiras carregadas com sucesso: " + lixeiras.size());
                } else {
                    String erro = "Falha ao carregar lixeiras";
                    if (task.getException() != null) {
                        erro += ": " + task.getException().getMessage();
                    }
                    callback.onErro(erro);
                    Log.e(TAG, erro, task.getException());
                    
                    // Exibe mensagem de erro no Toast se o contexto estiver disponível
                    if (context != null) {
                        Toast.makeText(context, erro, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
    
    /**
     * Obtém a lixeira do cache local ou do Firestore
     */
    public void getLixeira(String id, final LixeiraCallback callback) {
        if (lixeirasCache.containsKey(id)) {
            callback.onLixeiraCarregada(lixeirasCache.get(id));
            return;
        }
        
        lixeiraService.getLixeiraPorId(id, new OnCompleteListener<Lixeira>() {
            @Override
            public void onComplete(@NonNull Task<Lixeira> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    Lixeira lixeira = task.getResult();
                    lixeirasCache.put(lixeira.getID(), lixeira);
                    callback.onLixeiraCarregada(lixeira);
                } else {
                    String erro = "Falha ao carregar lixeira";
                    if (task.getException() != null) {
                        erro += ": " + task.getException().getMessage();
                    }
                    callback.onErro(erro);
                }
            }
        });
    }
    
    /**
     * Salva uma lixeira no Firestore
     */
    public void salvarLixeira(Lixeira lixeira, final OperacaoCallback callback) {
        lixeiraService.salvarLixeira(lixeira, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // Atualiza o cache
                    lixeirasCache.put(lixeira.getID(), lixeira);
                    callback.onSucesso();
                    Log.d(TAG, "Lixeira salva com sucesso: " + lixeira.getID());
                } else {
                    String erro = "Falha ao salvar lixeira";
                    if (task.getException() != null) {
                        erro += ": " + task.getException().getMessage();
                    }
                    callback.onErro(erro);
                    Log.e(TAG, erro, task.getException());
                }
            }
        });
    }
    
    /**
     * Atualiza apenas o nível de enchimento da lixeira
     */
    public void atualizarNivelLixeira(String id, float novoNivel, final OperacaoCallback callback) {
        lixeiraService.atualizarNivelLixeira(id, novoNivel, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // Atualiza o cache se a lixeira já estiver nele
                    if (lixeirasCache.containsKey(id)) {
                        Lixeira lixeira = lixeirasCache.get(id);
                        lixeira.atualizarNivel(novoNivel);
                    }
                    
                    callback.onSucesso();
                } else {
                    String erro = "Falha ao atualizar nível da lixeira";
                    if (task.getException() != null) {
                        erro += ": " + task.getException().getMessage();
                    }
                    callback.onErro(erro);
                }
            }
        });
    }
    
    /**
     * Exclui uma lixeira do Firestore
     */
    public void excluirLixeira(String id, final OperacaoCallback callback) {
        lixeiraService.excluirLixeira(id, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // Remove do cache
                    lixeirasCache.remove(id);
                    callback.onSucesso();
                    Log.d(TAG, "Lixeira excluída com sucesso: " + id);
                } else {
                    String erro = "Falha ao excluir lixeira";
                    if (task.getException() != null) {
                        erro += ": " + task.getException().getMessage();
                    }
                    callback.onErro(erro);
                    Log.e(TAG, erro, task.getException());
                }
            }
        });
    }
    
    /**
     * Busca lixeiras próximas a uma coordenada
     */
    public void getLixeirasProximas(float lat, float lng, float raioKm, final LixeirasCallback callback) {
        lixeiraService.getLixeirasProximas(lat, lng, raioKm, new OnCompleteListener<List<Lixeira>>() {
            @Override
            public void onComplete(@NonNull Task<List<Lixeira>> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    List<Lixeira> lixeiras = task.getResult();
                    callback.onLixeirasCarregadas(lixeiras);
                } else {
                    String erro = "Falha ao buscar lixeiras próximas";
                    if (task.getException() != null) {
                        erro += ": " + task.getException().getMessage();
                    }
                    callback.onErro(erro);
                }
            }
        });
    }
    
    /**
     * Busca lixeiras que precisam de atenção (cheias ou em manutenção)
     */
    public void getLixeirasParaColeta(final LixeirasCallback callback) {
        lixeiraService.getLixeirasParaColeta(new OnCompleteListener<List<Lixeira>>() {
            @Override
            public void onComplete(@NonNull Task<List<Lixeira>> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    List<Lixeira> lixeiras = task.getResult();
                    callback.onLixeirasCarregadas(lixeiras);
                } else {
                    String erro = "Falha ao buscar lixeiras para coleta";
                    if (task.getException() != null) {
                        erro += ": " + task.getException().getMessage();
                    }
                    callback.onErro(erro);
                }
            }
        });
    }
}