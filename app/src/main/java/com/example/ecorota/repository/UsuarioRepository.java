package com.example.ecorota.repository;

import android.content.Context;
import android.util.Log;

import com.example.ecorota.model.Usuario;
import com.example.ecorota.util.FileUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsuarioRepository {
    private static final String TAG = "UsuarioRepository";
    private static UsuarioRepository instance;
    private Map<String, Usuario> usuarios;
    private Context context;
    
    private UsuarioRepository() {
        usuarios = new HashMap<>();
    }
    
    public static UsuarioRepository getInstance() {
        if (instance == null) {
            instance = new UsuarioRepository();
            Log.d(TAG, "Nova instância do UsuarioRepository criada");
        }
        return instance;
    }
    
    public Context getContext() {
        return context;
    }
    
    public void recarregarUsuarios() {
        if (context == null) {
            Log.e(TAG, "Não é possível recarregar usuários: contexto nulo");
            return;
        }
        
        Log.d(TAG, "Recarregando usuários do arquivo...");
        Map<String, Usuario> usuariosCarregados = FileUtil.carregarUsuarios(context);
        
        if (usuariosCarregados != null && !usuariosCarregados.isEmpty()) {
            this.usuarios = usuariosCarregados;
            Log.d(TAG, "Usuários recarregados com sucesso: " + usuarios.size());
        } else {
            Log.w(TAG, "Não foi possível recarregar usuários do arquivo, mantendo estado atual");
        }
    }
    
    public void inicializar(Context context) {
        this.context = context;
        
        // Verificar o diretório de arquivos
        if (context != null) {
            Log.d(TAG, "Inicializando com contexto. Diretório de arquivos: " + context.getFilesDir());
        } else {
            Log.e(TAG, "Contexto é nulo ao inicializar UsuarioRepository");
            return;
        }
        
        // Carregar usuários do arquivo, se existir
        Map<String, Usuario> usuariosCarregados = FileUtil.carregarUsuarios(context);
        
        if (usuariosCarregados != null && !usuariosCarregados.isEmpty()) {
            this.usuarios = usuariosCarregados;
            Log.d(TAG, "Usuários carregados do arquivo: " + usuarios.size());
            
            // Listar os usuários carregados para debug
            for (Map.Entry<String, Usuario> entry : usuarios.entrySet()) {
                Log.d(TAG, "Usuário - ID: " + entry.getKey() + 
                      ", Nome: " + entry.getValue().getNome() + 
                      ", Email: " + entry.getValue().getEmail());
            }
        } else {
            // Se não existir arquivo, inicializa com dados mocados
            Log.d(TAG, "Nenhum usuário encontrado no arquivo, inicializando dados mocados");
            inicializarDadosMocados();
            
            // E salva no arquivo
            boolean salvou = FileUtil.salvarUsuarios(context, usuarios);
            Log.d(TAG, "Salvamento dos usuários mocados: " + (salvou ? "Sucesso" : "Falha"));
        }
    }
    
    private void inicializarDadosMocados() {
        Log.d(TAG, "Inicializando dados mocados");
        
        // Adicionar usuários mocados
        Usuario admin = new Usuario("U001", "Administrador", "admin@ecorota.com", "(15) 99999-0001", "ADMIN", "admin123");
        Usuario motorista1 = new Usuario("U002", "João Silva", "joao.silva@ecorota.com", "(15) 99999-0002", "MOTORISTA", "motorista123");
        Usuario motorista2 = new Usuario("U003", "Maria Oliveira", "maria.oliveira@ecorota.com", "(15) 99999-0003", "MOTORISTA", "motorista456");
        Usuario supervisor = new Usuario("U004", "Carlos Santos", "carlos.santos@ecorota.com", "(15) 99999-0004", "SUPERVISOR", "supervisor123");
        Usuario tecnico = new Usuario("U005", "Ana Costa", "ana.costa@ecorota.com", "(15) 99999-0005", "TECNICO", "tecnico123");
        
        usuarios.put(admin.getID(), admin);
        usuarios.put(motorista1.getID(), motorista1);
        usuarios.put(motorista2.getID(), motorista2);
        usuarios.put(supervisor.getID(), supervisor);
        usuarios.put(tecnico.getID(), tecnico);
    }
    
    public Usuario getUsuarioPorId(String id) {
        return usuarios.get(id);
    }
    
    public Usuario autenticarUsuario(String email, String senha) {
        Log.d(TAG, "Tentando autenticar usuário com email: " + email);
        
        if (usuarios == null || usuarios.isEmpty()) {
            Log.e(TAG, "Mapa de usuários está vazio ou nulo durante autenticação");
            
            // Tentar recarregar os usuários do arquivo caso o mapa esteja vazio
            if (context != null) {
                Log.d(TAG, "Tentando recarregar usuários do arquivo durante autenticação");
                Map<String, Usuario> usuariosCarregados = FileUtil.carregarUsuarios(context);
                if (usuariosCarregados != null && !usuariosCarregados.isEmpty()) {
                    this.usuarios = usuariosCarregados;
                    Log.d(TAG, "Usuários recarregados do arquivo: " + usuarios.size());
                } else {
                    Log.e(TAG, "Nenhum usuário encontrado ao recarregar do arquivo");
                }
            } else {
                Log.e(TAG, "Contexto é nulo, não foi possível recarregar usuários");
            }
        }
        
        // Log do número de usuários disponíveis
        Log.d(TAG, "Número de usuários disponíveis para autenticação: " + 
              (usuarios != null ? usuarios.size() : 0));
        
        // Listar usuários disponíveis para depuração
        if (usuarios != null) {
            for (Usuario usuario : usuarios.values()) {
                Log.d(TAG, "Usuário disponível - ID: " + usuario.getID() + 
                      ", Email: " + usuario.getEmail());
            }
        }
        
        // Tentativa de autenticação
        if (usuarios != null) {
            for (Usuario usuario : usuarios.values()) {
                Log.d(TAG, "Comparando com email: " + usuario.getEmail());
                if (usuario.autenticar(email, senha)) {
                    Log.d(TAG, "Autenticação bem-sucedida para: " + usuario.getNome());
                    return usuario;
                }
            }
        }
        
        Log.d(TAG, "Autenticação falhou para email: " + email);
        return null;
    }
    
    public List<Usuario> getMotoristas() {
        List<Usuario> motoristas = new ArrayList<>();
        for (Usuario usuario : usuarios.values()) {
            if (usuario.getFuncao().equals("MOTORISTA")) {
                motoristas.add(usuario);
            }
        }
        return motoristas;
    }
    
    public List<Usuario> getTodosUsuarios() {
        return new ArrayList<>(usuarios.values());
    }
      public void salvarUsuario(Usuario usuario) {
        if (usuario.getID() == null || usuario.getID().isEmpty()) {
            // Gera um novo ID se não tiver
            String novoId = FileUtil.gerarNovoId(usuarios);
            usuario.setID(novoId);
        }
        
        usuarios.put(usuario.getID(), usuario);
        
        // Persiste no arquivo JSON
        if (context != null) {
            FileUtil.salvarUsuarios(context, usuarios);
            Log.d(TAG, "Usuário salvo: " + usuario.getNome());
        } else {
            Log.e(TAG, "Contexto não inicializado. Impossível salvar usuário.");
        }
    }
    
    public void removerUsuario(String id) {
        usuarios.remove(id);
        
        // Persiste no arquivo JSON
        if (context != null) {
            FileUtil.salvarUsuarios(context, usuarios);
            Log.d(TAG, "Usuário removido: " + id);
        } else {
            Log.e(TAG, "Contexto não inicializado. Impossível remover usuário.");
        }
    }
    
    // Verificar se email já está em uso
    public boolean emailJaExiste(String email) {
        for (Usuario usuario : usuarios.values()) {
            if (usuario.getEmail().equals(email)) {
                return true;
            }
        }
        return false;
    }
}