package com.example.ecorota.util;

import android.content.Context;
import android.util.Log;

import com.example.ecorota.model.Lixeira;
import com.example.ecorota.model.Sensor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class LixeiraFileUtil {
    private static final String TAG = "LixeiraFileUtil";
    private static final String LIXEIRAS_FILE_NAME = "lixeiras.json";    // Salvar lixeiras no arquivo JSON
    public static boolean salvarLixeiras(Context context, Map<String, Lixeira> lixeiras) {
        if (context == null) {
            Log.e(TAG, "Erro: contexto nulo ao salvar lixeiras");
            return false;
        }
        
        if (lixeiras == null) {
            Log.e(TAG, "Erro: mapa de lixeiras nulo ao salvar");
            return false;
        }
        
        try {
            Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .create();
            
            // Salvar somente os campos simples para evitar referência circular
            Map<String, Object> lixeirasSimples = new HashMap<>();
            for (Map.Entry<String, Lixeira> entry : lixeiras.entrySet()) {
                String key = entry.getKey();
                Lixeira lixeira = entry.getValue();
                
                if (key == null || lixeira == null) {
                    Log.w(TAG, "Ignorando entrada nula no mapa de lixeiras");
                    continue;
                }
                  Map<String, Object> lixeiraMap = new HashMap<>();
                lixeiraMap.put("ID", lixeira.getID());
                lixeiraMap.put("latitude", lixeira.getLatitude());
                lixeiraMap.put("longitude", lixeira.getLongitude());
                lixeiraMap.put("nivelEnchimento", lixeira.getNivelEnchimento());
                lixeiraMap.put("status", lixeira.getStatus());
                lixeiraMap.put("imagemPath", lixeira.getImagemPath());
                
                // Armazenar o ID do sensor se disponível
                if (lixeira.getSensor() != null) {
                    lixeiraMap.put("sensorID", lixeira.getSensor().getID());
                } else if (lixeira.getSensorID() != null && !lixeira.getSensorID().isEmpty()) {
                    lixeiraMap.put("sensorID", lixeira.getSensorID());
                }
                
                lixeirasSimples.put(key, lixeiraMap);
            }
            
            String jsonLixeiras = gson.toJson(lixeirasSimples);
            
            File file = new File(context.getFilesDir(), LIXEIRAS_FILE_NAME);
            Log.d(TAG, "Salvando lixeiras no arquivo: " + file.getAbsolutePath());
            Log.d(TAG, "Conteúdo JSON a ser salvo: " + jsonLixeiras);
            
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(jsonLixeiras.getBytes());
                fos.flush();
                Log.d(TAG, "Lixeiras salvas com sucesso! Arquivo existe: " + file.exists() + ", Tamanho: " + file.length() + " bytes");
                return true;
            }
        } catch (IOException e) {
            Log.e(TAG, "Erro de E/S ao salvar lixeiras: " + e.getMessage(), e);
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Erro desconhecido ao salvar lixeiras: " + e.getMessage(), e);
            return false;
        }
    }// Carregar lixeiras do arquivo JSON
    public static Map<String, Lixeira> carregarLixeiras(Context context) {
        Map<String, Lixeira> lixeiras = new HashMap<>();
        
        File file = new File(context.getFilesDir(), LIXEIRAS_FILE_NAME);
        Log.d(TAG, "Tentando carregar lixeiras do arquivo: " + file.getAbsolutePath());
        Log.d(TAG, "O arquivo existe? " + file.exists() + ", Tamanho: " + (file.exists() ? file.length() : 0) + " bytes");
        
        // Se o arquivo não existir, retorna um mapa vazio
        if (!file.exists()) {
            Log.d(TAG, "Arquivo de lixeiras não encontrado, retornando mapa vazio");
            return lixeiras;
        }
        
        try {
            // Ler o conteúdo do arquivo
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            
            reader.close();
            
            // Interpretar o conteúdo JSON
            String jsonContent = stringBuilder.toString();
            Log.d(TAG, "Conteúdo JSON carregado: " + jsonContent);
            
            try {
                // Usar o Gson para converter de JSON para objetos
                Gson gson = new Gson();
                Type mapType = new TypeToken<Map<String, Map<String, Object>>>(){}.getType();
                Map<String, Map<String, Object>> lixeirasMap = gson.fromJson(jsonContent, mapType);
                
                if (lixeirasMap != null) {
                    // Converter os Maps em objetos Lixeira
                    for (Map.Entry<String, Map<String, Object>> entry : lixeirasMap.entrySet()) {
                        String id = entry.getKey();
                        Map<String, Object> lixeiraData = entry.getValue();
                        
                        Lixeira lixeira = new Lixeira();
                        lixeira.setID(id);
                        
                        // Converter campos
                        if (lixeiraData.containsKey("latitude")) {
                            double value = ((Number) lixeiraData.get("latitude")).doubleValue();
                            lixeira.setLatitude((float) value);
                        }
                        
                        if (lixeiraData.containsKey("longitude")) {
                            double value = ((Number) lixeiraData.get("longitude")).doubleValue();
                            lixeira.setLongitude((float) value);
                        }
                        
                        if (lixeiraData.containsKey("nivelEnchimento")) {
                            double value = ((Number) lixeiraData.get("nivelEnchimento")).doubleValue();
                            lixeira.setNivelEnchimento((float) value);
                        }
                        
                        if (lixeiraData.containsKey("status")) {
                            lixeira.setStatus((String) lixeiraData.get("status"));
                        }
                          if (lixeiraData.containsKey("sensorID")) {
                            lixeira.setSensorID((String) lixeiraData.get("sensorID"));
                        }
                        
                        if (lixeiraData.containsKey("imagemPath")) {
                            lixeira.setImagemPath((String) lixeiraData.get("imagemPath"));
                        } else {
                            lixeira.setImagemPath("lixeira_exemplo.jpg"); // Valor padrão caso não exista
                        }
                        
                        lixeiras.put(id, lixeira);
                    }
                    
                    // Criar e associar sensores para cada lixeira
                    for (Map.Entry<String, Lixeira> entry : lixeiras.entrySet()) {
                        Lixeira lixeira = entry.getValue();
                        String sensorID = lixeira.getSensorID();
                        if (sensorID != null && !sensorID.isEmpty()) {
                            // Cria um sensor com dados padrão
                            Sensor sensor = new Sensor(sensorID, "ULTRASSONICO", "ATIVO", lixeira.getNivelEnchimento());
                            lixeira.setSensor(sensor);
                            sensor.setLixeira(lixeira);
                        }
                          Log.d(TAG, "Lixeira carregada - ID: " + lixeira.getID() + 
                              ", Lat: " + lixeira.getLatitude() + 
                              ", Long: " + lixeira.getLongitude() + 
                              ", Imagem: " + lixeira.getImagemPath() +
                              ", Sensor: " + (lixeira.getSensor() != null ? lixeira.getSensor().getID() : "null"));
                    }
                    
                    Log.d(TAG, "Lixeiras carregadas com sucesso: " + lixeiras.size() + " lixeiras");
                } else {
                    Log.e(TAG, "lixeirasMap é null, retornando mapa vazio");
                }
            } catch (Exception e) {
                Log.e(TAG, "Erro ao converter JSON para lixeiras: " + e.getMessage(), e);
            }
        } catch (IOException e) {
            Log.e(TAG, "Erro ao carregar lixeiras: " + e.getMessage());
        }
        
        return lixeiras;
    }    // Verificar se uma lixeira já existe pelo ID
    public static boolean lixeiraExiste(Context context, String id) {
        if (context == null || id == null || id.isEmpty()) {
            Log.e(TAG, "Contexto ou ID inválidos ao verificar existência de lixeira");
            return false;
        }
        
        Map<String, Lixeira> lixeirasMap = carregarLixeiras(context);
        return lixeirasMap != null && lixeirasMap.containsKey(id);
    }
      // Adicionar uma nova lixeira
    public static boolean adicionarLixeira(Context context, Lixeira lixeira) {
        if (context == null) {
            Log.e(TAG, "Erro: contexto nulo ao adicionar lixeira");
            return false;
        }
        
        if (lixeira == null) {
            Log.e(TAG, "Erro: lixeira nula ao adicionar");
            return false;
        }
        
        Log.d(TAG, "Tentando adicionar lixeira: ID: " + lixeira.getID() + ", Lat: " + lixeira.getLatitude() + ", Long: " + lixeira.getLongitude());
        
        try {
            Map<String, Lixeira> lixeirasMap = carregarLixeiras(context);
            
            // Se o ID não foi definido, gera um novo
            if (lixeira.getID() == null || lixeira.getID().isEmpty()) {
                lixeira.setID(gerarNovoId(lixeirasMap));
                Log.d(TAG, "Novo ID gerado para a lixeira: " + lixeira.getID());
            }
            
            // Adiciona a nova lixeira usando o ID como chave
            lixeirasMap.put(lixeira.getID(), lixeira);
            
            Log.d(TAG, "Lixeira adicionada ao mapa. Total de lixeiras: " + lixeirasMap.size());
            
            // Salva o mapa atualizado
            boolean resultado = salvarLixeiras(context, lixeirasMap);
            Log.d(TAG, "Resultado do salvamento: " + (resultado ? "Sucesso" : "Falha"));
            return resultado;
        } catch (Exception e) {
            Log.e(TAG, "Erro ao adicionar lixeira: " + e.getMessage(), e);
            return false;
        }
    }
      // Gerar um novo ID de lixeira
    public static String gerarNovoId(Map<String, Lixeira> lixeirasMap) {
        // Garantir que o mapa não é nulo
        if (lixeirasMap == null) {
            return "L001"; // Valor padrão se não houver lixeiras
        }
        
        // Encontra o maior ID numérico atual
        int maiorId = 0;
        for (String id : lixeirasMap.keySet()) {
            if (id != null && id.startsWith("L")) {
                try {
                    int idNumerico = Integer.parseInt(id.substring(1));
                    if (idNumerico > maiorId) {
                        maiorId = idNumerico;
                    }
                } catch (NumberFormatException e) {
                    // Ignora IDs que não seguem o padrão L000
                    Log.e(TAG, "ID com formato inválido: " + id);
                }
            }
        }
        
        // Gera um novo ID incrementando o maior ID encontrado
        int novoIdNumerico = maiorId + 1;
        
        // Formata para o padrão L001, L002, etc.
        return String.format("L%03d", novoIdNumerico);
    }
      // Método auxiliar para verificar o diretório de arquivos e corrigir problemas
    public static void verificarDiretorioArquivos(Context context) {
        if (context == null) {
            Log.e(TAG, "Contexto nulo ao verificar diretório");
            return;
        }
        
        try {
            File filesDir = context.getFilesDir();
            Log.d(TAG, "Diretório de arquivos: " + filesDir.getAbsolutePath());
            Log.d(TAG, "Diretório existe: " + filesDir.exists());
            Log.d(TAG, "É diretório: " + filesDir.isDirectory());
            Log.d(TAG, "Pode ler: " + filesDir.canRead());
            Log.d(TAG, "Pode escrever: " + filesDir.canWrite());
            
            // Verificar se o diretório existe, se não, tentar criá-lo
            if (!filesDir.exists()) {
                boolean criado = filesDir.mkdirs();
                Log.d(TAG, "Tentativa de criar diretório: " + (criado ? "sucesso" : "falha"));
            }
            
            // Listar arquivos no diretório
            File[] arquivos = filesDir.listFiles();
            if (arquivos != null) {
                Log.d(TAG, "Número de arquivos no diretório: " + arquivos.length);
                for (File arquivo : arquivos) {
                    Log.d(TAG, "Arquivo: " + arquivo.getName() + ", Tamanho: " + arquivo.length() + " bytes");
                }
            } else {
                Log.d(TAG, "Diretório vazio ou não pode ser listado");
            }
            
            // Verificar arquivo de lixeiras especificamente
            File lixeirasFile = new File(filesDir, LIXEIRAS_FILE_NAME);
            Log.d(TAG, "Arquivo de lixeiras: " + LIXEIRAS_FILE_NAME);
            Log.d(TAG, "  - Existe: " + lixeirasFile.exists());
            Log.d(TAG, "  - Tamanho: " + (lixeirasFile.exists() ? lixeirasFile.length() : 0) + " bytes");
            Log.d(TAG, "  - Pode ler: " + (lixeirasFile.exists() ? lixeirasFile.canRead() : false));
            Log.d(TAG, "  - Pode escrever: " + (lixeirasFile.exists() ? lixeirasFile.canWrite() : false));
            
            // Verificar a integridade do arquivo JSON
            boolean arquivoPareceDanificado = false;
            
            if (lixeirasFile.exists() && lixeirasFile.length() > 0) {
                try (BufferedReader reader = new BufferedReader(new FileReader(lixeirasFile))) {
                    StringBuilder content = new StringBuilder();
                    String line;
                    int lineCount = 0;
                    while ((line = reader.readLine()) != null && lineCount < 10) {
                        content.append(line).append("\n");
                        lineCount++;
                    }
                    String conteudoInicial = content.toString();
                    Log.d(TAG, "Primeiras linhas do arquivo: " + conteudoInicial + 
                          (lineCount >= 10 ? "... (truncado)" : ""));
                    
                    // Verificação básica de JSON válido
                    if (!(conteudoInicial.trim().startsWith("{") && conteudoInicial.contains("\"")) &&
                        !(conteudoInicial.trim().startsWith("[") && conteudoInicial.contains("\""))) {
                        Log.w(TAG, "Arquivo de lixeiras parece não conter JSON válido");
                        arquivoPareceDanificado = true;
                    }
                    
                    // Tentar validar o JSON completo
                    try {
                        reader.close();  // Fechar e reabrir para ler do início
                        BufferedReader fullReader = new BufferedReader(new FileReader(lixeirasFile));
                        StringBuilder fullContent = new StringBuilder();
                        while ((line = fullReader.readLine()) != null) {
                            fullContent.append(line);
                        }
                        fullReader.close();
                        
                        // Tentar fazer parse do JSON
                        new Gson().fromJson(fullContent.toString(), Object.class);
                    } catch (Exception e) {
                        Log.w(TAG, "Erro ao fazer parse do JSON no arquivo: " + e.getMessage());
                        arquivoPareceDanificado = true;
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Erro ao ler arquivo de lixeiras: " + e.getMessage(), e);
                    arquivoPareceDanificado = true;
                }
                
                // Se o arquivo parecer danificado, fazer backup e deletar
                if (arquivoPareceDanificado) {
                    Log.w(TAG, "Detectado possível arquivo de lixeiras corrompido. Fazendo backup e resetando.");
                    try {
                        File backupFile = new File(filesDir, LIXEIRAS_FILE_NAME + ".bak");
                        if (backupFile.exists()) {
                            backupFile.delete();
                        }
                        boolean copiado = lixeirasFile.renameTo(backupFile);
                        Log.d(TAG, "Backup do arquivo corrompido: " + (copiado ? "sucesso" : "falha"));
                        if (!copiado) {
                            lixeirasFile.delete();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Erro ao fazer backup do arquivo corrompido: " + e.getMessage(), e);
                        lixeirasFile.delete();
                    }
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Exceção ao verificar diretório de arquivos: " + e.getMessage(), e);
        }
    }
    // Método para limpar dados corrompidos e forçar reinicialização
    public static boolean limparDadosLocais(Context context) {
        if (context == null) {
            Log.e(TAG, "Contexto nulo ao tentar limpar dados locais");
            return false;
        }
        
        Log.d(TAG, "Iniciando limpeza de dados locais...");
        
        try {
            File lixeirasFile = new File(context.getFilesDir(), LIXEIRAS_FILE_NAME);
            
            // Verificar se o arquivo existe
            if (lixeirasFile.exists()) {
                // Criar backup antes de excluir
                File backupFile = new File(context.getFilesDir(), LIXEIRAS_FILE_NAME + ".emergencybackup");
                if (backupFile.exists()) {
                    backupFile.delete(); // Remover backup antigo se existir
                }
                
                try {
                    // Copiar arquivo para backup (opcional)
                    boolean copiado = lixeirasFile.renameTo(backupFile);
                    Log.d(TAG, "Backup de emergência criado: " + (copiado ? "sucesso" : "falha"));
                    
                    if (!copiado) {
                        // Se não conseguiu renomear, tentar deletar diretamente
                        boolean deletado = lixeirasFile.delete();
                        Log.d(TAG, "Arquivo deletado diretamente: " + (deletado ? "sucesso" : "falha"));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Erro ao renomear arquivo para backup: " + e.getMessage(), e);
                    // Tentar excluir diretamente
                    lixeirasFile.delete();
                }
            } else {
                Log.d(TAG, "Arquivo de lixeiras não existe, nada para limpar");
            }
            
            Log.d(TAG, "Limpeza de dados concluída com sucesso");
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Erro ao limpar dados locais: " + e.getMessage(), e);
            return false;
        }
    }
}
