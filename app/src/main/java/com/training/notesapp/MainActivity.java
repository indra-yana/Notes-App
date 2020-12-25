package com.training.notesapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

/****************************************************
 * Created by Indra Muliana (indra.ndra26@gmail.com)
 * On Friday, 25/12/2020 10.11
 * https://gitlab.com/indra-yana
 ****************************************************/

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_ADD_NOTE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView ivAddNoteMain = findViewById(R.id.ivAddNoteMain);
        ivAddNoteMain.setOnClickListener(v -> startActivityForResult(new Intent(getApplicationContext(), CreateNoteActivity.class), REQUEST_CODE_ADD_NOTE));

    }
}