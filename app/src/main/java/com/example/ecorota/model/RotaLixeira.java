package com.example.ecorota.model;

public class RotaLixeira {
    private Rota rota;
    private Lixeira lixeira;
    private int ordemVisita;
    private String statusColeta; // "PENDENTE", "COLETADA", "IGNORADA", "FALHA"
    
    public RotaLixeira() {
    }
    
    public RotaLixeira(Rota rota, Lixeira lixeira, int ordemVisita) {
        this.rota = rota;
        this.lixeira = lixeira;
        this.ordemVisita = ordemVisita;
        this.statusColeta = "PENDENTE";
    }
    
    public void atualizarStatus(String status) {
        this.statusColeta = status;
        
        // Quando a lixeira é coletada, redefine o nível de enchimento
        if (status.equals("COLETADA")) {
            lixeira.atualizarNivel(0.0f);
        }
    }
    
    // Getters e Setters
    public Rota getRota() {
        return rota;
    }
    
    public void setRota(Rota rota) {
        this.rota = rota;
    }
    
    public Lixeira getLixeira() {
        return lixeira;
    }
    
    public void setLixeira(Lixeira lixeira) {
        this.lixeira = lixeira;
    }
    
    public int getOrdemVisita() {
        return ordemVisita;
    }
    
    public void setOrdemVisita(int ordemVisita) {
        this.ordemVisita = ordemVisita;
    }
    
    public String getStatusColeta() {
        return statusColeta;
    }
    
    public void setStatusColeta(String statusColeta) {
        this.statusColeta = statusColeta;
    }
}