package com.d34n0s.www.iinetusagemeter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static com.d34n0s.www.iinetusagemeter.HttpURLConnectionClient.getData;

/**
 * Created by dlawrence on 18/11/2014.
 */
public class Usage extends Activity implements View.OnClickListener{

    //account info
    TextView tv_usage_plan;
    TextView tv_usage_product;
    TextView tv_usage_ip;
    TextView tv_usage_onSince;
    TextView tv_usage_daysGone;
    TextView tv_usage_daysRemaining;
    TextView tv_usage_anniversary;

    //enable to display raw data
    //TextView tv_usage_json;
    //TextView tv_usage_url;

    //list of traffic usage
    ListView lv_usage_traffic;
    ProgressDialog progressDialog;
    String urlComplete;


    //screen dimensions
    int sWidth;
    int sHeight;
    int pixelsPerPercent;
    Double percentageDaysUsed;

    public static String prefsFilename = "sharedPrefsDataFile";
    SharedPreferences prefsSP;

    //this is the array to hold our class data
    ArrayList<Usage_Traffic> arrayOfWebData = new ArrayList<Usage_Traffic>();

    //this is our adapter which is also a class variable
    FancyAdapter fa = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.usage);

        initialise();

        getScreenDimentions();

        urlComplete = getIntent().getExtras().getString("url");

        new HttpAsyncTask().execute(urlComplete);
    }

    private void initialise(){
        //tv_usage_json = (TextView) findViewById(R.id.tv_usage_json);
        //tv_usage_url = (TextView) findViewById(R.id.tv_usage_url);
        lv_usage_traffic = (ListView) findViewById(R.id.lv_usage_traffic);
        tv_usage_plan = (TextView) findViewById(R.id.tv_usage_plan);
        tv_usage_product = (TextView) findViewById(R.id.tv_usage_product);
        tv_usage_ip = (TextView) findViewById(R.id.tv_usage_ip);
        tv_usage_onSince = (TextView) findViewById(R.id.tv_usage_onSince);
        tv_usage_daysGone = (TextView) findViewById(R.id.tv_usage_daysGone);
        tv_usage_daysRemaining = (TextView) findViewById(R.id.tv_usage_daysRemaining);
        tv_usage_anniversary = (TextView) findViewById(R.id.tv_usage_anniversary);

    }

    private void getScreenDimentions(){
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        sWidth = size.x;
        sHeight = size.y;

        if(sWidth != 0){
            pixelsPerPercent = sWidth / 100;
        }
    }

    private class HttpAsyncTask extends AsyncTask<String, Void, String> {

        protected void onPreExecute() {

            super.onPreExecute();
            progressDialog = new ProgressDialog(Usage.this);
            progressDialog = ProgressDialog.show(Usage.this, "", "Please Wait", true, false);

        }

        @Override
        protected String doInBackground(String... urls) {

            //return GET(urls[0]); //this one uses the old HttpClient (Apache) connection method
            return getData(urls[0]); //this one uses the new HttpUTLConnection method

        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {

            progressDialog.dismiss();

            Toast.makeText(getBaseContext(), "Data Received", Toast.LENGTH_LONG).show();

            try {
                JSONObject json = new JSONObject(result);

                if(json.has("error")){
                    Toast.makeText(getBaseContext(), "Retrying: " + json.getString("error"), Toast.LENGTH_LONG).show();

                    SharedPreferences.Editor editor = prefsSP.edit();

                    editor.putString("authToken", "");
                    editor.putString("serviceToken", "");
                    editor.commit();

                    Intent i = new Intent(Usage.this, MainActivity.class);
                    startActivity(i);

                }

                JSONObject response = json.getJSONObject("response");

                JSONObject usage = response.getJSONObject("usage");

                JSONArray traffic_types = usage.getJSONArray("traffic_types");

                int i = 0;
                while (i < traffic_types.length()) {

                    JSONObject json_data = traffic_types.getJSONObject(i);
                    Usage_Traffic resultRow = new Usage_Traffic();

                    resultRow.name = json_data.getString("name");
                    resultRow.used = json_data.getString("used");
                    resultRow.allocation = json_data.getString("allocation");

                    arrayOfWebData.add(resultRow);

                    i++;
                }

                //set account info into object
                Usage_Account_Info acc = new Usage_Account_Info();
                JSONArray connectionsArray = response.getJSONArray("connections");
                JSONObject connections = connectionsArray.getJSONObject(0);
                acc.ip = connections.getString("ip");
                acc.on_since =  connections.getString("on_since");

                JSONObject account_info = response.getJSONObject("account_info");
                acc.plan = account_info.getString("plan");
                acc.product = account_info.getString("product");

                JSONObject quota_reset = response.getJSONObject("quota_reset");
                acc.days_so_far = quota_reset.getString("days_so_far");
                acc.anniversary = quota_reset.getString("anniversary");
                acc.days_remaining = quota_reset.getString("days_remaining");

                percentageDaysUsed = acc.getPercentageDaysUsed();

                tv_usage_plan.setText(acc.plan);
                tv_usage_product.setText(acc.product);
                tv_usage_ip.setText(acc.ip);
                tv_usage_onSince.setText(acc.on_since);
                tv_usage_daysGone.setText(acc.days_so_far);
                tv_usage_daysRemaining.setText(acc.days_remaining);

                Calendar c = Calendar.getInstance();

                SimpleDateFormat df = new SimpleDateFormat("MMM/yyyy");
                String formattedDate = df.format(c.getTime());

                SimpleDateFormat dffull = new SimpleDateFormat("dd/MMM/yyyy");
                String sCurrentDate = dffull.format(c.getTime());
                Date dCurrentDate = dffull.parse(sCurrentDate);


                String AnniversaryDate = acc.anniversary + "/" + formattedDate;


                Date convertedDate = null;
                try {
                    SimpleDateFormat newdf = new SimpleDateFormat("dd/MMM/yyyy");
                    convertedDate = newdf.parse(AnniversaryDate);
                    AnniversaryDate = String.valueOf(convertedDate);
                    if(convertedDate.before(dCurrentDate) ){
                        Calendar c1 = Calendar.getInstance();
                            c1.setTime(convertedDate);
                        c1.add(Calendar.MONTH, 1);  // number of days to add, can also use Calendar.DAY_OF_MONTH in place of Calendar.DATE
                        String output = newdf.format(c1.getTime());
                        convertedDate = newdf.parse(output);
                        AnniversaryDate = String.valueOf(convertedDate);
                    }


                } catch (ParseException e) {

                    e.printStackTrace();
                }
                tv_usage_anniversary.setText(AnniversaryDate);
                //tv_usage_url.setText(urlComplete);


            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            //initialise our fancy adapter object
            fa = new FancyAdapter();

            //set the adapter to turn it on
            lv_usage_traffic.setAdapter(fa);

        }
        //String sub = et_userName.getText().toString();
        //tv_status.setText(sub.substring(0,sub.indexOf("@")));

    }

    class FancyAdapter extends ArrayAdapter<Usage_Traffic> {

        FancyAdapter(){
            super(Usage.this, android.R.layout.simple_list_item_1, arrayOfWebData);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            //we use an if statement on our view that is passed in, to see if ti has been recycled or not.
            // if it's been recycled, then it already exists and we don't need to call the inflater function.
            // this saves heaps of time and resources.
            if (convertView == null) {
                LayoutInflater inflater = getLayoutInflater();
                convertView = inflater.inflate(R.layout.usage_list, null);

                //we're using a view holder class to cache the result of the findViewbyID function which we then store in a tag on the view
                holder = new ViewHolder(convertView);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.populateFrom(arrayOfWebData.get(position));

            return(convertView);
        }
    }

    class ViewHolder {
        public TextView tv_usage_name = null;
        public TextView tv_usage_data = null;
        public TextView tv_usage_allocation = null;
        public TextView tv_usage_remaining = null;
        //progress bar
        public ImageView iv_usage_percentDataBar = null;
        public TextView tv_usage_percentDataUsed = null;
        public ImageView iv_usage_percentDaysBar = null;
        public TextView tv_usage_percentDaysUsed = null;
        public LinearLayout ll_progressBar = null;



        ViewHolder(View resultRow){
            tv_usage_name = (TextView) resultRow.findViewById(R.id.tv_usage_name);
            tv_usage_data = (TextView) resultRow.findViewById(R.id.tv_usage_data);
            tv_usage_allocation = (TextView) resultRow.findViewById(R.id.tv_usage_allocation);
            tv_usage_remaining = (TextView) resultRow.findViewById(R.id.tv_usage_remaining);
            iv_usage_percentDataBar = (ImageView) resultRow.findViewById(R.id.iv_usage_percentDataBar);
            tv_usage_percentDataUsed = (TextView) resultRow.findViewById(R.id.tv_usage_percentDataUsed);
            iv_usage_percentDaysBar = (ImageView) resultRow.findViewById(R.id.iv_usage_percentDaysBar);
            tv_usage_percentDaysUsed = (TextView) resultRow.findViewById(R.id.tv_usage_percentDaysUsed);
            ll_progressBar = (LinearLayout) resultRow.findViewById(R.id.ll_progressBar);

        }

        String populateFrom(Usage_Traffic ut) {
            //set the basic data on each element
            tv_usage_name.setText(ut.name);
            tv_usage_data.setText(String.format("%.2f", ut.getUsedMB()));
            tv_usage_allocation.setText(String.format("%.2f", ut.getAllocationdMB()));
            tv_usage_remaining.setText(String.format("%.2f", ut.getRemaining()));
            tv_usage_percentDataUsed.setText("Data Used: " + ut.getPercentDataUsed().toString() + "%");
            tv_usage_percentDaysUsed.setText("Days Used: " + String.format("%.0f", percentageDaysUsed) + "%");

            if(ut.getName().contentEquals("peak") || ut.getName().contentEquals("offpeak")){
                ll_progressBar.setVisibility(ll_progressBar.VISIBLE);
            }

            int PercentDataUsed = ut.getPercentDataUsed();
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(PercentDataUsed * pixelsPerPercent, 40);
            iv_usage_percentDataBar.setLayoutParams(layoutParams);

            LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams((int) (percentageDaysUsed * pixelsPerPercent), 40);
            iv_usage_percentDaysBar.setLayoutParams(layoutParams1);

            if(PercentDataUsed > percentageDaysUsed - 15){
                iv_usage_percentDataBar.setImageResource(R.drawable.progressbar_data_warn);
            }
            if(PercentDataUsed > percentageDaysUsed){
                iv_usage_percentDataBar.setImageResource(R.drawable.progressbar_data_danger);
            }





            //tv_usage_percentDaysUsed.getLayoutParams().width =(percentageDaysUsed);

            return null;


        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_logout) {
            prefsSP = getSharedPreferences(prefsFilename, 0);
            SharedPreferences.Editor editor = prefsSP.edit();
            editor.putString("userName", "");
            editor.putString("password", "");
            editor.putString("savePassword", "0");
            editor.putString("authToken", "");
            editor.putString("serviceToken", "");
            editor.commit();
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onClick(View view) {

    }
}
