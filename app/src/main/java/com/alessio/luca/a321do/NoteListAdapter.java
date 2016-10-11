package com.alessio.luca.a321do;

import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.text.SpannableString;
import android.text.style.StrikethroughSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Created by Luca on 11/10/2016.
 */

public class NoteListAdapter extends ArrayAdapter {
    Context context;
    int layoutResourceId;
    Note[] data = null;

    public NoteListAdapter(Context context, int layoutResourceId, Note[] data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
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

            switch (listViewItemType){
                case 1:
                    row=inflater.inflate(R.layout.note_row_done,null);
                    content.setSpan(new StrikethroughSpan(), 0, content.length(), 0);
                    break;
                case 2:
                    row=inflater.inflate(R.layout.note_row_planned,null);
                    break;
                case 3:
                    row=inflater.inflate(R.layout.note_row_expired,null);
                    break;
                default:
                    row=inflater.inflate(R.layout.note_row_planned,null);
                    break;
            }

            holder = new NoteViewHolder();
            holder.textView = (TextView)row.findViewById(R.id.row_text);

            row.setTag(holder);
        }
        else
        {
            holder = (NoteViewHolder)row.getTag();
        }
        holder.textView.setText(content);
        return row;
    }

    static class NoteViewHolder //TODO da rendere più conforme ai principi di buona programmazione (get, set, costruttori...)
    {
        TextView textView;
    }
}
