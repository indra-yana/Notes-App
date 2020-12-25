package com.training.notesapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.training.notesapp.R;
import com.training.notesapp.database.NotesDatabase;
import com.training.notesapp.entities.Note;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/****************************************************
 * Created by Indra Muliana (indra.ndra26@gmail.com)
 * On Friday, 25/12/2020 10.11
 * https://gitlab.com/indra-yana
 ****************************************************/

public class CreateNoteActivity extends AppCompatActivity {

    private EditText etInputNoteTitle, etInputNoteSubtitle, etInputNoteText;
    private TextView tvTextDateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

        ImageView ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(v -> onBackPressed());

        etInputNoteTitle = findViewById(R.id.etInputNoteTitle);
        etInputNoteSubtitle = findViewById(R.id.etInputNoteSubtitle);
        etInputNoteText = findViewById(R.id.etInputNoteText);
        tvTextDateTime = findViewById(R.id.tvTextDateTime);

        tvTextDateTime.setText(new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault()).format(new Date()));

        ImageView ivSaveNote = findViewById(R.id.ivSaveNote);
        ivSaveNote.setOnClickListener(v -> saveNote());
    }

    private void saveNote() {
        if (etInputNoteTitle.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Title can't be empty!", Toast.LENGTH_SHORT).show();
            return;
        } else if (etInputNoteSubtitle.getText().toString().trim().isEmpty() && etInputNoteText.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Note can't be empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        final Note note = new Note();
        note.setTitle(etInputNoteTitle.getText().toString());
        note.setSubtitle(etInputNoteSubtitle.getText().toString());
        note.setNoteText(etInputNoteText.getText().toString());
        note.setDateTime(tvTextDateTime.getText().toString());

        new SaveNoteTask(note).execute();
    }

    @SuppressLint("StaticFieldLeak")
    class SaveNoteTask extends AsyncTask<Void, Void, Void> {

        private Note note;

        public SaveNoteTask(Note note) {
            this.note = note;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            NotesDatabase.getNotesDatabase(getApplicationContext()).noteDao().insertNote(note);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(getApplicationContext(), "Note has been saved!", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK, new Intent());
            finish();
        }
    }

}