package com.example.ecorota.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecorota.R;
import com.example.ecorota.model.Lixeira;

import java.util.List;

public class LixeiraAdapter extends RecyclerView.Adapter<LixeiraAdapter.LixeiraViewHolder> {

    private final List<Lixeira> lixeiras;
    private final Context context;
    private OnLixeiraClickListener listener;
    
    public interface OnLixeiraClickListener {
        void onLixeiraClick(Lixeira lixeira, int position);
    }
    
    public LixeiraAdapter(Context context, List<Lixeira> lixeiras) {
        this.context = context;
        this.lixeiras = lixeiras;
    }
    
    public void setOnLixeiraClickListener(OnLixeiraClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public LixeiraViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_lixeira, parent, false);
        return new LixeiraViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LixeiraViewHolder holder, int position) {
        Lixeira lixeira = lixeiras.get(position);
        
        holder.tvLixeiraId.setText("ID: " + lixeira.getID());
        holder.tvLixeiraLocalizacao.setText(String.format("Lat: %.4f, Long: %.4f", 
                                           lixeira.getLatitude(), lixeira.getLongitude()));
        
        // Formatar o nível como porcentagem
        int nivelPorcentagem = (int) (lixeira.getNivelEnchimento() * 100);
        holder.tvLixeiraNivel.setText("Nível: " + nivelPorcentagem + "%");
        
        holder.tvLixeiraStatus.setText("Status: " + lixeira.getStatus());
        
        // Definir cor do status baseado no nível
        int cor;
        switch (lixeira.getStatus()) {
            case "CHEIA":
                cor = android.graphics.Color.RED;
                break;
            case "PARCIAL":
                cor = android.graphics.Color.rgb(255, 165, 0); // Laranja
                break;
            case "VAZIA":
                cor = android.graphics.Color.GREEN;
                break;
            case "MANUTENCAO":
                cor = android.graphics.Color.GRAY;
                break;
            default:
                cor = android.graphics.Color.BLACK;
        }
        holder.tvLixeiraStatus.setTextColor(cor);
        
        // Carregar a imagem da lixeira (sempre usando a imagem padrão neste momento)
        if (holder.ivLixeiraThumb != null) {
            // No futuro, podemos usar bibliotecas como Glide ou Picasso para carregar imagens 
            // de forma mais eficiente, especialmente se vierem da internet
            int resourceId = context.getResources().getIdentifier(
                "lixeira_exemplo", "drawable", context.getPackageName());
            if (resourceId != 0) {
                holder.ivLixeiraThumb.setImageResource(resourceId);
            }
        }
        
        // Configurar click listener
        if (listener != null) {
            holder.itemView.setOnClickListener(v -> {
                listener.onLixeiraClick(lixeira, position);
            });
        }
    }

    @Override
    public int getItemCount() {
        return lixeiras != null ? lixeiras.size() : 0;
    }
    
    public void atualizarLixeiras(List<Lixeira> novasLixeiras) {
        this.lixeiras.clear();
        this.lixeiras.addAll(novasLixeiras);
        notifyDataSetChanged();
    }

    static class LixeiraViewHolder extends RecyclerView.ViewHolder {
        TextView tvLixeiraId, tvLixeiraLocalizacao, tvLixeiraNivel, tvLixeiraStatus;
        ImageView ivLixeiraThumb;

        public LixeiraViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLixeiraId = itemView.findViewById(R.id.tvLixeiraId);
            tvLixeiraLocalizacao = itemView.findViewById(R.id.tvLixeiraLocalizacao);
            tvLixeiraNivel = itemView.findViewById(R.id.tvLixeiraNivel);
            tvLixeiraStatus = itemView.findViewById(R.id.tvLixeiraStatus);
            ivLixeiraThumb = itemView.findViewById(R.id.ivLixeiraThumb);
        }
    }
}
