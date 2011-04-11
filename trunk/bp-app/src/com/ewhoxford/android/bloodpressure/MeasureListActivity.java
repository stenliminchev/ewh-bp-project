/*
 */

package com.ewhoxford.android.bloodpressure;

import java.io.IOException;
import java.util.ArrayList;

import android.app.ListActivity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.ewhoxford.android.bloodpressure.database.BloodPressureMeasureTable.BPMeasure;
import com.ewhoxford.android.bloodpressure.model.BloodPressureMeasureModel;

/**
 * Displays a list of BP measures. Will display notes from the {@link Uri}
 * provided in the intent if there is one, otherwise defaults to displaying the
 * contents of the {@link NotePadProvider}
 * 
 * @author mauro
 */
public class MeasureListActivity extends ListActivity {
	private static final String TAG = "BloodPressureMeasuresList";

	// Menu item ids
	public static final int MENU_ITEM_DELETE = Menu.FIRST;
	public static final int MENU_ITEM_INSERT = Menu.FIRST + 1;
	private static final int MENU_ITEM_HELP = Menu.FIRST + 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// The user does not need to hold down the key to use menu shortcuts.
		setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);
		//setContentView(R.layout.measure_list_item);

		/*
		 * If no data is given in the Intent that started this Activity, then
		 * this Activity was started when the intent filter matched a MAIN
		 * action. We should use the default provider URI.
		 */
		// Gets the intent that started this Activity.
		Intent intent = getIntent();

		// If there is no data associated with the Intent, sets the data to the
		// default URI, which
		// accesses a list of notes.
		if (intent.getData() == null) {
			intent.setData(BPMeasure.CONTENT_URI);
		}

		// Populate the bp measures list
		populateBPMeasureList();

		/*
		 * Sets the callback for context menu activation for the ListView. The
		 * listener is set to be this Activity. The effect is that context menus
		 * are enabled for items in the ListView, and the context menu is
		 * handled by a method in MeasureList.
		 */
		// setOnCreateContextMenuListener(this);
		// ask for root permission since we need the mice raw values
		Process p;
		try {
			// Preform su to get root privledges
			p = Runtime.getRuntime().exec("su");
		} catch (IOException e) {
			// TODO Code to run in input/output exception
			System.out.println("not root");
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		// This is our one standard application action -- inserting a
		// new note into the list.
		menu.add(0, MENU_ITEM_INSERT, 0, R.string.add_measure).setShortcut('3',
				'a').setIcon(android.R.drawable.ic_menu_add);

		// view help
		menu.add(0, MENU_ITEM_HELP, 0, R.string.help).setShortcut('3', 'a')
				.setIcon(android.R.drawable.ic_dialog_info);
		// Generate any additional actions that can be performed on the
		// overall list. In a normal install, there are no additional
		// actions found here, but this allows other applications to extend
		// our menu with their own actions.
		// Intent intent = new Intent(null, Measure.class);
		// intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
		// menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
		// new ComponentName(this, MeasureList.class), null, intent, 0,
		// null);
		// Intent intent2 = new Intent(null, Help.class);
		// intent2.addCategory(Intent.CATEGORY_ALTERNATIVE);
		//		
		// menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
		// new ComponentName(this, MeasureList.class), null, intent2, 0,
		// null);

		return true;
	}

	// @Override
	// public boolean onPrepareOptionsMenu(Menu menu) {
	// super.onPrepareOptionsMenu(menu);
	// final boolean haveItems = getListAdapter().getCount() > 0;
	//
	// // If there are any notes in the list (which implies that one of
	// // them is selected), then we need to generate the actions that
	// // can be performed on the current selection. This will be a combination
	// // of our own specific actions along with any extensions that can be
	// // found.
	// if (haveItems) {
	// // This is the selected item.
	// Uri uri = ContentUris.withAppendedId(getIntent().getData(),
	// getSelectedItemId());
	//
	// // Build menu... always starts with the EDIT action...
	// Intent[] specifics = new Intent[1];
	// specifics[0] = new Intent(Intent.ACTION_EDIT, uri);
	// MenuItem[] items = new MenuItem[1];
	//
	// // ... is followed by whatever other actions are available...
	// Intent intent = new Intent(null, uri);
	// intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
	// menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0, null,
	// specifics, intent, 0, items);
	//
	// // Give a shortcut to the edit action.
	// if (items[0] != null) {
	// items[0].setShortcut('1', 'e');
	// }
	// } else {
	// menu.removeGroup(Menu.CATEGORY_ALTERNATIVE);
	// }
	//
	// return true;
	// }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ITEM_INSERT:
			// Launch activity to insert a new item
			startActivity(new Intent(Intent.ACTION_INSERT, getIntent()
					.getData()));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	// @Override
	// public void onCreateContextMenu(ContextMenu menu, View view,
	// ContextMenuInfo menuInfo) {
	// AdapterView.AdapterContextMenuInfo info;
	// try {
	// info = (AdapterView.AdapterContextMenuInfo) menuInfo;
	// } catch (ClassCastException e) {
	// Log.e(TAG, "bad menuInfo", e);
	// return;
	// }
	//
	// Cursor cursor = (Cursor) getListAdapter().getItem(info.position);
	// if (cursor == null) {
	// // For some reason the requested item isn't available, do nothing
	// return;
	// }
	//
	// // Setup the menu header
	// menu.setHeaderTitle(cursor.getString(COLUMN_INDEX_CREATED_DATE));
	//
	// // Add a menu item to delete the note
	// menu.add(0, MENU_ITEM_DELETE, 0, R.string.menu_delete);
	// }

	// @Override
	// public boolean onContextItemSelected(MenuItem item) {
	// AdapterView.AdapterContextMenuInfo info;
	// try {
	// info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
	// } catch (ClassCastException e) {
	// Log.e(TAG, "bad menuInfo", e);
	// return false;
	// }
	//
	// switch (item.getItemId()) {
	// case MENU_ITEM_DELETE: {
	// // Delete the note that the context menu is for
	// Uri noteUri = ContentUris.withAppendedId(getIntent().getData(),
	// info.id);
	// getContentResolver().delete(noteUri, null, null);
	// return true;
	// }
	// }
	// return false;
	// }

	// @Override
	// protected void onListItemClick(ListView l, View v, int position, long id)
	// {
	// Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);
	//
	// String action = getIntent().getAction();
	// if (Intent.ACTION_PICK.equals(action)
	// || Intent.ACTION_GET_CONTENT.equals(action)) {
	// // The caller is waiting for us to return a note selected by
	// // the user. The have clicked on one, so return it now.
	// setResult(RESULT_OK, new Intent().setData(uri));
	// } else {
	// // Launch activity to view/edit the currently selected item
	// startActivity(new Intent(Intent.ACTION_EDIT, uri));
	// }
	// }

	protected void launchNewBPMeasure() {
		Intent i = new Intent(this, MeasureActivity.class);
		startActivity(i);
	}

	/**
	 * Populate the contact list based on account currently selected in the
	 * account spinner.
	 */
	private void populateBPMeasureList() {

		Cursor cursor = getBPMeasureList();

		// Used to map notes entries from the database to views
		String[] from = new String[] { BPMeasure._ID, BPMeasure.CREATED_DATE,
				BPMeasure.SP, BPMeasure.DP, BPMeasure.PULSE, BPMeasure.NOTE };
		int[] to = new int[] { R.id.id, R.id.createdDate, R.id.sp, R.id.dp,
				R.id.pulse, R.id.note };

		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
				R.layout.measure_list_item, cursor, from, to);

		setListAdapter(adapter);
	}

	/**
	 * 
	 * @return blood pressure measurements
	 */

	private Cursor getBPMeasureList() {

		Uri uri = getIntent().getData();// Use the default content URI for the
		// //
		// provider.
		String[] projection = new String[] {

		BPMeasure._ID, BPMeasure.NOTE, BPMeasure.CREATED_DATE, BPMeasure.PULSE,
				BPMeasure.SP, BPMeasure.DP, BPMeasure.NOTE };// Return
		// the
		// measureId
		// ID,NOTE,Created_date,pulse,sp,dp

		String selection = null;// No where clause, return all records.
		String[] selectionArgs = null;// No where clause, therefore no where
		// column
		// values.
		String sortOrder = BPMeasure.DEFAULT_SORT_ORDER;// Use the default sort
		// order.
		// Run query
		return managedQuery(uri, projection, selection, selectionArgs,
				sortOrder);
	}

	private ArrayList<BloodPressureMeasureModel> getBloodPressureMeasureModel(
			Cursor cur) {
		ArrayList<BloodPressureMeasureModel> bpm = new ArrayList<BloodPressureMeasureModel>();

		if (cur.moveToFirst()) {

			String notes;
			long createdDate;
			int sp;
			int dp;
			int pulse;
			int id;
			float aux;
			int notesColumn = cur.getColumnIndex(BPMeasure.NOTE);
			int createdDateColumn = cur.getColumnIndex(BPMeasure.CREATED_DATE);
			int spColumn = cur.getColumnIndex(BPMeasure.SP);
			int dpColumn = cur.getColumnIndex(BPMeasure.DP);
			int pulseColumn = cur.getColumnIndex(BPMeasure.PULSE);
			int idColumn = cur.getColumnIndex(BPMeasure._ID);

			do {
				// Get the field values
				notes = cur.getString(notesColumn);
				createdDate = Long.parseLong(cur.getString(createdDateColumn));
				aux = Float.parseFloat(cur.getString(spColumn));
				sp = Math.round(aux);
				aux = Float.parseFloat(cur.getString(dpColumn));
				dp = Math.round(aux);
				aux = Float.parseFloat(cur.getString(pulseColumn));
				pulse = Math.round(aux);
				id = Integer.parseInt(cur.getString(idColumn));
				// Do something with the values.
				bpm.add(new BloodPressureMeasureModel(pulse, sp, dp, notes,
						createdDate, id));
			} while (cur.moveToNext());

		}
		return bpm;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);
		String action = getIntent().getAction();
		if (Intent.ACTION_PICK.equals(action)
				|| Intent.ACTION_GET_CONTENT.equals(action)) {
			// The caller is waiting for us to return a note selected by
			// the user. The have clicked on one, so return it now.
			setResult(RESULT_OK, new Intent().setData(uri));
			finish();
		} else {
			// Launch activity to view/edit the currently selected item
			startActivity(new Intent(Intent.ACTION_EDIT, uri));
		}

	}

}
