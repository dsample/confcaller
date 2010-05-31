package uk.me.sample.android.confcaller;

import java.net.URLEncoder;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.PhoneNumberUtils;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class ConfCaller extends ListActivity {
	
	private ConfDbAdapter mDbHelper;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mDbHelper = new ConfDbAdapter(this);
        mDbHelper.open();
        
        //setListAdapter(new ArrayAdapter<String>(this, R.layout.contactslistitem, COUNTRIES));
        setListAdapter(getList());
        ListView lv = getListView();
        lv.setTextFilterEnabled(true);
/*
        TextView emptyView = new TextView(this);
        emptyView.setText(R.string.emptyText);
        emptyView.setTextColor(R.color.emptyTextColour);
        emptyView.setTextSize(10);
        lv.setEmptyView(emptyView);
*/
        registerForContextMenu(lv);
                
        lv.setOnItemClickListener(new OnItemClickListener() {
        	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        		// When clicked, show a toast with the TextView text
        		
        		Cursor confItem = mDbHelper.fetchConf(id);
        		
        		String confNumber = confItem.getString(confItem.getColumnIndexOrThrow(ConfDbAdapter.KEY_NUMBER));
        		String confPin = confItem.getString(confItem.getColumnIndexOrThrow(ConfDbAdapter.KEY_PIN));
        		
        		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        		
        		String accessNumber = prefs.getString("localAccessNumber", "");
        		String separator = prefs.getString("separator", "");

        		confItem.close();
        		mDbHelper.close();

        		if (accessNumber.equals("")) {
        			Toast.makeText(getApplicationContext(), "No access number defined", Toast.LENGTH_LONG).show();
        			return;
        		} else {
	        		String telUri = "tel:" + accessNumber + PhoneNumberUtils.PAUSE + confNumber + URLEncoder.encode(separator) + PhoneNumberUtils.PAUSE + confPin + URLEncoder.encode(separator);
	        		Toast.makeText(getApplicationContext(), "Calling " + ((TextView) view).getText(), Toast.LENGTH_SHORT).show();
	        		Intent call = new Intent(android.content.Intent.ACTION_CALL, Uri.parse(telUri));
	        		//call.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        		startActivity(call);
        		}
            }
        });
    }
    
    @Override
	protected void onPause() {
		// TODO Auto-generated method stub
		mDbHelper.close();
		super.onPause();
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		mDbHelper.open();
		setListAdapter(getList());
		super.onResume();
	}
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		mDbHelper.close();
		super.onStop();
	}

    private SimpleCursorAdapter getList() {
        // Get all of the notes from the database and create the item list
        Cursor c = mDbHelper.fetchAllConfs();
        startManagingCursor(c);

        String[] from = new String[] { ConfDbAdapter.KEY_NAME };
        int[] to = new int[] { R.id.item_name };
        
        // Now create an array adapter and set it to display using our row
        SimpleCursorAdapter confs =
            new SimpleCursorAdapter(this, R.layout.contactslistitem, c, from, to);
    	return confs;
    }
    
    /* (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent i;
		
		switch (item.getItemId()) {
			case R.id.newConf:
				i = new Intent(this, NewConf.class);
				startActivityForResult(i, NewConf.ACTIVITY_CREATE);
				return true;
/*			case R.id.adhoc:
				return false;*/
			case R.id.settingsOption:
				i = new Intent(this, Preferences.class);
				startActivity(i);
				return true;
			default:
				return false;
		}
	}

	static final int DELETE_ID = Menu.FIRST;
	static final int EDIT_ID = Menu.FIRST + 1;
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu, android.view.View, android.view.ContextMenu.ContextMenuInfo)
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		//menu.add(R.string.menu_delete);
		menu.add(0, DELETE_ID, 0, R.string.menu_delete);
		menu.add(0, EDIT_ID, 1, R.string.menu_edit);
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// TODO Auto-generated method stub

        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

		Intent i;
		switch (item.getItemId()) {
			case DELETE_ID:
		        mDbHelper.deleteConf(info.id);
		        setListAdapter(getList());
				return true;
			case EDIT_ID:
				i = new Intent(this, NewConf.class);
				i.putExtra(ConfDbAdapter.KEY_ROWID, info.id);
				startActivityForResult(i, NewConf.ACTIVITY_EDIT);
				return true;
			default:
				return false;
		}
	}
}