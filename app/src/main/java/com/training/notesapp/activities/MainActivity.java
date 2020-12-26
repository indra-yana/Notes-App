package com.training.notesapp.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
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
    public static final int REQUEST_CODE_UPDATE_NOTE = 2;
    public static final int REQUEST_CODE_SHOW_NOTE = 3;

    private RecyclerView rvNotesList;
    private List<Note> noteList;
    private NotesAdapter notesAdapter;

    private int noteClickedPosition = RecyclerView.NO_POSITION;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView ivAddNoteMain = findViewById(R.id.ivAddNoteMain);
        ivAddNoteMain.setOnClickListener(v -> startActivityForResult(new Intent(getApplicationContext(), CreateNoteActivity.class), REQUEST_CODE_ADD_NOTE));

        rvNotesList = findViewById(R.id.rvNotesList);
        rvNotesList.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

        noteList = new ArrayList<>();
        notesAdapter = new NotesAdapter(noteList, (note, position) -> {
            noteClickedPosition = position;
            Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
            intent.putExtra("isViewOrUpdate", true);
            intent.putExtra("note", note);
            startActivityForResult(intent, REQUEST_CODE_UPDATE_NOTE);
        });

        rvNotesList.setAdapter(notesAdapter);

        new GetNoteTask(REQUEST_CODE_SHOW_NOTE, false).execute();

        ImageView ivClearInputSearch = findViewById(R.id.ivClearInputSearch);
        EditText etInputSearch = findViewById(R.id.etInputSearch);
        etInputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                notesAdapter.cancelSearch();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (noteList.size() != 0) {
                    notesAdapter.searchNotes(s.toString());
                }

                if (s.toString().trim().isEmpty()) {
                    ivClearInputSearch.setVisibility(View.GONE);
                } else {
                    ivClearInputSearch.setVisibility(View.VISIBLE);
                }
            }
        });

        ivClearInputSearch.setOnClickListener(v -> {
            etInputSearch.setText(null);
            ivClearInputSearch.setVisibility(View.GONE);
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK) {
            new GetNoteTask(REQUEST_CODE_ADD_NOTE, false).execute();
        } else if (requestCode == REQUEST_CODE_UPDATE_NOTE && resultCode == RESULT_OK) {
            if (data != null) {
                new GetNoteTask(REQUEST_CODE_UPDATE_NOTE, data.getBooleanExtra("isNoteDeleted", false)).execute();
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    class GetNoteTask extends AsyncTask<Void, Void, List<Note>> {

        private int requestCode;
        private boolean isNoteDeleted;

        public GetNoteTask(int requestCode, boolean isNoteDeleted) {
            this.requestCode = requestCode;
            this.isNoteDeleted = isNoteDeleted;
        }

        @Override
        protected List<Note> doInBackground(Void... voids) {
            return NotesDatabase.getNotesDatabase(getApplicationContext()).noteDao().getAllNotes();
        }

        @Override
        protected void onPostExecute(List<Note> notes) {
            super.onPostExecute(notes);

            if (requestCode == REQUEST_CODE_SHOW_NOTE) {
                noteList.addAll(notes);
                notesAdapter.notifyDataSetChanged();
            } else if (requestCode == REQUEST_CODE_ADD_NOTE) {
                noteList.add(0, notes.get(0));
                notesAdapter.notifyItemInserted(0);
            } else if (requestCode == REQUEST_CODE_UPDATE_NOTE) {
                noteList.remove(noteClickedPosition);
                if (isNoteDeleted) {
                    notesAdapter.notifyItemRemoved(noteClickedPosition);
                } else {
                    noteList.add(noteClickedPosition, notes.get(noteClickedPosition));
                    notesAdapter.notifyItemChanged(noteClickedPosition);
                }
            }

            rvNotesList.smoothScrollToPosition(0);
        }
    }

}