package com.example.face_final;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.custom.FirebaseCustomLocalModel;
import com.google.firebase.ml.custom.FirebaseModelDataType;
import com.google.firebase.ml.custom.FirebaseModelInputOutputOptions;
import com.google.firebase.ml.custom.FirebaseModelInputs;
import com.google.firebase.ml.custom.FirebaseModelInterpreter;
import com.google.firebase.ml.custom.FirebaseModelInterpreterOptions;
import com.google.firebase.ml.custom.FirebaseModelOutputs;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class check extends AppCompatActivity {
TextView tv,tvb;
float min=1000000;
String mins="";
    static final int REQUEST_IMAGE_CAPTURE = 1;
    FirebaseModelInterpreter interpreter;
    FirebaseModelInputOutputOptions inputOutputOptions;
    FirebaseModelInputs inputs;
    ArrayList<list> al= new ArrayList<>();
    String faces="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check);
        tv=findViewById(R.id.tv);
        tvb=findViewById(R.id.tvb);
        SharedPreferences settings = getSharedPreferences("encoded_faces",MODE_PRIVATE);
        String restoredText = settings.getString("faces", null);

        Gson g1=new Gson();
        Type type=new TypeToken<ArrayList<list>>(){}.getType();
        al=g1.fromJson(restoredText,type);
        if(al==null) {
            al = new ArrayList<>();
        }


        FirebaseCustomLocalModel localModel = new FirebaseCustomLocalModel.Builder()
                .setAssetFilePath("model.tflite")
                .build();


        try {
            FirebaseModelInterpreterOptions options =
                    new FirebaseModelInterpreterOptions.Builder(localModel).build();
            interpreter = FirebaseModelInterpreter.getInstance(options);
        } catch (FirebaseMLException e) {
            //z+="option \n";
        }

        try {
            inputOutputOptions =
                    new FirebaseModelInputOutputOptions.Builder()
                            .setInputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, 160, 160, 3})
                            .setOutputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, 128})
                            .build();
        } catch (FirebaseMLException e) {
           // z+="iooption \n";
            e.printStackTrace();
        }

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

        }




    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageBitmap = Bitmap.createScaledBitmap(imageBitmap, 160, 160, true);
            //z+="on act \n";

            int batchNum = 0;
            float[][][][] input = new float[1][160][160][3];
            for (int x = 0; x < 160; x++) {
                for (int y = 0; y < 160; y++) {
                    int pixel = imageBitmap.getPixel(x, y);
                    // Normalize channel values to [-1.0, 1.0]. This requirement varies by
                    // model. For example, some models might require values to be normalized
                    // to the range [0.0, 1.0] instead.
                    input[batchNum][x][y][0] = (Color.red(pixel) - 127) / 128.0f;
                    input[batchNum][x][y][1] = (Color.green(pixel) - 127) / 128.0f;
                    input[batchNum][x][y][2] = (Color.blue(pixel) - 127) / 128.0f;
                }
            }

            try {
                inputs = new FirebaseModelInputs.Builder()
                        .add(input)  // add() as many input arrays as your model requires
                        .build();
            } catch (FirebaseMLException e) {
                //z+="builder \n";
                e.printStackTrace();
            }
            interpreter.run(inputs, inputOutputOptions)
                    .addOnSuccessListener(
                            new OnSuccessListener<FirebaseModelOutputs>() {
                                @Override
                                public void onSuccess(FirebaseModelOutputs result) {
                                    String s="";
                                    final float[][] output = result.getOutput(0);

                                            /*list l=new list(z,output);
                                            al.add(l);
                                            Gson g=new Gson();
                                            String json=g.toJson(al);
                                            tv.setText(json);
                                            SharedPreferences.Editor edit=getSharedPreferences("encoded_faces",MODE_PRIVATE).edit();
                                            edit.putString("faces",json);
                                            edit.apply();
*/                                          for(int i=0;i<al.size();i++){
                                                faces+=al.get(i).getS()+" : ";
                                                float re=fun(output,al.get(i).getInput());
                                                faces+=re+"\n";
                                                if(re<min){
                                                    min=re;
                                                    mins=al.get(i).getS();
                                                }

                                    }
                                    tv.setMovementMethod(new ScrollingMovementMethod());
                                    tv.setText(faces);
                                        min=1-min;
                                        min=min*100;
                                        mins=mins+" "+min+" %";
                                    tvb.setText(mins);

                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                }
                            });

        }




    }
    float fun(float f[][],float f2[][]){
        float r=0,r1=0;
        for(int i=0;i<128;i++){
            r1=f[0][i]-f2[0][i];
            r+=r1*r1;


        }
        r=r/128;
        return r;
    }
}
