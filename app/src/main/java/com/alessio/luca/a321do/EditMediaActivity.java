package com.alessio.luca.a321do;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

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
    private static boolean modified; //used to avoid heavy and useless rewriting operations in case the image is not modified
    private TextView emptyMessage;

    private static String mFileName = null;

    private Button recordButton = null;
    private MediaRecorder mediaRecorder = null;

    private Button playButton = null;
    private MediaPlayer mediaPlayer = null;

    boolean startRecording = true, startPlaying = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        note = (Note) getIntent().getExtras().get(Utilities.EDIT_NOTE_PAYLOAD_CODE);
        noteDBAdapter = new NoteDBAdapter(this);
        modified = false;

        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/321do" + note.getTitle() + note.getId() + ".3gp";

        setTitle(R.string.editNoteMediaTitle);
        setContentView(R.layout.media_layout);

        imageView = (ImageView) findViewById(R.id.imageView);
        final Button chooseImageButton = (Button) findViewById(R.id.buttonChooseImage);
        emptyMessage = (TextView) findViewById(R.id.textViewMediaImage);

        if (note.getImgBytes() == null)
            emptyMessage.setText(R.string.errorEmptyMediaImage);
        else
            imageView.setImageBitmap(BitmapFactory.decodeByteArray(note.getImgBytes(), 0, note.getImgBytes().length));

        chooseImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(EditMediaActivity.this);
                ListView modeListView = new ListView(EditMediaActivity.this);
                String[] modes = new String[]{getString(R.string.optionMediaChooseFromGallery), getString(R.string.optionMediaTakePhoto), getString(R.string.optionMediaDeleteAttachment)};
                ArrayAdapter<String> modeAdapter = new ArrayAdapter<>(EditMediaActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1, modes);
                modeListView.setAdapter(modeAdapter);
                builder.setView(modeListView);
                final Dialog dialog = builder.create();
                dialog.show();

                //gestico ordini
                modeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        switch (position) {
                            case 0: //scelgo immagine da galleria
                                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                startActivityForResult(i, RESULT_LOAD_IMAGE); //TODO impostare limite di grandezza file (1 mb? 500 kb ok)
                                break;
                            case 1: //uso fotocamera
                                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                startActivityForResult(intent, REQUEST_CAMERA);
                                break;
                            case 2: //cancello allegato
                                note.setImgBytes(new byte[0]);
                                imageView.setImageResource(android.R.color.transparent);
                                chooseImageButton.setGravity(Gravity.CENTER); //doesn't work
                                emptyMessage.setText(R.string.errorEmptyMediaImage);
                                modified = true;
                                System.gc();
                                break;
                        }
                        dialog.dismiss();
                    }
                });
            }
        });

        recordButton = (Button) findViewById(R.id.buttonRecord);
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRecord(startRecording);
                if (startRecording) {
                    recordButton.setText(R.string.recordButtonStop);
                } else {
                    recordButton.setText(R.string.recordButtonStart);
                }
                startRecording = !startRecording;
            }
        });

        playButton = (Button) findViewById(R.id.buttonPlay);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPlay(startPlaying);
                if (startPlaying) {
                    playButton.setText(R.string.playButtonStop);
                } else {
                    playButton.setText(R.string.playButtonStart);
                }
                startPlaying = !startPlaying;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == REQUEST_CAMERA) //fatto foto? creo file e metto in data
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

            if (requestCode == RESULT_LOAD_IMAGE) //prelievo immagine da data (è indifferente se è stata presa dalla galleria o dalla fotocamera)
            {
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();

                Bitmap fileDecoded = BitmapFactory.decodeFile(picturePath);
                fileDecoded = Utilities.resizeImage(fileDecoded, 0.5f);

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
        if (modified) {
            noteDBAdapter.updateNote(note);
            modified = false;
        }
        super.onPause();
        if (mediaRecorder != null) {
            mediaRecorder.release();
            mediaRecorder = null;
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void onRecord(boolean start) {
        if (start) {
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setOutputFile(mFileName);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            try {
                mediaRecorder.prepare();
            } catch (IOException e) {
                Log.e("321error", "prepare() failed");
            }
            mediaRecorder.start();
        } else {
//            byte[] soundBytes;
//            try {
//                InputStream inputStream = getContentResolver().openInputStream(Uri.fromFile(new File(mFileName)));
//                soundBytes = new byte[inputStream.available()];
//                soundBytes = Utilities.toByteArray(inputStream);
//                note.setAudioBytes(soundBytes);
//            }catch(Exception e){
//                e.printStackTrace();
//            }
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    private void onPlay(boolean start) {
        if (start) {
            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(mFileName);
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (IOException e) {
                Log.e("321error", "prepare() failed");
            }
//            File path = new File(mFileName);
//            FileOutputStream fos = null;
//            Log.d("321media","sono dentro onplay\naudio ne ho?"+(note.getAudioBytes().length!=0)+"\nil percorso è "+mFileName);
//            try {
//                fos = new FileOutputStream(path);
//                fos.write(note.getAudioBytes());
//                fos.close();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            MediaPlayer mediaPlayer = new MediaPlayer();
//
//            try {
//                mediaPlayer.setDataSource(mFileName);
//                mediaPlayer.prepare();
//                mediaPlayer.start();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        } else {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
