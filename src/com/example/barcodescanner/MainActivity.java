package com.example.barcodescanner;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener {

	// ID's initialization
	private Button scanBtn, previewBtn, linkBtn;
	private TextView authorText, titleText, descriptionText, dateText,
			ratingCountText;
	private LinearLayout starLayout;
	private ImageView thumbView;
	private ImageView[] starViews;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Get views and buttons and set OnClickListeners
		scanBtn = (Button) findViewById(R.id.scan_button);
		previewBtn = (Button) findViewById(R.id.preview_btn);
		previewBtn.setVisibility(View.GONE);
		linkBtn = (Button) findViewById(R.id.link_btn);
		linkBtn.setVisibility(View.GONE);
		authorText = (TextView) findViewById(R.id.book_author);
		titleText = (TextView) findViewById(R.id.book_title);
		descriptionText = (TextView) findViewById(R.id.book_description);
		dateText = (TextView) findViewById(R.id.book_date);
		starLayout = (LinearLayout) findViewById(R.id.star_layout);
		ratingCountText = (TextView) findViewById(R.id.book_rating_count);
		thumbView = (ImageView) findViewById(R.id.thumb);
		previewBtn.setOnClickListener(this);
		linkBtn.setOnClickListener(this);
		scanBtn.setOnClickListener(this);

		// instantiate the array for the star Image Views
		starViews = new ImageView[5];
		for (int s = 0; s < starViews.length; s++) {
			starViews[s] = new ImageView(this);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onClick(View v) {

		// handle click
		if (v.getId() == R.id.scan_button) {
			IntentIntegrator scanIntegrator = new IntentIntegrator(this);
			scanIntegrator.initiateScan();
		}

	}

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		// retrieve result of scanning - instantiate ZXing object
		IntentResult scanningResult = IntentIntegrator.parseActivityResult(
				requestCode, resultCode, intent);
		// check we have a valid result
		if (scanningResult != null) {
			// get content from Intent Result
			String scanContent = scanningResult.getContents();
			// get format name of data scanned
			String scanFormat = scanningResult.getFormatName();
			if (scanContent != null && scanFormat != null
					&& scanFormat.equalsIgnoreCase("EAN_13")) {
				// book search
				String bookSearchString = "https://www.googleapis.com/books/v1/volumes?"
						+ "q=isbn:"
						+ scanContent
						+ "&key=AIzaSyDuXhrM7zg9x9h01ZcrB4aWY38GcdV3MpM";
			} else {
				Toast toast = Toast.makeText(getApplicationContext(),
						"Not a valid scan!", Toast.LENGTH_SHORT);
				toast.show();
			}
		} else {
			// invalid scan data or scan canceled
			Toast toast = Toast.makeText(getApplicationContext(),
					"No book scan data received!", Toast.LENGTH_SHORT);
			toast.show();
		}

	}

	private class GetBookInfo extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... bookURLs) {
			// request book info
			StringBuilder bookBuilder = new StringBuilder();
			for (String bookSearchURL : bookURLs) {
				// search urls
				HttpClient bookClient = new DefaultHttpClient();
				try {
					// get the data
					HttpGet bookGet = new HttpGet(bookSearchURL);
					HttpResponse bookResponse = bookClient.execute(bookGet);
					StatusLine bookSearchStatus = bookResponse.getStatusLine();
					if (bookSearchStatus.getStatusCode() == 200) {
						// we have a result
						HttpEntity bookEntity = bookResponse.getEntity();
						InputStream bookContent = bookEntity.getContent();
						InputStreamReader bookInput = new InputStreamReader(
								bookContent);
						BufferedReader bookReader = new BufferedReader(
								bookInput);
						String lineIn;
						while ((lineIn = bookReader.readLine()) != null) {
							bookBuilder.append(lineIn);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return bookBuilder.toString();
		}
		// fetch book info

	}

}
