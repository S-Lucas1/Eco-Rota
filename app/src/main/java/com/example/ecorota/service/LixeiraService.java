package com.example.ecorota.service;

import com.example.ecorota.model.Lixeira;
import com.example.ecorota.model.Sensor;
import com.example.ecorota.repository.LixeiraRepository;

import java.util.Date;
import java.util.List;

public class LixeiraService {
    private static LixeiraService instance;
    private final LixeiraRepository lixeiraRepository;
    
    private LixeiraService() {
        lixeiraRepository = LixeiraRepository.getInstance();
    }
    
    public static LixeiraService getInstance() {
        if (instance == null) {
            instance = new LixeiraService();
        }
        return instance;
    }
    
    public List<Lixeira> getTodasLixeiras() {
        return lixeiraRepository.getTodas();
    }
    
    public Lixeira getLixeiraPorId(String id) {
        return lixeiraRepository.getLixeiraPorId(id);
    }
    
    public List<Lixeira> getLixeirasComNivelAlto() {
        return lixeiraRepository.getLixeirasComNivelAlto();
    }
    
    public List<Lixeira> getLixeirasEmManutencao() {
        return lixeiraRepository.getLixeirasEmManutencao();
    }
    
    public void atualizarNivelLixeira(String lixeiraId, float novoNivel) {
        Lixeira lixeira = lixeiraRepository.getLixeiraPorId(lixeiraId);
        if (lixeira != null) {
            lixeira.atualizarNivel(novoNivel);
            
            // Atualiza também o sensor
            if (lixeira.getSensor() != null) {
                lixeira.getSensor().setUltimaLeitura(novoNivel);
            }
            
            lixeiraRepository.salvarLixeira(lixeira);
        }
    }
    
    public void reportarFalhaSensor(String lixeiraId) {
        Lixeira lixeira = lixeiraRepository.getLixeiraPorId(lixeiraId);
        if (lixeira != null && lixeira.getSensor() != null) {
            Sensor sensor = lixeira.getSensor();
            sensor.reportarFalha();
            lixeira.setStatus("MANUTENCAO");
            lixeiraRepository.salvarLixeira(lixeira);
        }
    }
    
    public void adicionarLixeira(Lixeira lixeira) {
        lixeiraRepository.salvarLixeira(lixeira);
    }
    
    public void removerLixeira(String id) {
        lixeiraRepository.removerLixeira(id);
    }
}