package com.example.ecorota.service;

import android.content.Context;

import com.example.ecorota.model.Usuario;
import com.example.ecorota.repository.UsuarioRepository;

import java.util.List;

public class UsuarioService {
    private static UsuarioService instance;
    private final UsuarioRepository usuarioRepository;
    
    private UsuarioService() {
        usuarioRepository = UsuarioRepository.getInstance();
    }
    
    public static UsuarioService getInstance() {
        if (instance == null) {
            instance = new UsuarioService();
        }
        return instance;
    }
    
    public void inicializar(Context context) {
        if (context == null) {
            android.util.Log.e("UsuarioService", "Tentativa de inicializar com contexto nulo");
            return;
        }
        
        // Usar o contexto do aplicativo para evitar vazamentos de memória
        Context applicationContext = context.getApplicationContext();
        android.util.Log.d("UsuarioService", "Inicializando UsuarioService com contexto: " + 
                          applicationContext.getClass().getName());
        
        usuarioRepository.inicializar(applicationContext);
    }
      public Usuario autenticar(String email, String senha) {
        return usuarioRepository.autenticarUsuario(email, senha);
    }
    
    public List<Usuario> getMotoristas() {
        return usuarioRepository.getMotoristas();
    }
    
    public Usuario getUsuarioPorId(String id) {
        return usuarioRepository.getUsuarioPorId(id);
    }
    
    public List<Usuario> getTodosUsuarios() {
        return usuarioRepository.getTodosUsuarios();
    }
    
    public void salvarUsuario(Usuario usuario) {
        usuarioRepository.salvarUsuario(usuario);
    }
    
    public void removerUsuario(String id) {
        usuarioRepository.removerUsuario(id);
    }
    
    // Verifica se o email já está em uso
    public boolean emailJaExiste(String email) {
        return usuarioRepository.emailJaExiste(email);
    }
}