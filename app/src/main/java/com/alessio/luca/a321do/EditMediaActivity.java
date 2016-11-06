package com.alessio.luca.a321do;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Luca on 25/10/2016.
 */

public class EditMediaActivity extends Activity {
    public static final int RESULT_LOAD_IMAGE = 1;
    public static final int REQUEST_CAMERA = 2;
    private Note note;
    private NoteDBAdapter noteDBAdapter;
    private ImageView imageView;
    private static boolean modified = false; //used to avoid heavy and useless rewriting operations in case the image is not modified
    private TextView emptyMessage;
    private String eventualAudioPath = null;
    private MediaRecorder mediaRecorder = null;
    private MediaPlayer mediaPlayer = null;
    boolean startRecording = true, startPlaying = true;
    private Uri picUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        note = (Note) getIntent().getExtras().get(Utilities.EDIT_NOTE_PAYLOAD_CODE);
        noteDBAdapter = new NoteDBAdapter(this);
        modified = false;

        eventualAudioPath = Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/321do" + note.getTitle() + note.getId() + ".3gp";

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
                                startActivityForResult(i, RESULT_LOAD_IMAGE);
                                break;
                            case 1: //uso fotocamera
                                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                startActivityForResult(intent, REQUEST_CAMERA);
                                break;
                            case 2: //cancello allegato
                                AlertDialog.Builder builder = new AlertDialog.Builder(EditMediaActivity.this);
                                builder.setTitle(R.string.deleteImageMessage);
                                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        note.setImgBytes(new byte[0]);
                                        imageView.setImageResource(android.R.color.transparent);
                                        emptyMessage.setText(R.string.errorEmptyMediaImage);
                                        modified = true;
                                        Toast.makeText(EditMediaActivity.this, R.string.imageDeleteMessage,Toast.LENGTH_SHORT).show();
                                        System.gc();
                                    }
                                });
                                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                    }
                                });
                                AlertDialog dialog = builder.create();
                                dialog.show();

                                break;
                        }
                        dialog.dismiss();
                    }
                });
            }
        });

        final Button recordButton = (Button) findViewById(R.id.buttonRecord);
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

        final ImageButton playButton = (ImageButton) findViewById(R.id.buttonPlay);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPlay(startPlaying);
                if (startPlaying) {
                    playButton.setImageResource(R.mipmap.stop);
                } else {
                    playButton.setImageResource(R.mipmap.play);
                }
                startPlaying = !startPlaying;
            }
        });

        final ImageButton deleteAudioButton = (ImageButton) findViewById(R.id.buttonDeleteAudio);
        deleteAudioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(EditMediaActivity.this);
                builder.setTitle(R.string.deleteAudioMessage);
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        modified = true;
                        File toDelete = new File(note.getAudioPath());
                        toDelete.delete();
                        note.setAudioPath("");
                        playButton.setVisibility(View.GONE);
                        deleteAudioButton.setVisibility(View.GONE);
                        recordButton.setText(R.string.recordMessageNoAudio);
                        Toast.makeText(EditMediaActivity.this, R.string.audioDeleteMessage,Toast.LENGTH_SHORT).show();
                        System.gc();
                    }
                });
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        if(!note.getAudioPath().isEmpty() && note.getAudioPath()!=null)
        {
            playButton.setVisibility(View.VISIBLE);
            deleteAudioButton.setVisibility(View.VISIBLE);
        }
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("picUri", picUri);
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        picUri= savedInstanceState.getParcelable("picUri");

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
                } catch (IOException e) {
                    e.printStackTrace();
                }
                newFile = Utilities.resizeImage(newFile, 3f);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                newFile.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream); //sopporta 70 senza problemi (sul mio dispositivo)

                note.setImgBytes(byteArrayOutputStream.toByteArray());
                imageView.setImageBitmap(newFile);

                emptyMessage.setText("");
                modified = true;
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
        final ImageButton playButton = (ImageButton) findViewById(R.id.buttonPlay);
        final ImageButton deleteAudioButton = (ImageButton) findViewById(R.id.buttonDeleteAudio);
        modified = true;
        if (start)
        {
            Toast.makeText(EditMediaActivity.this, R.string.recordingAudioStartMessage,Toast.LENGTH_SHORT).show();
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setOutputFile(eventualAudioPath);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            playButton.setEnabled(false);
            try
            {
                mediaRecorder.prepare();
            }
            catch (IOException e)
            {
                Log.e("321error", "prepare() failed");
            }
            mediaRecorder.start();
        }
        else
        {
            mediaRecorder.stop();
            mediaRecorder.release();
            playButton.setEnabled(true);
            mediaRecorder = null;
            note.setAudioPath(eventualAudioPath);
            playButton.setVisibility(View.VISIBLE);
            deleteAudioButton.setVisibility(View.VISIBLE);
            Toast.makeText(EditMediaActivity.this,getString(R.string.recordingAudioStopMessage)+eventualAudioPath,Toast.LENGTH_SHORT).show();
        }
    }
    private void onPlay(boolean start) {
        final Button recordButton = (Button) findViewById(R.id.buttonRecord);
        final ImageButton playButton = (ImageButton) findViewById(R.id.buttonPlay);
        if (start)
        {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    playButton.setImageResource(R.mipmap.play);
                    recordButton.setEnabled(true);
                }
            });
            try
            {
                recordButton.setEnabled(false);
                mediaPlayer.setDataSource(note.getAudioPath());
                mediaPlayer.prepare();
                mediaPlayer.start();
            }
            catch (IOException e)
            {
                Log.e("321error", "prepare() failed");
            }
        }
        else
        {
            mediaPlayer.release();
            recordButton.setEnabled(true);
            mediaPlayer = null;
        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
    }
}
