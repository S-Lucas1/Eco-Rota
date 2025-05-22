package com.example.ecorota.repository;

import com.example.ecorota.model.Lixeira;
import com.example.ecorota.model.Rota;
import com.example.ecorota.model.RotaLixeira;
import com.example.ecorota.model.Usuario;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RotaRepository {
    private static RotaRepository instance;
    private Map<String, Rota> rotas;
    
    private RotaRepository() {
        rotas = new HashMap<>();
        inicializarDados();
    }
    
    public static RotaRepository getInstance() {
        if (instance == null) {
            instance = new RotaRepository();
        }
        return instance;
    }
    
    private void inicializarDados() {
        // Dados mockados de rotas
        UsuarioRepository usuarioRepository = UsuarioRepository.getInstance();
        LixeiraRepository lixeiraRepository = LixeiraRepository.getInstance();
        
        // Obter motoristas
        List<Usuario> motoristas = usuarioRepository.getMotoristas();
        if (motoristas.isEmpty()) {
            return;
        }
        
        // Obter lixeiras
        List<Lixeira> todasLixeiras = lixeiraRepository.getTodas();
        if (todasLixeiras.isEmpty()) {
            return;
        }
        
        // Rota 1: Região Central - Prioridade Alta (lixeiras mais cheias)
        Calendar calendar = Calendar.getInstance();
        Date dataInicio = calendar.getTime();
        
        Rota rota1 = new Rota("R001", dataInicio, motoristas.get(0));
        
        // Adicionar algumas lixeiras com nível alto à rota prioritária
        List<Lixeira> lixeirasAltas = lixeiraRepository.getLixeirasComNivelAlto();
        int ordem = 1;
        for (Lixeira lixeira : lixeirasAltas) {
            rota1.addLixeira(lixeira, ordem++);
        }
        rota1.calcularRota();
        
        // Rota 2: Região Residencial - Programada para amanhã
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        Date dataAmanha = calendar.getTime();
        
        Rota rota2 = new Rota("R002", dataAmanha, motoristas.get(1));
        
        // Adicionar lixeiras à rota residencial
        List<Lixeira> todasDisponiveis = new ArrayList<>();
        for (Lixeira lixeira : todasLixeiras) {
            if (!lixeira.getStatus().equals("MANUTENCAO")) {
                todasDisponiveis.add(lixeira);
            }
            
            // Limitar a 5 lixeiras para esta rota
            if (todasDisponiveis.size() >= 5) break;
        }
        
        ordem = 1;
        for (Lixeira lixeira : todasDisponiveis) {
            rota2.addLixeira(lixeira, ordem++);
        }
        rota2.calcularRota();
        
        // Rota 3: Manutenção - Programada para 2 dias depois
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        Date dataDoisDias = calendar.getTime();
        
        Rota rota3 = new Rota("R003", dataDoisDias, motoristas.get(0));
        
        // Adicionar lixeiras em manutenção
        List<Lixeira> lixeirasManutencao = lixeiraRepository.getLixeirasEmManutencao();
        ordem = 1;
        for (Lixeira lixeira : lixeirasManutencao) {
            rota3.addLixeira(lixeira, ordem++);
        }
        
        // Adicionar rotas ao repositório
        rotas.put(rota1.getID(), rota1);
        rotas.put(rota2.getID(), rota2);
        rotas.put(rota3.getID(), rota3);
        
        // Associar rotas aos motoristas
        motoristas.get(0).addRota(rota1);
        motoristas.get(0).addRota(rota3);
        motoristas.get(1).addRota(rota2);
    }
    
    public Rota getRotaPorId(String id) {
        return rotas.get(id);
    }
    
    public List<Rota> getTodasRotas() {
        return new ArrayList<>(rotas.values());
    }
    
    public List<Rota> getRotasPorMotorista(Usuario motorista) {
        List<Rota> rotasMotorista = new ArrayList<>();
        for (Rota rota : rotas.values()) {
            if (rota.getMotorista() != null && 
                rota.getMotorista().getID().equals(motorista.getID())) {
                rotasMotorista.add(rota);
            }
        }
        return rotasMotorista;
    }
    
    public List<Rota> getRotasPrioritarias() {
        List<Rota> prioritarias = new ArrayList<>();
        for (Rota rota : rotas.values()) {
            // Verifica se a rota tem pelo menos uma lixeira com nível alto
            for (RotaLixeira rotaLixeira : rota.getRotaLixeiras()) {
                if (rotaLixeira.getLixeira().getNivelEnchimento() > 0.75f) {
                    prioritarias.add(rota);
                    break;
                }
            }
        }
        return prioritarias;
    }
    
    public void salvarRota(Rota rota) {
        rotas.put(rota.getID(), rota);
    }
    
    public void removerRota(String id) {
        rotas.remove(id);
    }
}