package com.alessio.luca.a321do;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.speech.RecognizerIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
//TODO salvataggio audio
//TODO abbellimento layout
//TODO temi?
//TODO faq
public class NoteActivity extends AppCompatActivity {
    private ListView listView;
    private NoteDBAdapter noteDBAdapter;
    private static ArrayList<Note> retrievedNotes;
    private SortingOrder currentOrder;
    private DrawerLayout drawerLayout;
    private FloatingActionButton fabText, fabAudio;
    public static final int REQ_CODE_SPEECH_INPUT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        //here i had to use an actionbardrawertoggle just to have the hamburger animated icon...
        //i'm sure there is a smarter way to achieve that
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.app_name, R.string.app_name);
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        drawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        noteDBAdapter = new NoteDBAdapter(this);
        listView = (ListView)findViewById(R.id.note_list_view);
        retrievedNotes = new ArrayList<>();
        currentOrder = new SortingOrder();
        updateListView(currentOrder);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int masterListPosition, long id) {
                //creo finestra
                final Note selectedNote = retrievedNotes.get(masterListPosition);
                fabText.hide();
                fabAudio.hide();
                AlertDialog.Builder builder = new AlertDialog.Builder(NoteActivity.this);
                ListView modeListView = new ListView(NoteActivity.this);
                String[] modes = new String[] { getString(R.string.noteOptionEdit), getString(R.string.noteOptionDelete), getString(R.string.noteOptionClone), getString(R.string.noteOptionTick) };
                ArrayAdapter<String> modeAdapter = new ArrayAdapter<>(NoteActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1, modes);
                modeListView.setAdapter(modeAdapter);
                builder.setView(modeListView);
                final Dialog dialog = builder.create();
                dialog.show();

                modeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        switch (position){
                            case 0:
                                Bundle bundle = new Bundle();
                                bundle.putSerializable(Utilities.EDIT_NOTE_PAYLOAD_CODE,noteDBAdapter.retrieveNoteById(selectedNote.getId()));
                                Intent intent = new Intent(NoteActivity.this, EditNoteActivity.class);
                                intent.putExtras(bundle);
                                startActivity(intent);
                                overridePendingTransition(0,0); //TODO decidere se tenere animazioni
                                break;
                            case 1:
                                final Note deletedNote = noteDBAdapter.deleteNote(selectedNote);
                                Snackbar snackbarDelete = Snackbar
                                        .make(findViewById(android.R.id.content), R.string.snackbarDeleteMessage, Snackbar.LENGTH_LONG)
                                        .setActionTextColor(Color.YELLOW)
                                        .setAction(R.string.snackbarUndo, new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                Note restoredNote =  noteDBAdapter.createNote(deletedNote.getTitle());
                                                deletedNote.setId(restoredNote.getId());
                                                noteDBAdapter.updateNote(deletedNote);
                                                updateListView(currentOrder);
                                            }
                                        });
                                snackbarDelete.show();
                                break;
                            case 2:
                                final Note clonedNote = noteDBAdapter.cloneNote(selectedNote);
                                Snackbar snackbarClone = Snackbar
                                        .make(findViewById(android.R.id.content), R.string.snackbarCloneMessage, Snackbar.LENGTH_LONG)
                                        .setActionTextColor(Color.YELLOW)
                                        .setAction(R.string.snackbarUndo, new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                noteDBAdapter.deleteNote(clonedNote);
                                                updateListView(currentOrder);
                                            }
                                        });
                                snackbarClone.show();
                                break;
                            case 3:
                                noteDBAdapter.tickNote(selectedNote);
                                break;
                        }
                        updateListView(currentOrder);
                        dialog.dismiss();
                        fabText.show();
                        fabAudio.show();
                    }
                });

                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        fabText.show();
                        fabAudio.show();
                    }
                });
            }
        });

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if(scrollState == SCROLL_STATE_TOUCH_SCROLL)
                {
                    fabText.hide();
                    fabAudio.hide();
                }
                else
                {
                    fabText.show();
                    fabAudio.show();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });

        fabText = (FloatingActionButton) findViewById(R.id.fabText);
        fabText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(NoteActivity.this,NewNoteActivity.class);
                startActivity(intent);
                overridePendingTransition(0,0);
            }
        });

        fabAudio = (FloatingActionButton) findViewById(R.id.fabAudio);
        fabAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newAudioNote();
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
                                    getString(R.string.drawerOptionAttachment),
                                    getString(R.string.drawerOptionAll) };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, drawerContent);
        drawerList.setAdapter(adapter);
        //TODO order by tag
        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        currentOrder = new SortingOrder(currentOrder.getOrder(),SortingOrder.Filter.TODAY,currentOrder.getSearchParameter());
                    case 1:
                        currentOrder = new SortingOrder(currentOrder.getOrder(),SortingOrder.Filter.TOMORROW,currentOrder.getSearchParameter());
                        break;
                    case 2:
                        currentOrder = new SortingOrder(currentOrder.getOrder(),SortingOrder.Filter.NEXT7DAYS,currentOrder.getSearchParameter());
                        break;
                    case 3:
                        currentOrder = new SortingOrder(currentOrder.getOrder(),SortingOrder.Filter.ONLY_PLANNED,currentOrder.getSearchParameter());
                        break;
                    case 4:
                        currentOrder = new SortingOrder(currentOrder.getOrder(),SortingOrder.Filter.ONLY_EXPIRED,currentOrder.getSearchParameter());
                        break;
                    case 5:
                        currentOrder = new SortingOrder(currentOrder.getOrder(),SortingOrder.Filter.ONLY_COMPLETED,currentOrder.getSearchParameter());
                        break;
                    case 6:
                        currentOrder = new SortingOrder(currentOrder.getOrder(), SortingOrder.Filter.WITH_ATTACHMENT,currentOrder.getSearchParameter());
                        break;
                    case 7:
                        currentOrder = new SortingOrder(currentOrder.getOrder(),SortingOrder.Filter.NONE);
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
        if(currentOrder.getOrder()!= SortingOrder.Order.NONE || currentOrder.getFilter()!= SortingOrder.Filter.NONE)
        {
            currentOrder = new SortingOrder(SortingOrder.Order.NONE, SortingOrder.Filter.NONE);
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
                currentOrder = new SortingOrder(currentOrder.getOrder(),currentOrder.getFilter(),query);
                updateListView(currentOrder);
                fabText.show();
                fabAudio.show();
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                fabText.hide();
                fabAudio.hide();
                return false;
            }
        });

        //buggato non funziona!
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                currentOrder = new SortingOrder(SortingOrder.Order.NONE, SortingOrder.Filter.NONE);
                updateListView(currentOrder);
                return false;
            }
        });

        //per gestire il ritorno alla vista normale dopo che premo indietro
        MenuItemCompat.setOnActionExpandListener(menu.findItem(R.id.action_search), new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                currentOrder = new SortingOrder(SortingOrder.Order.NONE,currentOrder.getFilter());
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
                Intent intent = new Intent(NoteActivity.this,NewNoteActivity.class);
                startActivity(intent);
                overridePendingTransition(0,0);
                return true;
            case R.id.action_sort:
                showSortMenu();
                return true;
            case R.id.action_clear_completed:
                AlertDialog.Builder builder = new AlertDialog.Builder(NoteActivity.this);
                builder.setTitle(R.string.deleteAllCompletedMessage);
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        for(int i=0; i<retrievedNotes.size(); i++)
                            if(retrievedNotes.get(i).isDone())
                                noteDBAdapter.deleteNote(retrievedNotes.get(i));
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        updateListView(currentOrder);
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            case R.id.action_settings:
                Intent intent1 = new Intent(NoteActivity.this,SettingsActivity.class);
                startActivity(intent1);
                overridePendingTransition(0,0);
                return true;
            case R.id.action_exit:
                finish();
                return true;
            default:
                drawerLayout.openDrawer(GravityCompat.START);
                return super.onOptionsItemSelected(item);

        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && resultCode == RESULT_OK)
        {
            if (requestCode == REQ_CODE_SPEECH_INPUT)
            {
                noteDBAdapter.createNote(data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0));
                updateListView(currentOrder);
            }
        }
        fabText.show();
        fabAudio.show();
    }
    //funzioni di servizio
    private void newAudioNote() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.audioRecordMessage));
        try {
            fabText.hide();
            fabAudio.hide();
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(), getString(R.string.errorAudioRecord), Toast.LENGTH_LONG).show();
        }
    }
    private void showSortMenu() {
        fabText.hide();
        fabAudio.hide();
        AlertDialog.Builder builder = new AlertDialog.Builder(NoteActivity.this);
        ListView modeListView = new ListView(NoteActivity.this);
        String[] modes = new String[] { getString(R.string.sortOptionCreation),
                getString(R.string.sortOptionDueDate),
                getString(R.string.sortOptionImportance),
                getString(R.string.sortOptionTag)};
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
                        currentOrder = new SortingOrder(SortingOrder.Order.NONE,currentOrder.getFilter(),currentOrder.getSearchParameter());
                        break;
                    case 1:
                        currentOrder = new SortingOrder(SortingOrder.Order.DUEDATE,currentOrder.getFilter(),currentOrder.getSearchParameter());
                        break;
                    case 2:
                        currentOrder = new SortingOrder(SortingOrder.Order.IMPORTANCE,currentOrder.getFilter(),currentOrder.getSearchParameter());
                        break;
                    case 3:
                        currentOrder = new SortingOrder(SortingOrder.Order.CATEGORY,currentOrder.getFilter(),currentOrder.getSearchParameter());
                        break;
                }
                updateListView(currentOrder);
                dialog.dismiss();
                System.gc();
            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                fabText.show();
                fabAudio.show();
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
            ArrayList<String> nCheckList = new ArrayList<>(Utilities.stringToCheckList(cursor.getString(cursor.getColumnIndex(NoteDBAdapter.COL_CHECKLIST))));
            temp.setCheckList(nCheckList);
            if(cursor.getInt(cursor.getColumnIndex(NoteDBAdapter.COL_DONE))==0)
                temp.setDone(false);
            else
                temp.setDone(true);
            if(cursor.getInt(cursor.getColumnIndex(NoteDBAdapter.COL_ALARM))==0)
                temp.setAlarm(false);
            else
                temp.setAlarm(true);
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
            String currentTag = retrievedNotes.get(i).getTag().replaceAll("\\s+","");
            if(currentTag!="" && !tags.contains(currentTag))
                tags.add(currentTag);
        }
        return tags.toArray(new String[tags.size()]);
    }
}