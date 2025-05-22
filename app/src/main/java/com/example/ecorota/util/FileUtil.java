package com.example.ecorota.util;

import android.content.Context;
import android.util.Log;

import com.example.ecorota.model.Usuario;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileUtil {
    private static final String TAG = "FileUtil";
    private static final String USUARIOS_FILE_NAME = "usuarios.json";
    
    // Salvar usuários no arquivo JSON
    public static boolean salvarUsuarios(Context context, Map<String, Usuario> usuarios) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonUsuarios = gson.toJson(usuarios);
        
        try {
            File file = new File(context.getFilesDir(), USUARIOS_FILE_NAME);
            Log.d(TAG, "Salvando usuários no arquivo: " + file.getAbsolutePath());
            Log.d(TAG, "Conteúdo JSON a ser salvo: " + jsonUsuarios);
            
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(jsonUsuarios.getBytes());
            fos.close();
            Log.d(TAG, "Usuários salvos com sucesso! Arquivo existe: " + file.exists() + ", Tamanho: " + file.length() + " bytes");
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Erro ao salvar usuários: " + e.getMessage());
            return false;
        }
    }
    
    // Carregar usuários do arquivo JSON
    public static Map<String, Usuario> carregarUsuarios(Context context) {
        File file = new File(context.getFilesDir(), USUARIOS_FILE_NAME);
        Log.d(TAG, "Tentando carregar usuários do arquivo: " + file.getAbsolutePath());
        Log.d(TAG, "O arquivo existe? " + file.exists() + ", Tamanho: " + (file.exists() ? file.length() : 0) + " bytes");
        
        // Se o arquivo não existir, retorna um mapa vazio
        if (!file.exists()) {
            Log.d(TAG, "Arquivo de usuários não encontrado, retornando mapa vazio");
            return new HashMap<>();
        }
        
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            
            reader.close();
            
            String jsonContent = stringBuilder.toString();
            Log.d(TAG, "Conteúdo JSON carregado: " + jsonContent);
            
            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, Usuario>>(){}.getType();
            Map<String, Usuario> usuarios = gson.fromJson(jsonContent, type);
            
            // Se o JSON estiver vazio ou for inválido, retorna um mapa vazio
            if (usuarios == null) {
                Log.e(TAG, "JSON inválido ou vazio, retornando mapa vazio");
                return new HashMap<>();
            }
            
            Log.d(TAG, "Usuários carregados com sucesso: " + usuarios.size() + " usuários");
            for (Map.Entry<String, Usuario> entry : usuarios.entrySet()) {
                Log.d(TAG, "Usuário carregado - ID: " + entry.getKey() + 
                      ", Nome: " + entry.getValue().getNome() + 
                      ", Email: " + entry.getValue().getEmail());
            }
            return usuarios;
        } catch (IOException e) {
            Log.e(TAG, "Erro ao carregar usuários: " + e.getMessage());
            return new HashMap<>();
        }
    }
      // Verificar se um usuário existe pelo email
    public static boolean usuarioExiste(Context context, String email) {
        Map<String, Usuario> usuarios = carregarUsuarios(context);
        
        // Percorre todos os usuários para verificar se o email já existe
        for (Usuario usuario : usuarios.values()) {
            if (usuario.getEmail() != null && usuario.getEmail().equals(email)) {
                return true;
            }
        }
        
        return false;
    }
      // Adicionar um novo usuário
    public static boolean adicionarUsuario(Context context, Usuario usuario) {
        Log.d(TAG, "Tentando adicionar usuário: " + usuario.getNome() + ", Email: " + usuario.getEmail());
        Map<String, Usuario> usuarios = carregarUsuarios(context);
        
        // Verifica se o usuário já existe pelo email
        for (Usuario u : usuarios.values()) {
            if (u.getEmail().equals(usuario.getEmail())) {
                Log.d(TAG, "Usuário já existe com este email");
                return false;
            }
        }
        
        // Se o ID não foi definido, gera um novo
        if (usuario.getID() == null || usuario.getID().isEmpty()) {
            usuario.setID(gerarNovoId(usuarios));
            Log.d(TAG, "Novo ID gerado para o usuário: " + usuario.getID());
        }
        
        // Adiciona o novo usuário usando o ID como chave
        usuarios.put(usuario.getID(), usuario);
        
        Log.d(TAG, "Usuário adicionado ao mapa. Total de usuários: " + usuarios.size());
        
        // Salva o mapa atualizado
        boolean resultado = salvarUsuarios(context, usuarios);
        Log.d(TAG, "Resultado do salvamento: " + (resultado ? "Sucesso" : "Falha"));
        return resultado;
    }
    
    // Gerar um novo ID de usuário
    public static String gerarNovoId(Map<String, Usuario> usuarios) {
        // Encontra o maior ID numérico atual
        int maiorId = 0;
        for (String id : usuarios.keySet()) {
            if (id.startsWith("U")) {
                try {
                    int idNumerico = Integer.parseInt(id.substring(1));
                    if (idNumerico > maiorId) {
                        maiorId = idNumerico;
                    }
                } catch (NumberFormatException e) {
                    // Ignora IDs que não seguem o padrão U000
                    Log.e(TAG, "ID com formato inválido: " + id);
                }
            }
        }
        
        // Gera um novo ID incrementando o maior ID encontrado
        int novoIdNumerico = maiorId + 1;
        
        // Formata para o padrão U001, U002, etc.
        return String.format("U%03d", novoIdNumerico);
    }
    
    // Método auxiliar para verificar o diretório de arquivos
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
            
            // Verificar arquivo de usuários especificamente
            File usuariosFile = new File(filesDir, USUARIOS_FILE_NAME);
            Log.d(TAG, "Arquivo de usuários: " + USUARIOS_FILE_NAME);
            Log.d(TAG, "  - Existe: " + usuariosFile.exists());
            Log.d(TAG, "  - Tamanho: " + (usuariosFile.exists() ? usuariosFile.length() : 0) + " bytes");
            Log.d(TAG, "  - Pode ler: " + (usuariosFile.exists() ? usuariosFile.canRead() : false));
            Log.d(TAG, "  - Pode escrever: " + (usuariosFile.exists() ? usuariosFile.canWrite() : false));
            
            if (usuariosFile.exists() && usuariosFile.length() > 0) {
                try (BufferedReader reader = new BufferedReader(new FileReader(usuariosFile))) {
                    StringBuilder content = new StringBuilder();
                    String line;
                    int lineCount = 0;
                    while ((line = reader.readLine()) != null && lineCount < 5) {
                        content.append(line).append("\n");
                        lineCount++;
                    }
                    Log.d(TAG, "Primeiras linhas do arquivo: " + content.toString() + 
                          (lineCount >= 5 ? "... (truncado)" : ""));
                } catch (IOException e) {
                    Log.e(TAG, "Erro ao ler primeiras linhas: " + e.getMessage());
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Exceção ao verificar diretório de arquivos: " + e.getMessage());
        }
    }
}
