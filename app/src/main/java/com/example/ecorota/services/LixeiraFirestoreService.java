package com.example.ecorota.services;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.ecorota.model.Lixeira;
import com.example.ecorota.model.Sensor;
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
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Serviço para gerenciar operações do Firestore relacionadas a lixeiras
 */
public class LixeiraFirestoreService {
    private static final String TAG = "LixeiraFirestoreService";
    private static final String COLLECTION_LIXEIRAS = "lixeiras";
    
    private final FirebaseFirestore db;
    private final CollectionReference lixeirasCollection;
    
    // Singleton instance
    private static LixeiraFirestoreService instance;
    
    private LixeiraFirestoreService() {
        db = FirebaseFirestore.getInstance();
        lixeirasCollection = db.collection(COLLECTION_LIXEIRAS);
    }
    
    public static LixeiraFirestoreService getInstance() {
        if (instance == null) {
            instance = new LixeiraFirestoreService();
        }
        return instance;
    }
    
    /**
     * Adiciona ou atualiza uma lixeira no Firestore
     */
    public void salvarLixeira(Lixeira lixeira, final OnCompleteListener<Void> listener) {
        // Prepara os dados da lixeira para salvar no Firestore
        Map<String, Object> lixeiraMap = new HashMap<>();
        lixeiraMap.put("latitude", lixeira.getLatitude());
        lixeiraMap.put("longitude", lixeira.getLongitude());
        lixeiraMap.put("nivelEnchimento", lixeira.getNivelEnchimento());
        lixeiraMap.put("status", lixeira.getStatus()); 
        lixeiraMap.put("ultimaAtualizacao", lixeira.getUltimaAtualizacao());
        lixeiraMap.put("sensorID", lixeira.getSensorID());
        lixeiraMap.put("imagemPath", lixeira.getImagemPath());
        
        // Se já tiver ID, atualiza o documento existente. Senão, cria um novo.
        if (lixeira.getID() != null && !lixeira.getID().isEmpty()) {
            lixeirasCollection.document(lixeira.getID()).set(lixeiraMap)
                .addOnCompleteListener(listener);
        } else {
            lixeirasCollection.add(lixeiraMap)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        lixeira.setID(documentReference.getId());
                        Log.d(TAG, "Lixeira adicionada com ID: " + documentReference.getId());
                        if (listener != null) {
                            listener.onComplete(Tasks.forResult(null));
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Erro ao adicionar lixeira", e);
                        if (listener != null) {
                            listener.onComplete(Tasks.forException(e));
                        }
                    }
                });
        }
    }
    
    /**
     * Atualiza apenas o nível de enchimento da lixeira
     */
    public void atualizarNivelLixeira(String lixeiraId, float novoNivel, final OnCompleteListener<Void> listener) {
        // Calcula o novo status com base no nível
        String novoStatus;
        if (novoNivel < 0.25f) {
            novoStatus = "VAZIA";
        } else if (novoNivel < 0.75f) {
            novoStatus = "PARCIAL";
        } else {
            novoStatus = "CHEIA";
        }
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("nivelEnchimento", novoNivel);
        updates.put("status", novoStatus);
        updates.put("ultimaAtualizacao", new Date());
        
        lixeirasCollection.document(lixeiraId).update(updates)
            .addOnCompleteListener(listener);
    }
    
    /**
     * Obtém todas as lixeiras cadastradas
     */
    public void getLixeiras(final OnCompleteListener<List<Lixeira>> listener) {
        lixeirasCollection.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<Lixeira> lixeiras = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        // Map manually to handle string->double conversion
                        Lixeira lixeira = new Lixeira();
                        lixeira.setID(document.getId());
                        // latitude
                        Object latVal = document.get("latitude");
                        double lat = 0;
                        if (latVal instanceof String) {
                            try { lat = Double.parseDouble((String) latVal); } catch (Exception ignored) {}
                        } else if (latVal instanceof Number) {
                            lat = ((Number) latVal).doubleValue();
                        }
                        lixeira.setLatitude(lat);
                        // longitude
                        Object lngVal = document.get("longitude");
                        double lng = 0;
                        if (lngVal instanceof String) {
                            try { lng = Double.parseDouble((String) lngVal); } catch (Exception ignored) {}
                        } else if (lngVal instanceof Number) {
                            lng = ((Number) lngVal).doubleValue();
                        }
                        lixeira.setLongitude(lng);
                        // nivel de enchimento
                        Object nivelVal = document.get("nivelEnchimento");
                        float nivel = nivelVal instanceof Number ? ((Number) nivelVal).floatValue() : 0f;
                        lixeira.setNivelEnchimento(nivel);
                        // outros campos
                        lixeira.setStatus(document.getString("status"));
                        lixeira.setUltimaAtualizacao(document.getDate("ultimaAtualizacao"));
                        lixeira.setSensorID(document.getString("sensorID"));
                        lixeira.setImagemPath(document.getString("imagemPath"));
                        lixeiras.add(lixeira);
                        Log.d(TAG, document.getId() + " => " + document.getData());
                    }
                    listener.onComplete(Tasks.forResult(lixeiras));
                } else {
                    Log.w(TAG, "Erro ao obter lixeiras.", task.getException());
                    listener.onComplete(Tasks.forException(task.getException()));
                }
            }
        });
    }
    
    /**
     * Busca uma lixeira pelo ID
     */
    public void getLixeiraPorId(String lixeiraId, final OnCompleteListener<Lixeira> listener) {
        lixeirasCollection.document(lixeiraId).get()
            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Lixeira lixeira = document.toObject(Lixeira.class);
                            lixeira.setID(document.getId());
                            listener.onComplete(Tasks.forResult(lixeira));
                        } else {
                            Log.d(TAG, "Nenhuma lixeira encontrada com esse ID");
                            listener.onComplete(Tasks.forResult(null));
                        }
                    } else {
                        Log.w(TAG, "Erro ao buscar lixeira", task.getException());
                        listener.onComplete(Tasks.forException(task.getException()));
                    }
                }
            });
    }
    
    /**
     * Busca lixeiras próximas a uma coordenada
     * Nota: Esta implementação simples busca todas as lixeiras e filtra no cliente
     * Para maior eficiência, seria necessário implementar consultas geoespaciais
     */
    public void getLixeirasProximas(double lat, double lng, double raioKm, final OnCompleteListener<List<Lixeira>> listener) {
        getLixeiras(new OnCompleteListener<List<Lixeira>>() {
            @Override
            public void onComplete(@NonNull Task<List<Lixeira>> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    List<Lixeira> todasLixeiras = task.getResult();
                    List<Lixeira> lixeirasProximas = new ArrayList<>();
                    
                    // Filtrar lixeiras pelo raio (cálculo simples de distância)
                    for (Lixeira lixeira : todasLixeiras) {
                        // Calcula distância usando double para compatibilidade com Lixeira
                        double distancia = calcularDistancia(lat, lng, lixeira.getLatitude(), lixeira.getLongitude());
                        if (distancia <= raioKm) {
                            lixeirasProximas.add(lixeira);
                        }
                    }
                    
                    listener.onComplete(Tasks.forResult(lixeirasProximas));
                } else {
                    listener.onComplete(task);
                }
            }
        });
    }
    
    /**
     * Exclui uma lixeira do Firestore
     */
    public void excluirLixeira(String lixeiraId, final OnCompleteListener<Void> listener) {
        lixeirasCollection.document(lixeiraId).delete()
            .addOnCompleteListener(listener);
    }
    
    /**
     * Método auxiliar para calcular distância entre duas coordenadas
     * Fórmula de Haversine para distância em km
     */
    private double calcularDistancia(double lat1, double lng1, double lat2, double lng2) {
        final int R = 6371; // Raio da Terra em km
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lngDistance = Math.toRadians(lng2 - lng1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);
                
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }
    
    /**
     * Busca lixeiras que precisam de atenção (cheias ou em manutenção)
     */
    public void getLixeirasParaColeta(final OnCompleteListener<List<Lixeira>> listener) {
        lixeirasCollection
            .whereIn("status", Arrays.asList("CHEIA", "MANUTENCAO"))
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        List<Lixeira> lixeiras = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Lixeira lixeira = document.toObject(Lixeira.class);
                            lixeira.setID(document.getId());
                            lixeiras.add(lixeira);
                        }
                        listener.onComplete(Tasks.forResult(lixeiras));
                    } else {
                        Log.w(TAG, "Erro ao buscar lixeiras para coleta", task.getException());
                        listener.onComplete(Tasks.forException(task.getException()));
                    }
                }
            });
    }
}
