package com.training.notesapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.training.notesapp.R;
import com.training.notesapp.entities.Note;

import java.util.List;

/****************************************************
 * Created by Indra Muliana (indra.ndra26@gmail.com)
 * On Friday, 25/12/2020 22.11
 * https://gitlab.com/indra-yana
 ****************************************************/

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NotesViewHolder> {

    private List<Note> notes;

    public NotesAdapter(List<Note> notes) {
        this.notes = notes;
    }

    @NonNull
    @Override
    public NotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NotesViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_container_note, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull NotesViewHolder holder, int position) {
        holder.setNote(notes.get(position));
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    static class NotesViewHolder extends RecyclerView.ViewHolder {

        private TextView tvTextTitle, tvTextSubtitle, tvTextDateTime;

        public NotesViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTextTitle = itemView.findViewById(R.id.tvTextTitle);
            tvTextSubtitle = itemView.findViewById(R.id.tvTextSubtitle);
            tvTextDateTime = itemView.findViewById(R.id.tvTextDateTime);
        }

        public void setNote(Note note) {
            if (note.getSubtitle().trim().isEmpty()) {
                tvTextSubtitle.setVisibility(View.GONE);
            } else {
                tvTextSubtitle.setText(note.getSubtitle());
            }

            tvTextTitle.setText(note.getTitle());
            tvTextDateTime.setText(note.getDateTime());
        }
    }

}
