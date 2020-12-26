package com.training.notesapp.listeners;

import com.training.notesapp.entities.Note;

/****************************************************
 * Created by Indra Muliana (indra.ndra26@gmail.com)
 * On Saturday, 26/12/2020 16.50
 * https://gitlab.com/indra-yana
 ****************************************************/

public interface NotesListener {
    void onNoteClicked(Note note, int position);
}
