package com.training.notesapp.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.training.notesapp.R;
import com.training.notesapp.database.NotesDatabase;
import com.training.notesapp.entities.Note;

import java.io.InputStream;
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
    private TextView tvTextDateTime, tvWebURL;
    private View viewSubtitleIndicator;
    private ImageView ivImageNote;
    private LinearLayout layoutWebURL;

    private String selectedNoteColor;
    private String selectedImagePath;

    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;
    private static final int REQUEST_CODE_SELECT_IMAGE = 2;

    private AlertDialog dialogAddURL;
    private AlertDialog dialogDeleteNote;

    private Note alreadyAvailableNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

        ImageView ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(v -> {
            onBackPressed();
            hideKeyboard(this);
        });

        etInputNoteTitle = findViewById(R.id.etInputNoteTitle);
        etInputNoteSubtitle = findViewById(R.id.etInputNoteSubtitle);
        etInputNoteText = findViewById(R.id.etInputNoteText);
        tvTextDateTime = findViewById(R.id.tvTextDateTime);
        tvWebURL = findViewById(R.id.tvWebURL);
        viewSubtitleIndicator = findViewById(R.id.viewSubtitleIndicator);
        ivImageNote = findViewById(R.id.ivImageNotePreview);
        layoutWebURL = findViewById(R.id.layoutWebURL);

        tvTextDateTime.setText(new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault()).format(new Date()));

        ImageView ivSaveNote = findViewById(R.id.ivSaveNote);
        ivSaveNote.setOnClickListener(v -> saveNote());

        selectedNoteColor = "#333333";
        selectedImagePath = "";

        if (getIntent().getBooleanExtra("isViewOrUpdate", false)) {
            TextView textView = findViewById(R.id.tvMenuTitle);
            textView.setText(R.string.text_update_note);

            alreadyAvailableNote = (Note) getIntent().getSerializableExtra("note");
            setViewOrUpdateNote();
        }

        findViewById(R.id.ivRemoveWebURL).setOnClickListener(v -> {
            tvWebURL.setText(null);
            layoutWebURL.setVisibility(View.GONE);
        });

        findViewById(R.id.ivRemoveImageNote).setOnClickListener(v -> {
            ivImageNote.setImageBitmap(null);
            ivImageNote.setVisibility(View.GONE);
            selectedImagePath = "";

            findViewById(R.id.ivRemoveImageNote).setVisibility(View.GONE);
        });

        if (getIntent().getBooleanExtra("isFromQuickActions", false)) {
            String type = getIntent().getStringExtra("quickActionType");
            if (type != null) {
                if (type.equals("addImage")) {
                    selectedImagePath = getIntent().getStringExtra("imagePath");
                    ivImageNote.setImageBitmap(BitmapFactory.decodeFile(selectedImagePath));
                    ivImageNote.setVisibility(View.VISIBLE);
                    findViewById(R.id.ivRemoveImageNote).setVisibility(View.VISIBLE);
                } else if (type.equals("addWebURL")) {
                    tvWebURL.setText(getIntent().getStringExtra("webURL"));
                    layoutWebURL.setVisibility(View.VISIBLE);
                }
            }
        }

        initMiscellaneous();
        setSubtitleIndicator();
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
        note.setColor(selectedNoteColor);
        note.setImagePath(selectedImagePath);

        if (layoutWebURL.getVisibility() == View.VISIBLE) {
            note.setWebLink(tvWebURL.getText().toString());
        }

        if (alreadyAvailableNote != null) {
            note.setId(alreadyAvailableNote.getId());
        }

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
            hideKeyboard(CreateNoteActivity.this);
        }
    }

    private void setViewOrUpdateNote() {
        etInputNoteTitle.setText(alreadyAvailableNote.getTitle());
        etInputNoteSubtitle.setText(alreadyAvailableNote.getSubtitle());
        etInputNoteText.setText(alreadyAvailableNote.getNoteText());
        tvTextDateTime.setText(alreadyAvailableNote.getDateTime());

        if (alreadyAvailableNote.getImagePath() != null && !alreadyAvailableNote.getImagePath().trim().isEmpty()) {
            ivImageNote.setImageBitmap(BitmapFactory.decodeFile(alreadyAvailableNote.getImagePath()));
            ivImageNote.setVisibility(View.VISIBLE);
            findViewById(R.id.ivRemoveImageNote).setVisibility(View.VISIBLE);
            selectedImagePath = alreadyAvailableNote.getImagePath();
        }

        if (alreadyAvailableNote.getWebLink() != null && !alreadyAvailableNote.getWebLink().trim().isEmpty()) {
            tvWebURL.setText(alreadyAvailableNote.getWebLink());
            layoutWebURL.setVisibility(View.VISIBLE);
        }
    }

    private void initMiscellaneous() {
        final LinearLayout layoutMiscellaneous = findViewById(R.id.layoutMiscellaneous);
        final BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(layoutMiscellaneous);
        layoutMiscellaneous.findViewById(R.id.tvTextMiscellaneous).setOnClickListener(v -> {
            if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            } else {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        final ImageView imageColor1 = layoutMiscellaneous.findViewById(R.id.imageColor1);
        final ImageView imageColor2 = layoutMiscellaneous.findViewById(R.id.imageColor2);
        final ImageView imageColor3 = layoutMiscellaneous.findViewById(R.id.imageColor3);
        final ImageView imageColor4 = layoutMiscellaneous.findViewById(R.id.imageColor4);
        final ImageView imageColor5 = layoutMiscellaneous.findViewById(R.id.imageColor5);

        layoutMiscellaneous.findViewById(R.id.viewColor1).setOnClickListener(v -> {
            selectedNoteColor = "#333333";
            imageColor1.setImageResource(R.drawable.ic_done);
            imageColor2.setImageResource(0);
            imageColor3.setImageResource(0);
            imageColor4.setImageResource(0);
            imageColor5.setImageResource(0);
            setSubtitleIndicator();
        });

        layoutMiscellaneous.findViewById(R.id.viewColor2).setOnClickListener(v -> {
            selectedNoteColor = "#FDBE3B";
            imageColor1.setImageResource(0);
            imageColor2.setImageResource(R.drawable.ic_done);
            imageColor3.setImageResource(0);
            imageColor4.setImageResource(0);
            imageColor5.setImageResource(0);
            setSubtitleIndicator();
        });

        layoutMiscellaneous.findViewById(R.id.viewColor3).setOnClickListener(v -> {
            selectedNoteColor = "#FF4842";
            imageColor1.setImageResource(0);
            imageColor2.setImageResource(0);
            imageColor3.setImageResource(R.drawable.ic_done);
            imageColor4.setImageResource(0);
            imageColor5.setImageResource(0);
            setSubtitleIndicator();
        });

        layoutMiscellaneous.findViewById(R.id.viewColor4).setOnClickListener(v -> {
            selectedNoteColor = "#3A52FC";
            imageColor1.setImageResource(0);
            imageColor2.setImageResource(0);
            imageColor3.setImageResource(0);
            imageColor4.setImageResource(R.drawable.ic_done);
            imageColor5.setImageResource(0);
            setSubtitleIndicator();
        });

        layoutMiscellaneous.findViewById(R.id.viewColor5).setOnClickListener(v -> {
            selectedNoteColor = "#000000";
            imageColor1.setImageResource(0);
            imageColor2.setImageResource(0);
            imageColor3.setImageResource(0);
            imageColor4.setImageResource(0);
            imageColor5.setImageResource(R.drawable.ic_done);
            setSubtitleIndicator();
        });

        if (alreadyAvailableNote != null && alreadyAvailableNote.getColor() != null && !alreadyAvailableNote.getColor().trim().isEmpty()) {
            switch (alreadyAvailableNote.getColor()) {
                case "#FDBE3B":
                    layoutMiscellaneous.findViewById(R.id.viewColor2).performClick();
                    break;
                case "#FF4842":
                    layoutMiscellaneous.findViewById(R.id.viewColor3).performClick();
                    break;
                case "#3A52FC":
                    layoutMiscellaneous.findViewById(R.id.viewColor4).performClick();
                    break;
                case "#000000":
                    layoutMiscellaneous.findViewById(R.id.viewColor5).performClick();
                    break;

            }
        }

        layoutMiscellaneous.findViewById(R.id.layoutAddImage).setOnClickListener(v -> {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(CreateNoteActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE_PERMISSION);
            } else {
                selectImage();
            }
        });

        layoutMiscellaneous.findViewById(R.id.layoutAddUrl).setOnClickListener(v -> {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            showAddURLDialog();
        });

        if (alreadyAvailableNote != null) {
            layoutMiscellaneous.findViewById(R.id.layoutDeleteNote).setVisibility(View.VISIBLE);
            layoutMiscellaneous.findViewById(R.id.layoutDeleteNote).setOnClickListener(v -> {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                showDeleteNoteDialog();
            });
        }

    }

    private void setSubtitleIndicator() {
        GradientDrawable gradientDrawable = (GradientDrawable) viewSubtitleIndicator.getBackground();
        gradientDrawable.setColor(Color.parseColor(selectedNoteColor));
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
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

    public void hideKeyboard(Activity activity) {
        View view = activity.findViewById(android.R.id.content);
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        ivImageNote.setImageBitmap(bitmap);
                        ivImageNote.setVisibility(View.VISIBLE);
                        findViewById(R.id.ivRemoveImageNote).setVisibility(View.VISIBLE);

                        selectedImagePath = getPathFromUri(selectedImageUri);
                    } catch (Exception ex) {
                        Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
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
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
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
                    Toast.makeText(CreateNoteActivity.this, "Please Enter URL!", Toast.LENGTH_SHORT).show();
                } else if (!Patterns.WEB_URL.matcher(etInputURL.getText().toString()).matches()) {
                    Toast.makeText(CreateNoteActivity.this, "Please Enter Valid URL!", Toast.LENGTH_SHORT).show();
                } else {
                    tvWebURL.setText(etInputURL.getText().toString());
                    layoutWebURL.setVisibility(View.VISIBLE);
                    dialogAddURL.dismiss();
                }
            });

            view.findViewById(R.id.tvCancelAddURL).setOnClickListener(v -> dialogAddURL.dismiss());
        }

        dialogAddURL.show();
    }

    private void showDeleteNoteDialog() {
        if (dialogDeleteNote == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
            View view = LayoutInflater.from(this).inflate(R.layout.layout_delete_note, (ViewGroup) findViewById(R.id.layoutDeleteNoteContainer));

            builder.setView(view);
            dialogDeleteNote = builder.create();

            if (dialogDeleteNote.getWindow() != null) {
                dialogDeleteNote.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            view.findViewById(R.id.tvDeleteNote).setOnClickListener(v -> new DeleteNoteTask().execute());
            view.findViewById(R.id.tvCancelDeleteNote).setOnClickListener(v -> dialogDeleteNote.dismiss());
        }

        dialogDeleteNote.show();
    }

    @SuppressLint("StaticFieldLeak")
    class DeleteNoteTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            NotesDatabase.getNotesDatabase(getApplicationContext()).noteDao().deleteNote(alreadyAvailableNote);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Intent intent = new Intent();
            intent.putExtra("isNoteDeleted", true);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

}