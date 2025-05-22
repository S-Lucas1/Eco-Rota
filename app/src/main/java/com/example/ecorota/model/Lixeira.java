package com.example.ecorota.model;

import java.util.Date;

public class Lixeira {
    private String ID;
    private double latitude;
    private double longitude;
    private float nivelEnchimento;
    private String status; // "VAZIA", "PARCIAL", "CHEIA", "MANUTENCAO"
    private Date ultimaAtualizacao;
    private String sensorID; // Armazenar ID do sensor para serialização
    private transient Sensor sensor; // Transient evita serialização circular
    private String imagemPath; // Caminho da imagem da lixeira
    
    public Lixeira() {
        this.ultimaAtualizacao = new Date();
        this.imagemPath = "lixeira_exemplo.jpg"; // Imagem padrão
    }
    
    public Lixeira(String ID, double latitude, double longitude, float nivelEnchimento) {
        this.ID = ID;
        this.latitude = latitude;
        this.longitude = longitude;
        this.nivelEnchimento = nivelEnchimento;
        this.ultimaAtualizacao = new Date();
        this.status = obterStatus();
        this.imagemPath = "lixeira_exemplo.jpg"; // Imagem padrão
    }
    
    public void atualizarNivel(float novoNivel) {
        this.nivelEnchimento = novoNivel;
        this.ultimaAtualizacao = new Date();
        this.status = obterStatus();
    }
    
    public String obterStatus() {
        if (nivelEnchimento < 0.25f) {
            return "VAZIA";
        } else if (nivelEnchimento < 0.75f) {
            return "PARCIAL";
        } else {
            return "CHEIA";
        }
    }
    
    // Getters e Setters
    public String getID() {
        return ID;
    }
    
    public void setID(String ID) {
        this.ID = ID;
    }
    
    public double getLatitude() {
        return latitude;
    }
    
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    
    public double getLongitude() {
        return longitude;
    }
    
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    
    public float getNivelEnchimento() {
        return nivelEnchimento;
    }
    
    public void setNivelEnchimento(float nivelEnchimento) {
        this.nivelEnchimento = nivelEnchimento;
        this.status = obterStatus();
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Date getUltimaAtualizacao() {
        return ultimaAtualizacao;
    }
    
    public void setUltimaAtualizacao(Date ultimaAtualizacao) {
        this.ultimaAtualizacao = ultimaAtualizacao;
    }
      public Sensor getSensor() {
        return sensor;
    }
    
    public void setSensor(Sensor sensor) {
        this.sensor = sensor;
        if (sensor != null) {
            this.sensorID = sensor.getID();
        }
    }
    
    public String getImagemPath() {
        return imagemPath;
    }
    
    public void setImagemPath(String imagemPath) {
        this.imagemPath = imagemPath;
    }
    
    public String getSensorID() {
        return sensorID;
    }
    
    public void setSensorID(String sensorID) {
        this.sensorID = sensorID;
    }
}