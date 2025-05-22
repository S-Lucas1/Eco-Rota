package com.example.ecorota.model;

public class Sensor {
    private String ID;
    private String tipo; // "ULTRASSONICO", "PESO", "INFRAVERMELHO"
    private String estado; // "ATIVO", "INATIVO", "FALHA"
    private float ultimaLeitura;
    private String lixeiraID; // Armazenar ID da lixeira para serialização
    private transient Lixeira lixeira; // Transient evita serialização circular
    
    public Sensor() {
    }
    
    public Sensor(String ID, String tipo, String estado, float ultimaLeitura) {
        this.ID = ID;
        this.tipo = tipo;
        this.estado = estado;
        this.ultimaLeitura = ultimaLeitura;
    }
    
    public float lerValor() {
        // Em um cenário real, esta função comunicaria com o sensor físico
        // Para dados mockados, apenas retorna a última leitura
        return ultimaLeitura;
    }
    
    public void reportarFalha() {
        this.estado = "FALHA";
    }
    
    // Getters e Setters
    public String getID() {
        return ID;
    }
    
    public void setID(String ID) {
        this.ID = ID;
    }
    
    public String getTipo() {
        return tipo;
    }
    
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
    
    public String getEstado() {
        return estado;
    }
    
    public void setEstado(String estado) {
        this.estado = estado;
    }
    
    public float getUltimaLeitura() {
        return ultimaLeitura;
    }
    
    public void setUltimaLeitura(float ultimaLeitura) {
        this.ultimaLeitura = ultimaLeitura;
    }
      public Lixeira getLixeira() {
        return lixeira;
    }
    
    public void setLixeira(Lixeira lixeira) {
        this.lixeira = lixeira;
        if (lixeira != null) {
            this.lixeiraID = lixeira.getID();
        }
    }
    
    public String getLixeiraID() {
        return lixeiraID;
    }
    
    public void setLixeiraID(String lixeiraID) {
        this.lixeiraID = lixeiraID;
    }
}