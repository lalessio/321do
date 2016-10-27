package com.alessio.luca.a321do;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;

/**
 * Created by Luca on 25/10/2016.
 */
// TODO migliorare gestione memoria
// TODO AUDIO

public class EditMediaActivity extends Activity {
    private static final int RESULT_LOAD_IMAGE = 1;
    private Note note;
    private NoteDBAdapter noteDBAdapter;
    private ImageView imageView;
    private boolean modified;
    private TextView emptyMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        note = (Note) getIntent().getExtras().get("EditNotePayload");
        noteDBAdapter = new NoteDBAdapter(this);
        modified = false;

        setTitle(R.string.editNoteMediaTitle);
        setContentView(R.layout.media_layout);

        imageView = (ImageView) findViewById(R.id.imageView);
        Button chooseImageButton = (Button) findViewById(R.id.buttonChooseImage);
        Button resetButton = (Button) findViewById(R.id.buttonResetMediaImage);
        emptyMessage = (TextView) findViewById(R.id.textViewMediaImage);

        if(note.getImgBytes()==null)
            emptyMessage.setText(R.string.errorEmptyMediaImage);
        else
            imageView.setImageBitmap(BitmapFactory.decodeByteArray(note.getImgBytes(),0,note.getImgBytes().length));

        chooseImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, RESULT_LOAD_IMAGE); //TODO impostare limite di grandezza file (1 mb? 500 kb ok)
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                note.setImgBytes(new byte[0]);
                imageView.setImageResource(android.R.color.transparent);
                emptyMessage.setText(R.string.errorEmptyMediaImage);
                modified = true;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            Bitmap fileDecoded = BitmapFactory.decodeFile(picturePath);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            fileDecoded.compress(Bitmap.CompressFormat.JPEG, 50, outputStream); //sopporta 70 senza problemi (sul mio dispositivo)

            note.setImgBytes(outputStream.toByteArray());
            imageView.setImageBitmap(fileDecoded);

            emptyMessage.setText("");
            modified = true;
        }
    }

    @Override
    protected void onPause() {
        if(modified)
            noteDBAdapter.updateNote(note);
        super.onPause();
    }
}
