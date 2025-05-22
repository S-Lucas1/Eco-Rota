package com.example.ecorota;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecorota.adapter.LixeiraAdapter;
import com.example.ecorota.model.Lixeira;
import com.example.ecorota.repository.LixeiraRepository;
import com.example.ecorota.repository.LixeiraFirebaseRepository;
import com.example.ecorota.util.LixeiraFileUtil;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LixeirasActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {    private static final String TAG = "LixeirasActivity";
    private RecyclerView recyclerView;
    private LixeiraAdapter adapter;
    private List<Lixeira> lixeirasList;
    private LixeiraRepository lixeiraRepository;
    private LixeiraFirebaseRepository lixeiraFirebaseRepository;@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lixeiras);
        
        // Verificar arquivos de dados primeiro
        LixeiraFileUtil.verificarDiretorioArquivos(this);
          // Inicializar repositories
        lixeiraRepository = LixeiraRepository.getInstance();
        lixeiraRepository.inicializar(this);
        
        // Inicializar repository do Firebase
        lixeiraFirebaseRepository = LixeiraFirebaseRepository.getInstance();
        lixeiraFirebaseRepository.setContext(this);
          // Configurar BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        // Garantir que o listener está corretamente configurado
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        // Definir o item selecionado
        bottomNavigationView.setSelectedItemId(R.id.nav_lixeiras);

        // Configurar RecyclerView
        recyclerView = findViewById(R.id.recyclerViewLixeiras);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        lixeirasList = new ArrayList<>();
        adapter = new LixeiraAdapter(this, lixeirasList);
        recyclerView.setAdapter(adapter);
        
        // Carregar lixeiras assim que a tela é criada
        carregarLixeiras();

        // Configurar clique em item da lista
        adapter.setOnLixeiraClickListener((lixeira, position) -> {
            // Abrir tela de edição de lixeira
            Intent intent = new Intent(LixeirasActivity.this, CadastroLixeiraActivity.class);
            intent.putExtra("lixeira_id", lixeira.getID());
            startActivity(intent);
        });
        
        // Configurar o botão de adicionar
        FloatingActionButton fabAddLixeira = findViewById(R.id.fabAdicionarLixeira);
        fabAddLixeira.setOnClickListener(v -> {
            // Abrir tela de cadastro de lixeira
            Intent intent = new Intent(LixeirasActivity.this, CadastroLixeiraActivity.class);
            startActivity(intent);
        });
    }    @Override
    protected void onResume() {
        super.onResume();
        carregarLixeiras();
    }
    private void carregarLixeiras() {
        try {
            Log.d(TAG, "Iniciando carregamento de lixeiras");
            
            // Verificar diretório de arquivos primeiro para detectar problemas
            LixeiraFileUtil.verificarDiretorioArquivos(this);
            
            // Verificar se os repositórios estão inicializados
            if (lixeiraRepository == null) {
                Log.e(TAG, "Erro: lixeiraRepository não foi inicializado");
                lixeiraRepository = LixeiraRepository.getInstance();
                lixeiraRepository.inicializar(this);
            }
            
            if (lixeiraFirebaseRepository == null) {
                Log.d(TAG, "Inicializando lixeiraFirebaseRepository");
                lixeiraFirebaseRepository = LixeiraFirebaseRepository.getInstance();
                lixeiraFirebaseRepository.setContext(this);
            }
            
            // Primeiro tenta carregar do Firebase
            lixeiraFirebaseRepository.carregarLixeiras(new LixeiraFirebaseRepository.LixeirasCallback() {
                @Override
                public void onLixeirasCarregadas(List<Lixeira> lixeiras) {
                    // Atualiza a interface com as lixeiras do Firebase
                    atualizarListaLixeiras(lixeiras);
                    Log.d(TAG, "Lixeiras carregadas do Firebase: " + lixeiras.size());
                      // Sincronizar com o repositório local para backup
                    for (Lixeira lixeira : lixeiras) {
                        lixeiraRepository.salvarLixeira(lixeira);
                    }
                }
                
                @Override
                public void onErro(String erro) {
                    Log.e(TAG, "Erro ao carregar lixeiras do Firebase: " + erro);
                    Toast.makeText(LixeirasActivity.this, "Erro ao carregar do Firebase. Usando dados locais.", Toast.LENGTH_SHORT).show();
                    
                    // Em caso de falha, carrega do repositório local
                    carregarLixeirasLocal();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Erro ao carregar lixeiras: " + e.getMessage(), e);
            Toast.makeText(this, "Erro ao carregar lixeiras: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            
            // Em caso de exceção, tenta carregar do repositório local
            carregarLixeirasLocal();
        }
    }
    
    private void carregarLixeirasLocal() {
        try {
            // Carregar lista de lixeiras do repository local
            List<Lixeira> lixeiras = null;
            try {
                lixeiras = lixeiraRepository.getTodas();
                Log.d(TAG, "Lixeiras carregadas localmente: " + (lixeiras != null ? lixeiras.size() : "null"));
            } catch (Exception e) {
                Log.e(TAG, "Erro ao recuperar lixeiras do repositório local: " + e.getMessage(), e);
                // Tentar limpar dados como recuperação de erro
                LixeiraFileUtil.limparDadosLocais(this);
                
                // Reinicializar o repositório
                lixeiraRepository = LixeiraRepository.getInstance();
                lixeiraRepository.inicializar(this);
                
                // Tentar novamente
                lixeiras = lixeiraRepository.getTodas();
            }
            
            // Se lixeiras ainda é null, inicializar com lista vazia
            if (lixeiras == null) {
                lixeiras = new ArrayList<>();
            }
              
            if (lixeiras.isEmpty()) {
                Log.w(TAG, "Nenhuma lixeira encontrada no repositório. Forçando inicialização com dados padrão...");
                
                // Limpar dados locais para forçar reinicialização
                boolean limpezaOK = LixeiraFileUtil.limparDadosLocais(this);
                Log.d(TAG, "Limpeza de dados locais: " + (limpezaOK ? "sucesso" : "falha"));
                
                // Tentar inicializar o repositório novamente
                lixeiraRepository = LixeiraRepository.getInstance();
                lixeiraRepository.inicializar(this);
                
                // Tentar carregar lixeiras novamente
                lixeiras = lixeiraRepository.getTodas();
                Log.d(TAG, "Lixeiras após reinicialização forçada: " + (lixeiras != null ? lixeiras.size() : "null"));
                
                // Se ainda estiver vazio, algo está muito errado
                if (lixeiras == null || lixeiras.isEmpty()) {
                    Log.e(TAG, "Falha crítica ao carregar dados de lixeiras mesmo após reinicialização");
                    // Exibir mensagem de erro para o usuário aqui (opcional)
                }
            } else {
                // Exibir as primeiras lixeiras para debug
                int count = 0;
                for (Lixeira lixeira : lixeiras) {
                    if (count++ < 3) {
                        Log.d(TAG, "Lixeira: ID=" + lixeira.getID() + 
                              ", Lat=" + lixeira.getLatitude() + 
                              ", Long=" + lixeira.getLongitude() + 
                              ", Nível=" + lixeira.getNivelEnchimento() + 
                              ", Sensor=" + (lixeira.getSensor() != null ? lixeira.getSensor().getID() : "null"));
                    }
                }
            }
            
            // Verificar se o adapter está inicializado (poderia não estar se a atividade foi recriada)
            if (adapter == null) {
                Log.w(TAG, "Adapter estava null, criando novo adapter");
                lixeirasList = new ArrayList<>();
                adapter = new LixeiraAdapter(this, lixeirasList);
                if (recyclerView != null) {
                    recyclerView.setAdapter(adapter);
                }
            }
            
            // Verificar se a lista está inicializada
            if (lixeirasList == null) {
                Log.w(TAG, "Lista de lixeiras estava null, criando nova lista");
                lixeirasList = new ArrayList<>();
            }
            
            // Atualizar adapter com segurança
            if (adapter != null) {
                // Criar uma cópia defensiva da lista
                final List<Lixeira> lixeirasFinais = new ArrayList<>(lixeiras);
                
                runOnUiThread(() -> {
                    try {                lixeirasList.clear();
                        lixeirasList.addAll(lixeirasFinais);
                        Log.d(TAG, "Atualizando o adapter com " + lixeirasList.size() + " lixeiras");
                        adapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        Log.e(TAG, "Erro ao atualizar o adapter: " + e.getMessage(), e);
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao carregar lixeiras: " + e.getMessage(), e);
        }
    }
    
    /**
     * Atualiza a lista de lixeiras na UI
     */
    private void atualizarListaLixeiras(List<Lixeira> lixeiras) {
        // Verificar se o adapter está inicializado
        if (adapter == null) {
            Log.w(TAG, "Adapter estava null, criando novo adapter");
            lixeirasList = new ArrayList<>();
            adapter = new LixeiraAdapter(this, lixeirasList);
            if (recyclerView != null) {
                recyclerView.setAdapter(adapter);
            }
        }
        
        // Verificar se a lista está inicializada
        if (lixeirasList == null) {
            Log.w(TAG, "Lista de lixeiras estava null, criando nova lista");
            lixeirasList = new ArrayList<>();
        }
        
        // Atualizar adapter com segurança na UI thread
        if (adapter != null) {
            // Criar uma cópia defensiva da lista
            final List<Lixeira> lixeirasFinais = new ArrayList<>(lixeiras);
            
            runOnUiThread(() -> {
                try {
                    lixeirasList.clear();
                    lixeirasList.addAll(lixeirasFinais);
                    Log.d(TAG, "Atualizando o adapter com " + lixeirasList.size() + " lixeiras do Firebase");
                    adapter.notifyDataSetChanged();
                } catch (Exception e) {
                    Log.e(TAG, "Erro ao atualizar o adapter com dados do Firebase: " + e.getMessage(), e);
                }
            });
        }
    }
    
      @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        Log.d(TAG, "Item de navegação selecionado: " + item.getTitle());
        
        if (itemId == R.id.nav_home) {
            Intent intent = new Intent(this, InicioActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish(); // Fechar esta atividade para evitar pilha de atividades
            return true;
        } else if (itemId == R.id.nav_mapa) {
            // Navegação para a tela de Mapa
            Intent intent = new Intent(this, Mapa.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish(); // Fechar esta atividade para evitar pilha de atividades
            return true;
        } else if (itemId == R.id.nav_lixeiras) {
            // Já estamos na LixeirasActivity, apenas recarregamos os dados
            Log.d(TAG, "Já estamos na LixeirasActivity - atualizando dados");
            // Recarregar lixeiras para garantir que a lista está atualizada
            try {
                carregarLixeiras();
            } catch (Exception e) {
                Log.e(TAG, "Erro ao carregar lixeiras: " + e.getMessage(), e);
            }
            return true;
        } else if (itemId == R.id.nav_reciclagem) {
            // Navegação para tela de reciclagem
            try {
                Class<?> reciclagemActivityClass = Class.forName("com.example.ecorota.ReciclagemActivity");
                Intent intent = new Intent(this, reciclagemActivityClass);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish(); // Fechar esta atividade
            } catch (ClassNotFoundException e) {
                Log.e(TAG, "Classe ReciclagemActivity não encontrada", e);
                // Mostrar mensagem para o usuário em vez de redirecionar silenciosamente
                Log.d(TAG, "Função de reciclagem ainda não implementada");
                // Mantendo na tela atual em vez de navegar para outra
                return true;
            }
            return true;
        }
        
        return false;
    }
}
