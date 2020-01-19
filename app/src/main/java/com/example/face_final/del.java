package com.example.face_final;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class del extends AppCompatActivity {
EditText et;
Button b;

    ArrayList<list> al= new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_del);
        et=findViewById(R.id.et);
        b=findViewById(R.id.but);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String s=et.getText().toString();
                SharedPreferences settings = getSharedPreferences("encoded_faces",MODE_PRIVATE);
                String restoredText = settings.getString("faces", null);

                Gson g1=new Gson();
                Type type=new TypeToken<ArrayList<list>>(){}.getType();
                al=g1.fromJson(restoredText,type);
                if(al==null) {
                    al = new ArrayList<>();
                }
                for(int i=0;i<al.size();i++){
                    if(al.get(i).getS().equals(s)) {
                        al.remove(i);
                        break;
                    }
                }


                Gson g=new Gson();
                String json=g.toJson(al);
                SharedPreferences.Editor edit=getSharedPreferences("encoded_faces",MODE_PRIVATE).edit();
                edit.putString("faces",json);
                edit.apply();
                Intent i=new Intent(del.this,MainActivity.class);
                startActivity(i);
                finish();

            }
        });


    }
}
