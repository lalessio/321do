package com.alessio.luca.b321do;

import android.app.Activity;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.style.StrikethroughSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Luca on 11/10/2016.
 */

public class NoteListAdapter extends ArrayAdapter {
    private Context context;
    private int layoutResourceId;
    private Note[] data = null;
    private SortingOrder sortingRequested;

    NoteListAdapter(Context context, int layoutResourceId, Note[] data, SortingOrder sortingRequested) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
        this.sortingRequested = sortingRequested;
    }
    @Override
    public int getItemViewType(int position) {
        Note.NoteState state = data[position].getNoteState();
        switch (state){
            case COMPLETED:
                return 1;
            case PLANNED:
                return 2;
            case EXPIRED:
                return 3;
            default:
                return 2;
        }
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        NoteViewHolder holder = null;
        Note note = data[position];
        SpannableString content = new SpannableString(note.getTitle());
        int listViewItemType = getItemViewType(position);

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            row = inflater.inflate(R.layout.note_row,null);
            TextView noteText = (TextView) row.findViewById(R.id.rowText);

            //coloro la nota in base al suo stato
            switch (listViewItemType){
                case 1:
                    content.setSpan(new StrikethroughSpan(), 0, content.length(), 0);
                    noteText.setTextColor(ContextCompat.getColor(context,R.color.green));
                    break;
                case 2:
                    noteText.setTextColor(ContextCompat.getColor(context,R.color.blue));
                    break;
                case 3:
                    noteText.setTextColor(ContextCompat.getColor(context,R.color.red));
                    break;
                default:
                    noteText.setTextColor(ContextCompat.getColor(context,R.color.blue));
                    break;
            }

            //mi occupo di popolare la sottovoce della nota se necessario
            TextView subNoteText = (TextView) row.findViewById(R.id.rowSubText);
            switch (sortingRequested.getOrder()){
                case DUEDATE:
                    subNoteText.setText(note.printTime());
                    break;
                case IMPORTANCE:
                    subNoteText.setText(note.getImportance().toString());
                    break;
                case CATEGORY:
                    subNoteText.setText(note.getTag());
                    break;
                default:
                    subNoteText.setText(note.getDescription().replaceAll("[\\t\\n\\r]"," ")); //tolgo caporiga e inserisco spaziature per riparmiare spazio nella visualizzazione
                    if(note.getImgBytes()!=null || (!note.getAudioPath().isEmpty() && note.getAudioPath()!=null))
                    {
                        ImageView imageView = (ImageView) row.findViewById(R.id.rowAttachmentImage);
                        imageView.setImageResource(R.mipmap.attachment);
                    }
                    break;
            }
            holder = new NoteViewHolder();
            holder.setTextView((TextView) row.findViewById(R.id.rowText));
            row.setTag(holder);
        }
        else
        {
            holder = (NoteViewHolder)row.getTag();
        }
        holder.getTextView().setText(content);
        return row;
    }
    private static class NoteViewHolder
    {
        TextView textView;
        TextView getTextView() {
            return textView;
        }
        void setTextView(TextView textView) {
            this.textView = textView;
        }
    }
}
