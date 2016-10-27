package com.alessio.luca.a321do;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class NoteActivity extends AppCompatActivity {
    private ListView listView;
    private NoteDBAdapter noteDBAdapter;
    private static ArrayList<Note> retrievedNotes;
    private SortingOrder currentOrder;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_layout);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        noteDBAdapter = new NoteDBAdapter(this);
        listView = (ListView)findViewById(R.id.note_list_view);
        retrievedNotes = new ArrayList<>();
        currentOrder = new SortingOrder();
        updateListView(currentOrder);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int masterListPosition, long id) {
                //creo finestra
                AlertDialog.Builder builder = new AlertDialog.Builder(NoteActivity.this);
                ListView modeListView = new ListView(NoteActivity.this);
                String[] modes = new String[] { getString(R.string.noteOptionEdit), getString(R.string.noteOptionDelete), getString(R.string.noteOptionClone), getString(R.string.noteOptionTick) };
                ArrayAdapter<String> modeAdapter = new ArrayAdapter<>(NoteActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1, modes);
                modeListView.setAdapter(modeAdapter);
                builder.setView(modeListView);
                final Dialog dialog = builder.create();
                dialog.show();

                //gestico ordini
                modeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        switch (position){
                            case 0:
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("EditNotePayload",noteDBAdapter.retrieveNoteById(retrievedNotes.get(masterListPosition).getId()));
                                //bundle.putSerializable("TagsPayload",getExistingTags());
                                Intent intent = new Intent(NoteActivity.this, EditNoteActivity.class);
                                intent.putExtras(bundle);
                                startActivity(intent);
                                overridePendingTransition(0,0);
                                break;
                            case 1:
                                noteDBAdapter.deleteNote(retrievedNotes.get(masterListPosition));
                                break;
                            case 2:
                                noteDBAdapter.cloneNote(retrievedNotes.get(masterListPosition));
                                break;
                            case 3:
                                noteDBAdapter.tickNote(retrievedNotes.get(masterListPosition));
                                break;
                        }
                        updateListView(currentOrder);
                        dialog.dismiss();
                    }
                });
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NewNoteDialog dialog = new NewNoteDialog(NoteActivity.this);
                dialog.show();
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        updateListView(currentOrder);
                    }
                });
            }
        });

        ListView drawerList = (ListView) findViewById(R.id.navList);
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        String[] drawerContent = { getString(R.string.drawerOptionToday),
                                    getString(R.string.drawerOptionTomorrow),
                                    getString(R.string.drawerOptionNext7Days),
                                    getString(R.string.drawerOptionPlanned),
                                    getString(R.string.drawerOptionExpired),
                                    getString(R.string.drawerOptionCompleted),
                                    getString(R.string.drawerOptionAll) };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, drawerContent);
        drawerList.setAdapter(adapter);
        //TODO order by tag
        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        currentOrder = new SortingOrder(SortingOrder.Order.TODAY,currentOrder.getSearchParameter());
                    case 1:
                        currentOrder = new SortingOrder(SortingOrder.Order.TOMORROW,currentOrder.getSearchParameter());
                        break;
                    case 2:
                        currentOrder = new SortingOrder(SortingOrder.Order.NEXT7DAYS,currentOrder.getSearchParameter());
                        break;
                    case 3:
                        currentOrder = new SortingOrder(SortingOrder.Order.ONLY_PLANNED,currentOrder.getSearchParameter());
                        break;
                    case 4:
                        currentOrder = new SortingOrder(SortingOrder.Order.ONLY_EXPIRED,currentOrder.getSearchParameter());
                        break;
                    case 5:
                        currentOrder = new SortingOrder(SortingOrder.Order.ONLY_COMPLETED,currentOrder.getSearchParameter());
                        break;
                    case 6:
                        currentOrder = new SortingOrder(SortingOrder.Order.NONE);
                        break;
                    default:
                        Toast.makeText(NoteActivity.this, "ERROR", Toast.LENGTH_SHORT).show();
                        break;
                }

                drawerLayout.closeDrawers();
                updateListView(currentOrder);
            }
        });

        //TODO cancellazione multipla
//        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
//        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
//            @Override
//            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
//            }
//
//            @Override
//            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
//                return false;
//            }
//
//            @Override
//            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
//                return false;
//            }
//
//            @Override
//            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
//                switch (item.getItemId()) {
//                    case R.id.multipleDelete:
//                        for (int nC = new NoteListAdapter(NoteActivity.this,R.layout.note_row,retrievedNotes.toArray(new Note[retrievedNotes.size()]),currentOrder).getCount() - 1; nC >= 0; nC--) {
//                            if (listView.isItemChecked(nC)) {
//                                noteDBAdapter.deleteNote(retrievedNotes.get(nC));
//                            }
//                        }
//                        mode.finish();
//                        updateListView(currentOrder);
//                        return true;
//                }
//                return false;
//            }
//
//            @Override
//            public void onDestroyActionMode(ActionMode mode) {
//
//            }
//        });

    }
    @Override
    protected void onResume() {
        super.onResume();
        updateListView(currentOrder);
    }
    @Override
    public void onBackPressed() {
        if(currentOrder.getOrder()!= SortingOrder.Order.NONE)
        {
            currentOrder = new SortingOrder(SortingOrder.Order.NONE);
            updateListView(currentOrder);
        }
        else
            super.onBackPressed();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_content, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentOrder = new SortingOrder(currentOrder.getOrder(),query);
                updateListView(currentOrder);
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        //buggato non funziona!
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                currentOrder = new SortingOrder(SortingOrder.Order.NONE);
                updateListView(currentOrder);
                return false;
            }
        });

        //per gestire il ritorno alla vista normale dopo che premo indietro
        MenuItemCompat.setOnActionExpandListener(menu.findItem(R.id.action_search), new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                currentOrder = new SortingOrder(SortingOrder.Order.NONE);
                updateListView(currentOrder);
                return true;
            }
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }
        });

        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                // gestito in onCreateOptionsMenu()
                return true;
            case R.id.action_new:
                NewNoteDialog dialog = new NewNoteDialog(this);
                dialog.show();
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        updateListView(currentOrder);
                    }
                });
                return true;
            case R.id.action_sort:
                showSortMenu();
                return true;
            case R.id.action_settings:
                Toast.makeText(NoteActivity.this, "settings TODO", Toast.LENGTH_SHORT).show(); //TODO
                return true;
            case R.id.action_exit:
                finish();
                return true;
            default:
                Toast.makeText(this,"default",Toast.LENGTH_SHORT).show();
                return super.onOptionsItemSelected(item);

        }
    }
    //funzioni di servizio
    private void showSortMenu() {
        AlertDialog.Builder builder = new AlertDialog.Builder(NoteActivity.this);
        ListView modeListView = new ListView(NoteActivity.this);
        String[] modes = new String[] { getString(R.string.sortOptionCreation),
                getString(R.string.sortOptionDueDate),
                getString(R.string.sortOptionImportance),
                getString(R.string.sortOptionTag) };
        ArrayAdapter<String> modeAdapter = new ArrayAdapter<>(NoteActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1, modes);
        modeListView.setAdapter(modeAdapter);
        builder.setView(modeListView);
        final Dialog dialog = builder.create();
        dialog.setTitle(R.string.sortOptionTitle);
        dialog.show();
        modeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        currentOrder = new SortingOrder(SortingOrder.Order.NONE,currentOrder.getSearchParameter());
                        break;
                    case 1:
                        currentOrder = new SortingOrder(SortingOrder.Order.DUEDATE,currentOrder.getSearchParameter());
                        break;
                    case 2:
                        currentOrder = new SortingOrder(SortingOrder.Order.IMPORTANCE,currentOrder.getSearchParameter());
                        break;
                    case 3:
                        currentOrder = new SortingOrder(SortingOrder.Order.CATEGORY,currentOrder.getSearchParameter());
                        break;
                }
                updateListView(currentOrder);
                dialog.dismiss();
            }
        });
    }
    private void updateListView(SortingOrder sortBy) {
        Cursor cursor = noteDBAdapter.retrieveAllNotes(sortBy);
        cursor.moveToFirst();
        retrievedNotes.clear();

        while (!cursor.isAfterLast()) //forse il contenuto di questo for può diventare una funzione da qualche altra parte
        {
            Note temp = new Note();
            temp.setId(cursor.getInt(cursor.getColumnIndex(NoteDBAdapter.COL_ID)));
            temp.setTitle(cursor.getString(cursor.getColumnIndex(NoteDBAdapter.COL_TITLE)));
            temp.setDescription(cursor.getString(cursor.getColumnIndex(NoteDBAdapter.COL_DESCRIPTION)));
            temp.setTag(cursor.getString(cursor.getColumnIndex(NoteDBAdapter.COL_TAG)));
            Calendar t = new GregorianCalendar();
            t.setTimeInMillis(Long.valueOf(cursor.getString(cursor.getColumnIndex(NoteDBAdapter.COL_DUEDATE))));
            temp.setDueDate(t);
            temp.setImportance(new Importance(cursor.getString(cursor.getColumnIndex(NoteDBAdapter.COL_IMPORTANCE))));
            temp.setImgBytes(cursor.getBlob(cursor.getColumnIndex(NoteDBAdapter.COL_IMAGE)));
            ArrayList<String> nCheckList = new ArrayList<>(Note.stringToCheckList(cursor.getString(cursor.getColumnIndex(NoteDBAdapter.COL_CHECKLIST))));
            temp.setCheckList(nCheckList);
            if(cursor.getInt(cursor.getColumnIndex(NoteDBAdapter.COL_DONE))==0)
                temp.setDone(false);
            else
                temp.setDone(true);
            retrievedNotes.add(temp);
            cursor.moveToNext();
        }
//TODO deallocazione
        Note[] notes = retrievedNotes.toArray(new Note[retrievedNotes.size()]);
        NoteListAdapter noteListAdapter = new NoteListAdapter(this,R.layout.note_row,notes,sortBy);
        listView.setAdapter(noteListAdapter);
        TextView emptyText = (TextView)  findViewById(R.id.emptyList);
        if(!noteListAdapter.isEmpty())
            emptyText.setText("");
        else
            emptyText.setText(R.string.errorEmptyListView);
    }
    public static String[] getExistingTags(){
        List<String> tags = new ArrayList<>();
        for(int i=0; i<retrievedNotes.size(); i++)
        {
            String currentTag = retrievedNotes.get(i).getTag();
            if(currentTag!="" && !tags.contains(currentTag))
                tags.add(currentTag);
        }
        return tags.toArray(new String[tags.size()]);
    }
}