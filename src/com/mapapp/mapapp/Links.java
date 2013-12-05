package com.mapapp.mapapp;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import com.ucdmap.R;

public class Links extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_links);
		
		
		ImageView img = (ImageView)findViewById(R.id.imageView1);
		img.setOnClickListener(new View.OnClickListener(){
	    public void onClick(View v){
	        Intent intent = new Intent();
	        intent.setAction(Intent.ACTION_VIEW);
	        intent.addCategory(Intent.CATEGORY_BROWSABLE);
	        intent.setData(Uri.parse("http://www.ucd.ie/maps/"));
	        startActivity(intent);
	    	}
		});
		
		ImageView imga = (ImageView)findViewById(R.id.imageView2);
		imga.setOnClickListener(new View.OnClickListener(){
	    public void onClick(View v){
	        Intent intent = new Intent();
	        intent.setAction(Intent.ACTION_VIEW);
	        intent.addCategory(Intent.CATEGORY_BROWSABLE);
	        intent.setData(Uri.parse("http://www.ucd.ie"));
	        startActivity(intent);
	    	}
		});
	}

}
