package uk.me.sample.android.confcaller;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class NewConf extends Activity {
	
	public static final int ACTIVITY_EDIT = 1;
	public static final int ACTIVITY_CREATE = 2;
	
	private EditText mNameText;
	private EditText mNumberText;
	private EditText mPinText;
	private Long mRowId;
	private ConfDbAdapter mDbHelper;

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		mDbHelper = new ConfDbAdapter(this);
		mDbHelper.open();
		
		setContentView(R.layout.newconf);
		
		mNameText = (EditText) findViewById(R.id.input_name);
		mNumberText = (EditText) findViewById(R.id.input_number);
		mPinText = (EditText) findViewById(R.id.input_pin);
		
		Button saveButton = (Button) findViewById(R.id.save);
		
		mRowId = (savedInstanceState == null) ? null :
            (Long) savedInstanceState.getSerializable(ConfDbAdapter.KEY_ROWID);
		if (mRowId == null) {
			Bundle extras = getIntent().getExtras();
			if (extras != null) {
				mRowId = extras.getLong(ConfDbAdapter.KEY_ROWID);
				loadData();
			}
		}
		
		saveButton.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				saveState();
				mDbHelper.close();
				finish();
			}
		});
	}
	
	protected void loadData() {
		if (mRowId != null) {
			Cursor conf = mDbHelper.fetchConf(mRowId);
			startManagingCursor(conf);
			mNameText.setText(conf.getString(conf.getColumnIndexOrThrow(ConfDbAdapter.KEY_NAME)));
			mNumberText.setText(conf.getString(conf.getColumnIndexOrThrow(ConfDbAdapter.KEY_NUMBER)));
			mPinText.setText(conf.getString(conf.getColumnIndexOrThrow(ConfDbAdapter.KEY_PIN)));
			conf.close();
		}
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		//saveState();
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		//saveState();
		mDbHelper.close();
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mDbHelper.open();
		//loadData();
	}
	
	private void saveState() {
		String name = mNameText.getText().toString();
		String number = mNumberText.getText().toString();
		String pin = mPinText.getText().toString();
		
		if (mRowId == null) {
			// New record
			long id = mDbHelper.createConf(name, number, pin);
			if (id > 0) {
				mRowId = id;
			}
		} else {
			// Update record
			mDbHelper.updateConf(mRowId, name, number, pin);
		}
	}

}
