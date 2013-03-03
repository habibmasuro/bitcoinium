package com.veken0m.bitcoinium;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class MinerStatsActivity extends SherlockFragmentActivity {

	static String pref_favPool;
	private static String pref_emcKey;
	private static String pref_slushKey;
	private static String pref_bitminterKey;
	private static String pref_deepbitKey;
	private static String pref_50BTCKey;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// ActionBar gets initiated and set to tabbed mode
		ActionBar actionbar = getSupportActionBar();
		actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		// Add the pools that have API keys
		readPreferences(getApplicationContext());
		if (pref_bitminterKey.length() > 6) {
			SherlockFragment BitMinterFragment = new BitMinterFragment();
			ActionBar.Tab BitMinterTab = actionbar.newTab()
					.setText("BitMinter");
			BitMinterTab.setTabListener(new MyTabsListener(BitMinterFragment));
			actionbar.addTab(BitMinterTab);
		}
		if (pref_deepbitKey.length() > 6) {
			SherlockFragment DeepBitFragment = new DeepBitFragment();
			ActionBar.Tab DeepBitTab = actionbar.newTab().setText("DeepBit");
			DeepBitTab.setTabListener(new MyTabsListener(DeepBitFragment));
			actionbar.addTab(DeepBitTab);
		}
		if (pref_slushKey.length() > 6) {
			SherlockFragment SlushFragment = new SlushFragment();
			ActionBar.Tab SlushTab = actionbar.newTab().setText("Slush");
			SlushTab.setTabListener(new MyTabsListener(SlushFragment));
			actionbar.addTab(SlushTab);
		}
		if (pref_emcKey.length() > 6) {
			SherlockFragment EMCFragment = new EMCFragment();
			ActionBar.Tab EMCTab = actionbar.newTab().setText("EclipseMC");
			EMCTab.setTabListener(new MyTabsListener(EMCFragment));
			actionbar.addTab(EMCTab);
		}
		if (pref_50BTCKey.length() > 6) {
			SherlockFragment FiftyBTCFragment = new FiftyBTCFragment();
			ActionBar.Tab FiftyBTCTab = actionbar.newTab().setText("50BTC");
			FiftyBTCTab.setTabListener(new MyTabsListener(FiftyBTCFragment));
			actionbar.addTab(FiftyBTCTab);
		}

		setContentView(R.layout.minerstats);
		try {
			setDifficulty(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
		actionbar.show();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		setContentView(R.layout.minerstats);

	}

	class MyTabsListener implements ActionBar.TabListener {
		public SherlockFragment fragment;

		public MyTabsListener(SherlockFragment fragment) {
			this.fragment = fragment;
		}

		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {
			// ft.replace(R.id.table_fragment, fragment);
		}

		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			ft.replace(R.id.table_fragment, fragment);
		}

		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			ft.remove(fragment);
		}

	}

	final Handler mMinerHandler = new Handler();
	protected Boolean connectionFail = false;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.preferences) {
			startActivity(new Intent(this, PreferencesActivity.class));
		}
		return super.onOptionsItemSelected(item);
	}

	public void setDifficulty(Context context) throws ClientProtocolException,
			IOException, EOFException {

		// TODO: Move networking to separate thread
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
				.permitAll().build();
		StrictMode.setThreadPolicy(policy);

		HttpClient client = new DefaultHttpClient();
		HttpGet post = new HttpGet("http://blockexplorer.com/q/getdifficulty");
		HttpResponse response = client.execute(post);
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				response.getEntity().getContent(), "UTF-8"));
		String CurrentDifficulty = reader.readLine();
		reader.close();

		post = new HttpGet("http://blockexplorer.com/q/estimate");
		response = client.execute(post);
		reader = new BufferedReader(new InputStreamReader(response.getEntity()
				.getContent(), "UTF-8"));
		String NextDifficulty = reader.readLine();
		reader.close();

		LinearLayout view = (LinearLayout) findViewById(R.id.miner_difficulty);

		TextView tvCurrentDifficulty = new TextView(context);
		TextView tvNextDifficulty = new TextView(context);

		tvCurrentDifficulty
				.setText("\nCurrent Difficulty: "
						+ Utils.formatDecimal(Float.valueOf(CurrentDifficulty),
								0, true));
		tvCurrentDifficulty.setGravity(Gravity.CENTER_HORIZONTAL);
		tvNextDifficulty.setText("Estimated Next Difficulty: "
				+ Utils.formatDecimal(Float.valueOf(NextDifficulty), 0, true)
				+ "\n");
		tvNextDifficulty.setGravity(Gravity.CENTER_HORIZONTAL);
		if (Float.valueOf(NextDifficulty) < Float.valueOf(CurrentDifficulty)) {
			tvNextDifficulty.setTextColor(Color.GREEN);
		} else {
			tvNextDifficulty.setTextColor(Color.RED);
		}

		view.addView(tvCurrentDifficulty);
		view.addView(tvNextDifficulty);

	}

	protected static void readPreferences(Context context) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);

		SharedPreferences.OnSharedPreferenceChangeListener prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
			public void onSharedPreferenceChanged(SharedPreferences pPrefs,
					String key) {

				pref_favPool = pPrefs.getString("favpoolPref", "bitminter");
			}
		};

		prefs.registerOnSharedPreferenceChangeListener(prefListener);

		pref_favPool = prefs.getString("favpoolPref", "bitminter");
		pref_emcKey = prefs.getString("emcKey", "");
		pref_slushKey = prefs.getString("slushKey", "");
		pref_bitminterKey = prefs.getString("bitminterKey", "");
		pref_deepbitKey = prefs.getString("deepbitKey", "");
		pref_50BTCKey = prefs.getString("50BTCKey", "");
	}

}
