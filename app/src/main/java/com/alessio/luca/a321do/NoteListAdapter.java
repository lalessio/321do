package com.alessio.luca.a321do;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Luca on 10/10/2016.
 */

public class NoteListAdapter extends ArrayAdapter {
    private ArrayList<Note> notes;

    private class ViewHolder {
        TextView text;
        public ViewHolder(TextView text) {
            this.text = text;
        }
        public TextView getText() {
            return text;
        }
        public void setText(TextView text) {
            this.text = text;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return notes.get(position).isDone()?2:1;
    }

    public NoteListAdapter(Context context, int resource, ArrayList<Note> notes) {
        super(context, resource, notes);
        this.notes = notes;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder = null;
        Note listViewItem = notes.get(position);
        int listViewItemType = getItemViewType(position);


        if (convertView == null) {

            if (listViewItemType == 2) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.note_row_done, null);
            }  else {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.note_row_undone, null);
            }

            TextView textView = (TextView) convertView.findViewById(R.id.row_text);
            viewHolder = new ViewHolder(textView);

            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.getText().setText(listViewItem.getTitle());
        if(listViewItemType==2)
            viewHolder.getText().setPaintFlags(viewHolder.getText().getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        return convertView;
    }
}
