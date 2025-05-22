package com.example.ecorota.repository;

import android.content.Context;
import android.util.Log;

import com.example.ecorota.model.Lixeira;
import com.example.ecorota.model.Sensor;
import com.example.ecorota.util.LixeiraFileUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LixeiraRepository {
    private static final String TAG = "LixeiraRepository";
    private static LixeiraRepository instance;
    private Map<String, Lixeira> lixeiras;
    private Context context;
    private boolean dadosInicializados = false;
    
    private LixeiraRepository() {
        lixeiras = new HashMap<>();
    }
    
    public static LixeiraRepository getInstance() {
        if (instance == null) {
            instance = new LixeiraRepository();
        }
        return instance;
    }
    
    public void inicializar(Context context) {
        this.context = context.getApplicationContext();
        
        // Verificar se há dados persistidos
        Map<String, Lixeira> lixeirasCarregadas = LixeiraFileUtil.carregarLixeiras(this.context);
        
        if (lixeirasCarregadas != null && !lixeirasCarregadas.isEmpty()) {
            Log.d(TAG, "Usando lixeiras carregadas do arquivo: " + lixeirasCarregadas.size() + " lixeiras");
            this.lixeiras = lixeirasCarregadas;
        } else if (!dadosInicializados) {
            Log.d(TAG, "Inicializando dados padrão de lixeiras");
            inicializarDados();
            dadosInicializados = true;
            // Salva os dados iniciais no arquivo
            LixeiraFileUtil.salvarLixeiras(this.context, this.lixeiras);
        }
    }
    
    private void inicializarDados() {
        // Dados mockados de lixeiras em diferentes locais da cidade
        // As coordenadas são exemplos e deveriam representar locais reais
        
        // Centro da cidade
        Lixeira lixeira1 = new Lixeira("L001", -23.500f, -47.430f, 0.85f);
        Sensor sensor1 = new Sensor("S001", "ULTRASSONICO", "ATIVO", 0.85f);
        lixeira1.setSensor(sensor1);
        sensor1.setLixeira(lixeira1);
        
        // Área residencial
        Lixeira lixeira2 = new Lixeira("L002", -23.505f, -47.435f, 0.35f);
        Sensor sensor2 = new Sensor("S002", "ULTRASSONICO", "ATIVO", 0.35f);
        lixeira2.setSensor(sensor2);
        sensor2.setLixeira(lixeira2);
        
        // Área comercial
        Lixeira lixeira3 = new Lixeira("L003", -23.498f, -47.428f, 0.90f);
        Sensor sensor3 = new Sensor("S003", "PESO", "ATIVO", 0.90f);
        lixeira3.setSensor(sensor3);
        sensor3.setLixeira(lixeira3);
        
        // Parque municipal
        Lixeira lixeira4 = new Lixeira("L004", -23.510f, -47.425f, 0.15f);
        Sensor sensor4 = new Sensor("S004", "ULTRASSONICO", "ATIVO", 0.15f);
        lixeira4.setSensor(sensor4);
        sensor4.setLixeira(lixeira4);
        
        // Universidade
        Lixeira lixeira5 = new Lixeira("L005", -23.515f, -47.440f, 0.65f);
        Sensor sensor5 = new Sensor("S005", "INFRAVERMELHO", "ATIVO", 0.65f);
        lixeira5.setSensor(sensor5);
        sensor5.setLixeira(lixeira5);
        
        // Shopping center
        Lixeira lixeira6 = new Lixeira("L006", -23.492f, -47.427f, 0.75f);
        Sensor sensor6 = new Sensor("S006", "PESO", "ATIVO", 0.75f);
        lixeira6.setSensor(sensor6);
        sensor6.setLixeira(lixeira6);
        
        // Estádio
        Lixeira lixeira7 = new Lixeira("L007", -23.488f, -47.432f, 0.05f);
        Sensor sensor7 = new Sensor("S007", "ULTRASSONICO", "FALHA", 0.05f);
        lixeira7.setSensor(sensor7);
        sensor7.setLixeira(lixeira7);
        lixeira7.setStatus("MANUTENCAO");
        
        // Hospital
        Lixeira lixeira8 = new Lixeira("L008", -23.502f, -47.420f, 0.55f);
        Sensor sensor8 = new Sensor("S008", "INFRAVERMELHO", "ATIVO", 0.55f);
        lixeira8.setSensor(sensor8);
        sensor8.setLixeira(lixeira8);
        
        // Terminal de ônibus
        Lixeira lixeira9 = new Lixeira("L009", -23.508f, -47.418f, 0.80f);
        Sensor sensor9 = new Sensor("S009", "ULTRASSONICO", "ATIVO", 0.80f);
        lixeira9.setSensor(sensor9);
        sensor9.setLixeira(lixeira9);
        
        // Escola
        Lixeira lixeira10 = new Lixeira("L010", -23.495f, -47.442f, 0.45f);
        Sensor sensor10 = new Sensor("S010", "PESO", "ATIVO", 0.45f);
        lixeira10.setSensor(sensor10);
        sensor10.setLixeira(lixeira10);
        
        // Adiciona lixeiras ao repositório
        lixeiras.put(lixeira1.getID(), lixeira1);
        lixeiras.put(lixeira2.getID(), lixeira2);
        lixeiras.put(lixeira3.getID(), lixeira3);
        lixeiras.put(lixeira4.getID(), lixeira4);
        lixeiras.put(lixeira5.getID(), lixeira5);
        lixeiras.put(lixeira6.getID(), lixeira6);
        lixeiras.put(lixeira7.getID(), lixeira7);
        lixeiras.put(lixeira8.getID(), lixeira8);
        lixeiras.put(lixeira9.getID(), lixeira9);
        lixeiras.put(lixeira10.getID(), lixeira10);
    }
      public Lixeira getLixeiraPorId(String id) {
        return lixeiras.get(id);
    }
    
    public List<Lixeira> getTodas() {
        return new ArrayList<>(lixeiras.values());
    }
    
    public List<Lixeira> getLixeirasComNivelAlto() {
        List<Lixeira> result = new ArrayList<>();
        for (Lixeira lixeira : lixeiras.values()) {
            if (lixeira.getNivelEnchimento() > 0.75f && !lixeira.getStatus().equals("MANUTENCAO")) {
                result.add(lixeira);
            }
        }
        return result;
    }
    
    public List<Lixeira> getLixeirasEmManutencao() {
        List<Lixeira> result = new ArrayList<>();
        for (Lixeira lixeira : lixeiras.values()) {
            if (lixeira.getStatus().equals("MANUTENCAO") || 
                (lixeira.getSensor() != null && lixeira.getSensor().getEstado().equals("FALHA"))) {
                result.add(lixeira);
            }
        }
        return result;
    }
    
    public void salvarLixeira(Lixeira lixeira) {
        lixeiras.put(lixeira.getID(), lixeira);
        
        // Persiste as alterações no arquivo
        if (context != null) {
            LixeiraFileUtil.salvarLixeiras(context, lixeiras);
        } else {
            Log.e(TAG, "Erro ao salvar lixeira: contexto não inicializado");
        }
    }
    
    public void removerLixeira(String id) {
        lixeiras.remove(id);
        
        // Persiste as alterações no arquivo
        if (context != null) {
            LixeiraFileUtil.salvarLixeiras(context, lixeiras);
        } else {
            Log.e(TAG, "Erro ao remover lixeira: contexto não inicializado");
        }
    }
    
    public boolean lixeiraExiste(String id) {
        return lixeiras.containsKey(id);
    }
    
    // Adiciona uma nova lixeira ao repositório e salva no arquivo
    public boolean adicionarLixeira(Lixeira lixeira) {
        if (lixeira.getID() == null || lixeira.getID().isEmpty()) {
            // Gera um novo ID
            String novoId = gerarNovoId();
            lixeira.setID(novoId);
        }
        
        lixeiras.put(lixeira.getID(), lixeira);
        
        // Persiste as alterações no arquivo
        if (context != null) {
            return LixeiraFileUtil.salvarLixeiras(context, lixeiras);
        } else {
            Log.e(TAG, "Erro ao adicionar lixeira: contexto não inicializado");
            return false;
        }
    }
    
    // Gera um novo ID para lixeira
    private String gerarNovoId() {
        int maiorId = 0;
        for (String id : lixeiras.keySet()) {
            if (id.startsWith("L")) {
                try {
                    int idNumerico = Integer.parseInt(id.substring(1));
                    if (idNumerico > maiorId) {
                        maiorId = idNumerico;
                    }
                } catch (NumberFormatException e) {
                    // Ignora IDs que não seguem o padrão L000
                }
            }
        }
        
        // Gera um novo ID incrementando o maior ID encontrado
        int novoIdNumerico = maiorId + 1;
        
        // Formata para o padrão L001, L002, etc.
        return String.format("L%03d", novoIdNumerico);
    }
}