package com.example.ecorota.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Rota {
    private String ID;
    private Date dataInicio;
    private Date dataFinal;
    private Usuario motorista;
    private List<RotaLixeira> rotaLixeiras;
    
    public Rota() {
        this.rotaLixeiras = new ArrayList<>();
    }
    
    public Rota(String ID, Date dataInicio, Usuario motorista) {
        this.ID = ID;
        this.dataInicio = dataInicio;
        this.motorista = motorista;
        this.rotaLixeiras = new ArrayList<>();
    }
    
    public void calcularRota() {
        // Em uma implementação real, usaria um algoritmo para otimizar a rota
        // Para dados mockados, apenas ordena por nível de enchimento (maior para menor)
        rotaLixeiras.sort((r1, r2) -> 
            Float.compare(r2.getLixeira().getNivelEnchimento(), 
                          r1.getLixeira().getNivelEnchimento()));
        
        // Atualiza a ordem de visita após a ordenação
        for (int i = 0; i < rotaLixeiras.size(); i++) {
            rotaLixeiras.get(i).setOrdemVisita(i + 1);
        }
    }
    
    public void atualizarRota() {
        // Recalcula a rota com base nos níveis atuais das lixeiras
        calcularRota();
    }
    
    public void addLixeira(Lixeira lixeira, int ordem) {
        RotaLixeira rotaLixeira = new RotaLixeira();
        rotaLixeira.setRota(this);
        rotaLixeira.setLixeira(lixeira);
        rotaLixeira.setOrdemVisita(ordem);
        rotaLixeira.setStatusColeta("PENDENTE");
        rotaLixeiras.add(rotaLixeira);
    }
    
    public List<Lixeira> getListaLixeiras() {
        List<Lixeira> lixeiras = new ArrayList<>();
        for (RotaLixeira rotaLixeira : rotaLixeiras) {
            lixeiras.add(rotaLixeira.getLixeira());
        }
        return lixeiras;
    }
    
    // Getters e Setters
    public String getID() {
        return ID;
    }
    
    public void setID(String ID) {
        this.ID = ID;
    }
    
    public Date getDataInicio() {
        return dataInicio;
    }
    
    public void setDataInicio(Date dataInicio) {
        this.dataInicio = dataInicio;
    }
    
    public Date getDataFinal() {
        return dataFinal;
    }
    
    public void setDataFinal(Date dataFinal) {
        this.dataFinal = dataFinal;
    }
    
    public Usuario getMotorista() {
        return motorista;
    }
    
    public void setMotorista(Usuario motorista) {
        this.motorista = motorista;
    }
    
    public List<RotaLixeira> getRotaLixeiras() {
        return rotaLixeiras;
    }
    
    public void setRotaLixeiras(List<RotaLixeira> rotaLixeiras) {
        this.rotaLixeiras = rotaLixeiras;
    }
}