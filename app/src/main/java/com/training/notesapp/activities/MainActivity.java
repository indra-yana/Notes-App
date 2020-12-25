package com.training.notesapp.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.training.notesapp.R;
import com.training.notesapp.adapters.NotesAdapter;
import com.training.notesapp.database.NotesDatabase;
import com.training.notesapp.entities.Note;

import java.util.ArrayList;
import java.util.List;

/****************************************************
 * Created by Indra Muliana (indra.ndra26@gmail.com)
 * On Friday, 25/12/2020 10.11
 * https://gitlab.com/indra-yana
 ****************************************************/

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_ADD_NOTE = 1;

    private RecyclerView rvNotesList;
    private List<Note> noteList;
    private NotesAdapter notesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView ivAddNoteMain = findViewById(R.id.ivAddNoteMain);
        ivAddNoteMain.setOnClickListener(v -> startActivityForResult(new Intent(getApplicationContext(), CreateNoteActivity.class), REQUEST_CODE_ADD_NOTE));

        rvNotesList = findViewById(R.id.rvNotesList);
        rvNotesList.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

        noteList = new ArrayList<>();
        notesAdapter = new NotesAdapter(noteList);
        rvNotesList.setAdapter(notesAdapter);

        new GetNoteTask().execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK) {
            new GetNoteTask().execute();
        }
    }

    @SuppressLint("StaticFieldLeak")
    class GetNoteTask extends AsyncTask<Void, Void, List<Note>> {

        @Override
        protected List<Note> doInBackground(Void... voids) {
            return NotesDatabase.getNotesDatabase(getApplicationContext()).noteDao().getAllNotes();
        }

        @Override
        protected void onPostExecute(List<Note> notes) {
            super.onPostExecute(notes);
            if (noteList.size() == 0) {
                noteList.addAll(notes);
                notesAdapter.notifyDataSetChanged();
            } else {
                noteList.add(0, notes.get(0));
                notesAdapter.notifyItemInserted(0);
            }

            rvNotesList.smoothScrollToPosition(0);
        }
    }

}