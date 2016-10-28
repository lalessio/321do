package com.alessio.luca.a321do;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.view.Display;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Luca on 25/10/2016.
 */
// TODO migliorare gestione memoria
// TODO AUDIO

public class EditMediaActivity extends Activity {
    public static final int RESULT_LOAD_IMAGE = 1;
    public static final int REQUEST_CAMERA = 2;
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
        emptyMessage = (TextView) findViewById(R.id.textViewMediaImage);

        if(note.getImgBytes()==null)
            emptyMessage.setText(R.string.errorEmptyMediaImage);
        else
            imageView.setImageBitmap(BitmapFactory.decodeByteArray(note.getImgBytes(),0,note.getImgBytes().length));

        chooseImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(EditMediaActivity.this);
                ListView modeListView = new ListView(EditMediaActivity.this);
                String[] modes = new String[] { getString(R.string.optionMediaChooseFromGallery), getString(R.string.optionMediaTakePhoto), getString(R.string.optionMediaDeleteAttachment) };
                ArrayAdapter<String> modeAdapter = new ArrayAdapter<>(EditMediaActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1, modes);
                modeListView.setAdapter(modeAdapter);
                builder.setView(modeListView);
                final Dialog dialog = builder.create();
                dialog.show();

                //gestico ordini
                modeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        switch (position){
                            case 0: //scelgo immagine da galleria
                                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                startActivityForResult(i, RESULT_LOAD_IMAGE); //TODO impostare limite di grandezza file (1 mb? 500 kb ok)
                                break;
                            case 1: //uso fotocamera
                                Toast.makeText(EditMediaActivity.this,"todo camera",Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                startActivityForResult(intent, REQUEST_CAMERA);
                                break;
                            case 2: //cancello allegato
                                note.setImgBytes(new byte[0]);
                                imageView.setImageResource(android.R.color.transparent);
                                emptyMessage.setText(R.string.errorEmptyMediaImage);
                                modified = true;
                                break;
                        }
                        dialog.dismiss();
                    }
                });
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && data != null)
        {
            if(requestCode == REQUEST_CAMERA) //fatto foto? creo file e metto in data
            {
                Bitmap newFile = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                newFile.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);
                File destination = new File(Environment.getExternalStorageDirectory(), "321DO" + note.getTitle() + note.getId() + ".jpg");
                FileOutputStream fileOutputStream;
                try {
                    destination.createNewFile();
                    fileOutputStream = new FileOutputStream(destination);
                    fileOutputStream.write(outputStream.toByteArray());
                    fileOutputStream.close();
                    requestCode = RESULT_LOAD_IMAGE;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if(requestCode == RESULT_LOAD_IMAGE) //prelievo immagine da data (è indifferente se è stata presa dalla galleria o dalla fotocamera)
            {
                Uri selectedImage = data.getData();
                String[] filePathColumn = { MediaStore.Images.Media.DATA };

                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();

                Bitmap fileDecoded = BitmapFactory.decodeFile(picturePath);
                fileDecoded = resizeImage(fileDecoded,0.5f);

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                fileDecoded.compress(Bitmap.CompressFormat.JPEG, 50, outputStream); //sopporta 70 senza problemi (sul mio dispositivo)

                note.setImgBytes(outputStream.toByteArray());
                imageView.setImageBitmap(fileDecoded);

                emptyMessage.setText("");
                modified = true;
            }
        }
    }
    @Override
    protected void onPause() {
        if(modified)
            noteDBAdapter.updateNote(note);
        super.onPause();
    }
    public static Bitmap resizeImage(Bitmap srcBitmap, float percentage) {
        int srcWidth = srcBitmap.getWidth();
        int srcHeight = srcBitmap.getHeight();
        int dstWidth = (int)(srcWidth*percentage);
        int dstHeight = (int)(srcHeight*percentage);
        return Bitmap.createScaledBitmap(srcBitmap, dstWidth, dstHeight, true);
    }
}
