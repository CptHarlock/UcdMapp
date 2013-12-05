package com.mapapp.mapapp;

import com.mapapp.main.MapAppActivity;
import com.ucdmap.R;

import android.os.Bundle;
import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class AndroidTabLayoutActivity extends TabActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		TabHost tabHost = getTabHost();
        TabSpec AZ= tabHost.newTabSpec("A-Z");
        // setting Title and Icon for the Tab
        AZ.setIndicator("A-Z");
        Intent AZIntent = new Intent(this, AZ.class);
        AZ.setContent(AZIntent);
        
        tabHost.addTab(AZ); 

        
        TabSpec Academic = tabHost.newTabSpec("Academic");
        Academic.setIndicator("Academic");
        Intent AcademicIntent = new Intent(this, Academic.class);
        Academic.setContent(AcademicIntent);
        
        tabHost.addTab(Academic); 
        
        TabSpec Catering = tabHost.newTabSpec("Catering");        
        Catering.setIndicator("Catering");
        Intent CateringIntent = new Intent(this, Catering.class);
        Catering.setContent(CateringIntent);
         
        tabHost.addTab(Catering); // Adding songs tab
        
        
        
        TabSpec Services = tabHost.newTabSpec("Services");
        Services.setIndicator("Services");
        Intent ServicesIntent = new Intent(this, Services.class);
        Services.setContent(ServicesIntent);
        
        tabHost.addTab(Services); 
        
        TabSpec Search = tabHost.newTabSpec("Search");        
        Search.setIndicator("Search");
        Intent SearchIntent = new Intent(this, Search.class);
        Search.setContent(SearchIntent);
        
        tabHost.addTab(Search);
 
  
        
        TabSpec Links = tabHost.newTabSpec("Links");
        Links.setIndicator("Links");
        Intent LinksIntent = new Intent(this, Links.class);
        Links.setContent(LinksIntent);
        
        tabHost.addTab(Links);
         

 Button mapmode = (Button) findViewById(R.id.mapmode);
	    
 mapmode.setOnClickListener(new View.OnClickListener()
	    {
	        @Override
	        public void onClick(View v)
	        {	
	        	
	        	Intent intent = new Intent(getBaseContext(), MapAppActivity.class);
	    		startActivity(intent);
	    		finish();
	        }
	    });
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
