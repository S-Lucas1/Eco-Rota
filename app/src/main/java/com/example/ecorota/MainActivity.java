package com.example.ecorota;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Toast.makeText(this, "Olá FACENS!!", Toast.LENGTH_LONG).show();
        TextView pessoa = new TextView(this);
        pessoa.setText("");
        setContentView(pessoa);
    }
}