package com.example.ecorota.util;

import com.example.ecorota.model.Lixeira;
import com.example.ecorota.model.Sensor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class GsonHelper {

    // Criar um Gson configurado para evitar referência circular
    public static Gson createGsonInstance() {
        return new GsonBuilder()
                .registerTypeAdapter(Lixeira.class, new LixeiraTypeAdapter())
                .registerTypeAdapter(Sensor.class, new SensorTypeAdapter())
                .setPrettyPrinting()
                .create();
    }

    // TypeAdapter personalizado para a classe Lixeira
    private static class LixeiraTypeAdapter extends TypeAdapter<Lixeira> {
        @Override
        public void write(JsonWriter out, Lixeira lixeira) throws IOException {
            out.beginObject();
            out.name("ID").value(lixeira.getID());
            out.name("latitude").value(lixeira.getLatitude());
            out.name("longitude").value(lixeira.getLongitude());
            out.name("nivelEnchimento").value(lixeira.getNivelEnchimento());
            out.name("status").value(lixeira.getStatus());
            
            // Apenas salvar o ID do sensor, não o objeto completo
            if (lixeira.getSensor() != null) {
                out.name("sensorID").value(lixeira.getSensor().getID());
            }
            
            out.endObject();
        }

        @Override
        public Lixeira read(JsonReader in) throws IOException {
            Lixeira lixeira = new Lixeira();
            
            in.beginObject();
            while (in.hasNext()) {
                String name = in.nextName();
                switch (name) {
                    case "ID":
                        lixeira.setID(in.nextString());
                        break;
                    case "latitude":
                        lixeira.setLatitude((float) in.nextDouble());
                        break;
                    case "longitude":
                        lixeira.setLongitude((float) in.nextDouble());
                        break;
                    case "nivelEnchimento":
                        lixeira.setNivelEnchimento((float) in.nextDouble());
                        break;
                    case "status":
                        lixeira.setStatus(in.nextString());
                        break;
                    case "sensorID":
                        // Armazenar o ID do sensor para posterior resolução
                        lixeira.setSensorID(in.nextString());
                        break;
                    default:
                        in.skipValue();
                }
            }
            in.endObject();
            
            return lixeira;
        }
    }

    // TypeAdapter personalizado para a classe Sensor
    private static class SensorTypeAdapter extends TypeAdapter<Sensor> {
        @Override
        public void write(JsonWriter out, Sensor sensor) throws IOException {
            out.beginObject();
            out.name("ID").value(sensor.getID());
            out.name("tipo").value(sensor.getTipo());
            out.name("estado").value(sensor.getEstado());
            out.name("ultimaLeitura").value(sensor.getUltimaLeitura());
            
            // Apenas salvar o ID da lixeira, não o objeto completo
            if (sensor.getLixeira() != null) {
                out.name("lixeiraID").value(sensor.getLixeira().getID());
            }
            
            out.endObject();
        }

        @Override
        public Sensor read(JsonReader in) throws IOException {
            Sensor sensor = new Sensor();
            
            in.beginObject();
            while (in.hasNext()) {
                String name = in.nextName();
                switch (name) {
                    case "ID":
                        sensor.setID(in.nextString());
                        break;
                    case "tipo":
                        sensor.setTipo(in.nextString());
                        break;
                    case "estado":
                        sensor.setEstado(in.nextString());
                        break;
                    case "ultimaLeitura":
                        sensor.setUltimaLeitura((float) in.nextDouble());
                        break;
                    case "lixeiraID":
                        // Armazenar o ID da lixeira para posterior resolução
                        sensor.setLixeiraID(in.nextString());
                        break;
                    default:
                        in.skipValue();
                }
            }
            in.endObject();
            
            return sensor;
        }
    }

    // Método para processar as relações entre lixeiras e sensores após carregamento
    public static void resolverReferencias(Map<String, Lixeira> lixeiras) {
        Map<String, Sensor> sensores = new HashMap<>();
        
        // Primeiro, identificar todos os sensores
        for (Lixeira lixeira : lixeiras.values()) {
            if (lixeira.getSensor() != null) {
                sensores.put(lixeira.getSensor().getID(), lixeira.getSensor());
            }
        }
        
        // Depois, resolver as referências
        for (Lixeira lixeira : lixeiras.values()) {
            String sensorID = lixeira.getSensorID();
            if (sensorID != null && sensores.containsKey(sensorID)) {
                Sensor sensor = sensores.get(sensorID);
                lixeira.setSensor(sensor);
                sensor.setLixeira(lixeira);
            }
        }
    }
}
