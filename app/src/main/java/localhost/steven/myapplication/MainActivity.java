package localhost.steven.myapplication;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    RelativeLayout mainLayout;
    int resultIndex;
    List<SearchResultLine> initSearchResults;
    EditText searchBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initSearchResults=new ArrayList<SearchResultLine>();
        resultIndex=0;
        mainLayout=(RelativeLayout) findViewById(R.id.mainscreen);

        // Example of a call to a native method
        //TextView tv = (TextView) findViewById(R.id.sample_text);
        //tv.setText(stringFromJNI());

        searchBar = new EditText(this);
        searchBar.addTextChangedListener(new TextWatcher(){
           @Override
            public void afterTextChanged(Editable s){

           }
            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                RunSearch();
            }
        });
        searchBar.setTextSize(12);
        searchBar.setHint("Search Word...");

        RelativeLayout.LayoutParams lineParam =
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,150);

        lineParam.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        //lineParam.setMargins(0,0,0,0);

        searchBar.setLayoutParams(lineParam);

        mainLayout.addView(searchBar);



    }

    public void RunSearch(){
        /*Legend
        * Letter position
        * 1. Word or phrase
        *   W = single word
        *   P = phrase
        * 2. Word Function
        *   N = Noun
        *   V = Verb
        *   A = Adjective
        *   0 = None (phrase)
        * 3. Tense/Declination
        *   Tense
        *       Pa = Past
        *       Pr = Present
        *       Fu = Future
        *   ---------------
        *   Declination
        *       Nom = Nominative Case
        *       Gen = Genitive Case
        *       Dat = Dative Case
        *       Acc = Accusative Case
        *       Inst = Instrumental Case
        *       Prep = Prepositional Case
        *
        *
         */
        for(int clearResults=0; clearResults<initSearchResults.size();clearResults++)
            mainLayout.removeView(initSearchResults.get(clearResults).mainText);

        initSearchResults.clear();
        resultIndex=0;
        String searchText = searchBar.getText().toString();
        TraverseJSON(searchText);
    }

    public void TraverseJSON(String searchText){
        if(searchText!=null && searchText!="" && searchText.length()>0) {
            JSONArray KeyResult = new JSONArray();

            //This will get all keys
            GetKeysFromJSONFile(KeyResult, searchText, "ChineseInit.json");
            GetKeysFromJSONFile(KeyResult, searchText, "RussianInit.json");

            //Now to connect strings by key
            //for (int i = 0; i < KeyResult.length(); i++){
                JSONArray SearchResult = new JSONArray();
                StringBuilder SearchResultString = new StringBuilder();
                SearchResultString.append("Chn: ");
                GetAllElementsByKey(SearchResultString, KeyResult, "ChineseInit.json");
                SearchResultString.append(", ");
                SearchResultString.append("Rus: ");
                GetAllElementsByKey(SearchResultString, KeyResult, "RussianInit.json");

                if (SearchResultString.toString() != "") {
                    SearchResult.put(SearchResultString.toString());
                }
                AddToResultList(SearchResult);
            //}
        }
    }

    public void GetKeysFromJSONFile(JSONArray KeyResult,String searchText,String jsonFileName){
        JSONObject thisJSONObject;
        try {
            thisJSONObject = new JSONObject(loadJSONFromAsset(this, jsonFileName));
            CheckAllKeysOfJSONObject(KeyResult,thisJSONObject, searchText);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void GetAllElementsByKey(StringBuilder SearchResultString,JSONArray KeyResult,String jsonFileName){
        JSONObject thisJSONObject;
        try {
            thisJSONObject = new JSONObject(loadJSONFromAsset(this, jsonFileName));
            JSONArray KeyResultCheck = new JSONArray();
            //for(int i=0;i<KeyResult.length();i++) {
            int i = KeyResult.length()-1; //get last key to check through
                CheckAllElementsByKey(SearchResultString, thisJSONObject, KeyResult.getString(i),KeyResultCheck);
                //CheckAllElementsByKey(SearchResultString, thisJSONObject, KeyResult);
            //}

            boolean checkFailed=false;
            if(KeyResultCheck.length()==KeyResult.length()){
                for(int j=0; j<KeyResultCheck.length(); j++){
                    if(!KeyResultCheck.getString(j).equals(KeyResult.getString(j)))
                        checkFailed=true;
                }

                if(checkFailed)
                    SearchResultString.setLength(0); //clear string builder
            }
            else
                SearchResultString.setLength(0); //clear string builder
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void CheckAllElementsByKey(StringBuilder returnString, JSONObject thisJSONObject, String theKey,JSONArray KeyResultCheck){
        try {

            JSONArray theseJSONKeys = thisJSONObject.names();

            for (int i = 0; i < theseJSONKeys.length(); i++) { //go through all keys
                String thisKey = theseJSONKeys.getString(i); //get actual key
                Object testType = thisJSONObject.get(thisKey); //get the object at key

                KeyResultCheck.put(thisKey);

                if (testType instanceof JSONObject) {
                    CheckAllElementsByKey(returnString, (JSONObject) thisJSONObject.get(thisKey), theKey, KeyResultCheck);
                }
                if(thisKey.equals(theKey)) {
                    //if it is an array, go through array
                    if (testType instanceof JSONArray) {

                        JSONArray thisElement = thisJSONObject.getJSONArray(thisKey);
                        for (int j = 0; j < thisElement.length(); j++) {
                            returnString.append(thisElement.get(j));
                            if(j<thisElement.length()-1)
                                returnString.append(", ");
                        }
                    }

                    if (testType instanceof String) {
                        returnString.append(thisJSONObject.getString(thisKey));
                    }
                }
                else{
                    //at this point if the key is not equal then clear keycheck
                    KeyResultCheck=new JSONArray();
                }
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void CheckAllKeysOfJSONObject(JSONArray returnArray, JSONObject thisJSONObject, String searchText){
        //JSONArray returnArray=new JSONArray();
        try {

            JSONArray theseJSONKeys = thisJSONObject.names();
            for (int i = 0; i < theseJSONKeys.length(); i++) { //go through all keys
                String thisKey = theseJSONKeys.getString(i); //get actual key
                if(!JSONArrayContains(returnArray,thisKey)) {
                    Object testType = thisJSONObject.get(thisKey); //get the object at key
                    returnArray.put(thisKey);//get path no matter what

                    //if the object has names of its own, check recursively
                    if (testType instanceof JSONObject) {
                        CheckAllKeysOfJSONObject(returnArray, (JSONObject) thisJSONObject.get(thisKey), searchText);
                    }

                    //if code reaches below two sentinels there are no more objects
                    //path is at a dead end, if there are no matches this is a dead path
                    //if it is an array, go through array
                    if (testType instanceof JSONArray) {

                        JSONArray thisElement = thisJSONObject.getJSONArray(thisKey);

                        for (int j = 0; j < thisElement.length(); j++) {

                            if (!thisElement.get(j).toString().contains(searchText)) {// && !JSONArrayContains(returnArray,thisElement.get(j).toString())) {
                                //returnArray.put(thisElement.get(j));
                                //returnArray.put(thisKey);
                                returnArray = new JSONArray();//clear array
                            }

                        }
                    }

                    if (testType instanceof String) {
                        String thisString = thisJSONObject.getString(thisKey);
                        if (!thisString.contains(searchText)) { //&& !JSONArrayContains(returnArray,thisString)){
                            //returnArray.put(thisString);
                            //returnArray.put(thisKey);
                            returnArray = new JSONArray();//clear array
                        }

                    }
                }
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        //return returnArray;
    }

    public boolean JSONArrayContains(JSONArray theJSONArray,String checkString){
        boolean retVal = false;
        for(int i = 0;i<theJSONArray.length();i++) {
            try {
                if (theJSONArray.get(i).equals(checkString))
                    retVal = true;
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return retVal;
    }

    public void AddToResultList(JSONArray theJSONArray) {
        try {
            for(int i=0;i<theJSONArray.length();i++) {
                initSearchResults.add(new SearchResultLine(this, resultIndex, mainLayout, theJSONArray.get(i).toString()));
                resultIndex++;
            }
        }
        catch(JSONException e){
            e.printStackTrace();
        }
    }

    public String loadJSONFromAsset(Context context,String fileName) {
        String json = null;
        try {
            //InputStream is = context.getAssets().open(fileName);
            InputStream is = getApplicationContext().getAssets().open(fileName);
            int size = is.available();

            byte[] buffer = new byte[size];

            is.read(buffer);

            is.close();

            json = new String(buffer, "UTF-8");


        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;

    }

    public void PopupMessage(String message){
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
