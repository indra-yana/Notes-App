package com.training.notesapp.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

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
    public static final int REQUEST_CODE_SELECT_IMAGE = 4;
    public static final int REQUEST_CODE_STORAGE_PERMISSION = 5;

    private RecyclerView rvNotesList;
    private List<Note> noteList;
    private NotesAdapter notesAdapter;

    private int noteClickedPosition = RecyclerView.NO_POSITION;

    private AlertDialog dialogAddURL;

    private int currentViewType = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView ivAddNoteMain = findViewById(R.id.ivAddNoteMain);
        ivAddNoteMain.setOnClickListener(v -> startActivityForResult(new Intent(getApplicationContext(), CreateNoteActivity.class), REQUEST_CODE_ADD_NOTE));

        rvNotesList = findViewById(R.id.rvNotesList);
        // rvNotesList.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        setViewType();

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

        // Setup QuickAction Button
        findViewById(R.id.ivQuickActionAddNote).setOnClickListener(v -> startActivityForResult(new Intent(getApplicationContext(), CreateNoteActivity.class), REQUEST_CODE_ADD_NOTE));
        findViewById(R.id.ivQuickActionAddImage).setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE_PERMISSION);
            } else {
                selectImage();
            }
        });
        findViewById(R.id.ivQuickActionAddWebLink).setOnClickListener(v -> showAddURLDialog());

        findViewById(R.id.ivViewType).setOnClickListener(v -> setViewType());
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
        } else if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    try {
                        String selectedImagePath = getPathFromUri(selectedImageUri);
                        Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
                        intent.putExtra("isFromQuickActions", true);
                        intent.putExtra("quickActionType", "addImage");
                        intent.putExtra("imagePath", selectedImagePath);
                        startActivityForResult(intent, REQUEST_CODE_ADD_NOTE);
                    } catch (Exception ex) {
                        Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0) {
            selectImage();
        } else {
            Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
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

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
        }
    }

    private String getPathFromUri(Uri contentUri) {
        String filePath;
        Cursor cursor = getContentResolver().query(contentUri, null, null, null, null);

        if (cursor == null) {
            filePath = contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex("_data");
            filePath = cursor.getString(index);
            cursor.close();
        }

        return filePath;
    }

    private void showAddURLDialog() {
        if (dialogAddURL == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            View view = LayoutInflater.from(this).inflate(R.layout.layout_add_url, (ViewGroup) findViewById(R.id.layoutAddUrlContainer));

            builder.setView(view);

            dialogAddURL = builder.create();
            if (dialogAddURL.getWindow() != null) {
                dialogAddURL.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            final EditText etInputURL = view.findViewById(R.id.etInputURL);
            etInputURL.requestFocus();

            view.findViewById(R.id.tvAddURL).setOnClickListener(v -> {
                if (etInputURL.getText().toString().trim().isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please Enter URL!", Toast.LENGTH_SHORT).show();
                } else if (!Patterns.WEB_URL.matcher(etInputURL.getText().toString()).matches()) {
                    Toast.makeText(MainActivity.this, "Please Enter Valid URL!", Toast.LENGTH_SHORT).show();
                } else {
                    dialogAddURL.dismiss();

                    Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
                    intent.putExtra("isFromQuickActions", true);
                    intent.putExtra("quickActionType", "addWebURL");
                    intent.putExtra("webURL", etInputURL.getText().toString());
                    startActivityForResult(intent, REQUEST_CODE_ADD_NOTE);
                }
            });

            view.findViewById(R.id.tvCancelAddURL).setOnClickListener(v -> dialogAddURL.dismiss());
        }

        dialogAddURL.show();
    }

    private void setViewType() {
        ImageView imageView = findViewById(R.id.ivViewType);

        if (currentViewType == 1) {
            rvNotesList.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
            imageView.setImageResource(R.drawable.ic_view_list);
            currentViewType = 2;
        } else {
            rvNotesList.setLayoutManager(new LinearLayoutManager(this));
            imageView.setImageResource(R.drawable.ic_view_grid);
            currentViewType = 1;
        }
    }

}