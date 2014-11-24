package com.d34n0s.www.iinetusagemeter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.d34n0s.www.iinetusagemeter.HttpURLConnectionClient.getData;


public class MainActivity extends Activity implements View.OnClickListener{

    EditText et_userName;
    EditText et_password;
    Button b_login;
    TextView tv_authToken;
    TextView tv_serviceToken;
    TextView tv_rawJson;
    TextView tv_url;

    CheckBox cb_showPassword;

    CheckBox cb_saveCreds;

    ProgressDialog progressDialog;

    String urlBase = "https://toolbox.iinet.net.au/cgi-bin/api.cgi";
    String urlComplete;
    String username = "";
    String password = "";
    String authToken = "";
    String serviceToken = "";

    public static String prefsFilename = "sharedPrefsDataFile";
    SharedPreferences prefsSP;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialise();

        loadSharedPrefs();

        if(checkConnection()){
            autoLogin();
        }

    }

    private void initialise(){

        et_userName = (EditText) findViewById(R.id.et_userName);
        et_password = (EditText) findViewById(R.id.et_password);

        b_login = (Button) findViewById(R.id.b_login);
        b_login.setOnClickListener(this);
        b_login.setEnabled(false);

        cb_showPassword = (CheckBox) findViewById(R.id.cb_show_password);
        cb_showPassword.setChecked(false);
        cb_showPassword.setOnClickListener(this);

        cb_saveCreds = (CheckBox) findViewById(R.id.cb_save_creds);
        cb_saveCreds.setChecked(false);
        cb_saveCreds.setOnClickListener(this);

        tv_rawJson = (TextView) findViewById(R.id.tv_rawJson);
        tv_authToken = (TextView) findViewById(R.id.tv_authToken);
        tv_serviceToken = (TextView) findViewById(R.id.tv_serviceToken);
        tv_url = (TextView) findViewById(R.id.tv_url);

    }

    private void loadSharedPrefs(){

        prefsSP = getSharedPreferences(prefsFilename, 0);

        username = prefsSP.getString("userName", "");
        et_userName.setText(username);
        password = prefsSP.getString("password", "");
        et_password.setText(password);
        authToken = prefsSP.getString("authToken", "");
        //tv_authToken.setText(authToken);
        serviceToken = prefsSP.getString("serviceToken", "");
        //tv_serviceToken.setText(serviceToken);
        if(prefsSP.getString("savePassword", "0").matches("1")){
            cb_saveCreds.setChecked(true);
        }
    }

    private void writeSharedPrefs(){

        SharedPreferences.Editor editor = prefsSP.edit();

        if(cb_saveCreds.isChecked()){
            String userName = et_userName.getText().toString();
            String password = et_password.getText().toString();
            editor.putString("userName", userName);
            editor.putString("password", password);
            editor.putString("savePassword", "1");
            editor.putString("authToken", authToken);
            editor.putString("serviceToken", serviceToken);
        }else {
            editor.putString("userName", "");
            editor.putString("password", "");
            editor.putString("savePassword", "0");
            editor.putString("authToken", "");
            editor.putString("serviceToken", "");
        }

        editor.commit();

    }


    private boolean checkConnection(){

        if(ConnectionChecker.isInternetAvailable(this)){

            b_login.setEnabled(true);

            Toast t = Toast.makeText(this, "Connection Available", Toast.LENGTH_SHORT);
            t.show();

            return true;
        }
        else{

            b_login.setEnabled(false);

            Toast t = Toast.makeText(this, "No Connection Available!!", Toast.LENGTH_LONG);
            t.show();

            return false;
        }
    }

    private void autoLogin(){
        if(authToken.matches("") || serviceToken.matches("")){
            if(username.matches("") || password.matches("")) {
                Toast t = Toast.makeText(this, "Enter Username and Password", Toast.LENGTH_LONG);
                t.show();
            } else {
                urlComplete = urlBase + "?_USERNAME=" + username + "&_PASSWORD=" + password;

                new HttpAsyncTask().execute(urlComplete);

            }
        }else {
            goNext(urlBase + "?Usage&_TOKEN=" + authToken + "&_SERVICE=" + serviceToken);
        }
    }


    private class HttpAsyncTask extends AsyncTask<String, Void, String> {

        protected void onPreExecute() {

            super.onPreExecute();
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog = ProgressDialog.show(MainActivity.this, "", "Please Wait", true, false);

        }

        @Override
        protected String doInBackground(String... urls) {


            return getData(urls[0]);

        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {

            progressDialog.dismiss();

            Toast.makeText(getBaseContext(), "Data Received", Toast.LENGTH_LONG).show();

            try {
                JSONObject json = new JSONObject(result);

                //tv_rawJson.setText(json.toString());

                if (json.has("error")) {

                    Toast t = Toast.makeText(MainActivity.this, "Incorrect Username or Password", Toast.LENGTH_LONG);
                    t.show();

                    //tv_url.setText(urlComplete);



                } else if (json.has("token")) {

                    authToken = json.getString("token");

                    JSONObject response = json.getJSONObject("response");

                    JSONArray service_list = response.getJSONArray("service_list");

                    String sUser = et_userName.getText().toString();
                    String sub = (sUser.substring(0,sUser.indexOf("@")));
                    int i = 0;
                    while (i < service_list.length()) {

                        JSONObject json_data = service_list.getJSONObject(i);

                        if(json_data.getString("pk_v").matches(sub)){
                            serviceToken = json_data.getString("s_token");
                        }
                        i++;
                    }


                    //tv_authToken.setText(authToken);

                    //tv_serviceToken.setText(serviceToken);

                    //tv_url.setText(urlComplete);

                    goNext(urlBase + "?Usage&_TOKEN=" + authToken + "&_SERVICE=" + serviceToken);


                } else {

                    Toast t = Toast.makeText(MainActivity.this, "Unknown error occurred.", Toast.LENGTH_LONG);
                    t.show();

                    //tv_url.setText(urlComplete);
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }

            writeSharedPrefs();

        }


    }

    private void goNext(String url){
        Intent i = new Intent(this, Usage.class);
        i.putExtra("url",url);
        startActivity(i);
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
        switch(view.getId()){
            case R.id.b_login:
                username = et_userName.getText().toString();
                password = et_password.getText().toString();


                    if (username.matches("") || password.matches("")) {
                        Toast t = Toast.makeText(this, "Enter Username and Password", Toast.LENGTH_LONG);
                        t.show();
                    } else {
                        urlComplete = urlBase + "?_USERNAME=" + username + "&_PASSWORD=" + password;

                        new HttpAsyncTask().execute(urlComplete);

                    }


                break;

            case R.id.cb_save_creds:

                break;

            case R.id.cb_show_password:
                if (cb_showPassword.isChecked()) {
                    et_password.setInputType(InputType.TYPE_CLASS_TEXT);
                } else {
                    et_password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
                break;




        }
    }
}
