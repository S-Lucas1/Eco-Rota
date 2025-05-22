package com.example.ecorota.service;

import com.example.ecorota.model.Lixeira;
import com.example.ecorota.model.Rota;
import com.example.ecorota.model.RotaLixeira;
import com.example.ecorota.model.Usuario;
import com.example.ecorota.repository.RotaRepository;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class RotaService {
    private static RotaService instance;
    private final RotaRepository rotaRepository;
    
    private RotaService() {
        rotaRepository = RotaRepository.getInstance();
    }
    
    public static RotaService getInstance() {
        if (instance == null) {
            instance = new RotaService();
        }
        return instance;
    }
    
    public List<Rota> getTodasRotas() {
        return rotaRepository.getTodasRotas();
    }
    
    public Rota getRotaPorId(String id) {
        return rotaRepository.getRotaPorId(id);
    }
    
    public List<Rota> getRotasPorMotorista(Usuario motorista) {
        return rotaRepository.getRotasPorMotorista(motorista);
    }
    
    public List<Rota> getRotasPrioritarias() {
        return rotaRepository.getRotasPrioritarias();
    }
    
    public void criarNovaRota(Date dataInicio, Usuario motorista, List<Lixeira> lixeiras) {
        // Gera um ID único para a nova rota
        String novoId = "R" + String.format("%03d", rotaRepository.getTodasRotas().size() + 1);
        
        Rota novaRota = new Rota(novoId, dataInicio, motorista);
        
        // Adiciona as lixeiras à rota
        int ordem = 1;
        for (Lixeira lixeira : lixeiras) {
            novaRota.addLixeira(lixeira, ordem++);
        }
        
        // Calcula a melhor ordem de visita
        novaRota.calcularRota();
        
        // Salva a rota
        rotaRepository.salvarRota(novaRota);
        
        // Associa a rota ao motorista
        motorista.addRota(novaRota);
    }
    
    public void atualizarStatusColeta(String rotaId, String lixeiraId, String novoStatus) {
        Rota rota = rotaRepository.getRotaPorId(rotaId);
        if (rota != null) {
            for (RotaLixeira rotaLixeira : rota.getRotaLixeiras()) {
                if (rotaLixeira.getLixeira().getID().equals(lixeiraId)) {
                    rotaLixeira.atualizarStatus(novoStatus);
                    break;
                }
            }
            
            // Verifica se todas as lixeiras foram coletadas
            boolean todasColetadas = true;
            for (RotaLixeira rotaLixeira : rota.getRotaLixeiras()) {
                if (!rotaLixeira.getStatusColeta().equals("COLETADA") && 
                    !rotaLixeira.getStatusColeta().equals("IGNORADA")) {
                    todasColetadas = false;
                    break;
                }
            }
            
            // Se todas foram coletadas, finaliza a rota
            if (todasColetadas) {
                rota.setDataFinal(new Date());
            }
            
            rotaRepository.salvarRota(rota);
        }
    }
    
    public void recalcularRota(String rotaId) {
        Rota rota = rotaRepository.getRotaPorId(rotaId);
        if (rota != null) {
            rota.atualizarRota();
            rotaRepository.salvarRota(rota);
        }
    }
    
    public void finalizarRota(String rotaId) {
        Rota rota = rotaRepository.getRotaPorId(rotaId);
        if (rota != null) {
            rota.setDataFinal(new Date());
            rotaRepository.salvarRota(rota);
        }
    }
    
    public void removerRota(String id) {
        rotaRepository.removerRota(id);
    }
}