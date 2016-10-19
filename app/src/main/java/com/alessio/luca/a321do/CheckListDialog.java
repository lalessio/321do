package com.alessio.luca.a321do;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Created by Luca on 19/10/2016.
 */

public class CheckListDialog extends Dialog {
    private Context context;
    private Note note;
    private EditText editTextCheckList;
    private Button buttonAddCheckListItem, buttonRemoveCheckListItem, buttonConfirm;
    private ListView listViewCheckList;


    public CheckListDialog(Context context, Note note) {
        super(context);
        this.context = context;
        this.note = note;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.checkListTitle);
        setContentView(R.layout.dialog_checklist);

        editTextCheckList = (EditText) findViewById(R.id.editTextCheckList);
        buttonAddCheckListItem = (Button) findViewById(R.id.buttonCheckListAdd);
//        buttonRemoveCheckListItem = (Button) findViewById(R.id.buttonCheckListDelete);
        buttonConfirm = (Button) findViewById(R.id.button_confirm);
        listViewCheckList = (ListView) findViewById(R.id.checklist_list_view);
        update();

        buttonAddCheckListItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editTextCheckList.getText().toString().length()>=1)
                {
                    note.addToCheckList(editTextCheckList.getText().toString());
                    update();
                }
                else
                    Toast.makeText(context,R.string.errorEmptyField,Toast.LENGTH_SHORT).show();
            }
        });

//        buttonRemoveCheckListItem.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //TODO
//            }
//        });

        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    private void update(){
        ArrayAdapter<String> checkListAdapter = new ArrayAdapter<String>(context,android.R.layout.simple_list_item_1,note.getCheckList());
        listViewCheckList.setAdapter(checkListAdapter);
    }
}
