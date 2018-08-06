package localhost.steven.myapplication;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

    public char[] EnglishLetters={'A','a','B','b','C','c','D','d','E','e','F','f','G','g','H','h','I','i',
            'J','j','K','k','L','l','M','m','N','n','O','o','P','p','Q','q','R','r',
            'S','s','T','t','U','u','V','v','W','w','X','x','Y','y','Z','z'};

    public char[] RussianLetters={'ё','Ё','ъ','Ъ','я','Я','ш','Ш','е','Е','р','Р','т','Т','ы','Ы',
            'у','У','и','И','о','О','п','П','ю','Ю','щ','Щ','э','Э','а','А',
            'с','С','д','Д','ф','Ф','г','Г','ч','Ч','й','Й','к','К','л','Л',
            'ь','Ь','ж','Ж','з','З','х','Х','ц','Ц','в','В','б','Б','н','Н','м','М'};

    public char[] MongolianLetters={'А','а','Б','б','В','в','Г','г','Д','д','Е','е','Ё','ё','Ж','ж',
            'З','з','И','и','Й','й','К','к','Л','л','М','м','Н','н','О','о',
            'Ө','ө','П','п','Р','р','С','с','Т','т','У','у','Ү','ү','Ф','ф',
            'Х','х','Ц','ц','Ч','ч','Ш','ш','Щ','щ','Ъ','ъ','Ы','ы','Ь','ь',
            'Э','э','Ю','ю','Я','я'};

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
        *   Adv = Adverb
        *   Pr = Pronoun
        *   Prp = Preposition
        *   Con = Conjunction
        *   Int = Interjection
        *   Ar = Article
        *   0 = None (phrase)
        * 3. Tense/Declension
        *   Tense
        *       Pa = Past
        *       Pr = Present
        *       Fu = Future
        *   ---------------
        *   Declension
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

//    public void TraverseJSON(String searchText){
//        if(searchText!=null && searchText!="" && searchText.length()>0) {
//            ArrayList<JSONArray> KeyResults = new ArrayList<JSONArray>();//used to store all paths
//            JSONArray KeyResult = new JSONArray();//used to check path
//
//            //This will get all keys
//            GetKeysFromJSONFile(KeyResults,KeyResult, searchText, "EnglishInit.json");
//            //GetKeysFromJSONFile(KeyResults,KeyResult, searchText, "ChineseInit.json");
//            GetKeysFromJSONFile(KeyResults,KeyResult, searchText, "MongolianInit.json");
//            GetKeysFromJSONFile(KeyResults,KeyResult, searchText, "RussianInit.json");
//
//            //Now to connect strings by key
//            for (int i = 0; i < KeyResults.size(); i++){ //go through all key paths
//                KeyResult=KeyResults.get(i);
//                JSONArray SearchResult = new JSONArray();
//                StringBuilder SearchResultString = new StringBuilder();
//
//                SearchResultString.append("Eng: ");
//                GetAllElementsByKey(SearchResultString, KeyResult, "EnglishInit.json");
//                SearchResultString.append(", ");
//                //SearchResultString.append("Chn: ");
//                //GetAllElementsByKey(SearchResultString, KeyResult, "ChineseInit.json");
//                SearchResultString.append(", ");
//                SearchResultString.append("Mng: ");
//                GetAllElementsByKey(SearchResultString, KeyResult, "MongolianInit.json");
//                SearchResultString.append(", ");
//                SearchResultString.append("Rus: ");
//                GetAllElementsByKey(SearchResultString, KeyResult, "RussianInit.json");
//
//                if (SearchResultString.toString() != "") {
//                    SearchResult.put(SearchResultString.toString());
//                }
//                AddToResultList(SearchResult);
//            }
//        }
//    }

//    public void GetKeysFromJSONFile(ArrayList<JSONArray>KeyResults,JSONArray KeyResult,String searchText,String jsonFileName){
//        JSONObject thisJSONObject;
//        try {
//            thisJSONObject = new JSONObject(loadJSONFromAsset(this, jsonFileName));
//            CheckAllKeysOfJSONObject(KeyResults,KeyResult,thisJSONObject, searchText,0);
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }

    public int getLetterIndex(String theWord,int langIndex) {
        char firstLetter = theWord.charAt(0);
        switch (langIndex) {
            case 1:
                return getLetterIndexFromArray(firstLetter, EnglishLetters);

            case 2:
                return getLetterIndexFromArray(firstLetter, MongolianLetters);

            case 3:
                return getLetterIndexFromArray(firstLetter, RussianLetters);
        }
        return -1;

    }
    public int getLetterIndexFromArray(char firstLetter,char[] LetterArray) {
        int count = 0;
        for (char letter : LetterArray){
            if (firstLetter == letter)
                return count / 2 + 1;

            count += 1;
        }


        return -1;
    }

    //this file has indices for quick lookup
    public void GetKeysFromLookupFile(ArrayList<String> KeyResults,String searchText,int langIndex){
        try {
            BufferedReader lookup = new BufferedReader(new InputStreamReader(getApplicationContext().getAssets().open("LookupIndex.txt")));
            String line;
            while ((line = lookup.readLine()) != null)
            {
                String[] keys=line.split("\t");
                if(keys[0].equals(String.valueOf(langIndex)) && keys[1].equals(String.valueOf(getLetterIndex(searchText,langIndex)))
                        && keys[3].contains(searchText)
                        )
                    KeyResults.add(keys[2]);
            }
        }
        catch(Exception e){
            //no one cares
        }
    }

    public List<Integer> langIndices(String searchText){
        char firstLetter=searchText.charAt(0);
        ArrayList<Integer> retArray=new ArrayList<>();
        for(char c : EnglishLetters)
            if(c==firstLetter)
                retArray.add(1);

        for(char c : MongolianLetters)
            if(c==firstLetter)
                retArray.add(2);

        for(char c : RussianLetters)
            if(c==firstLetter)
                retArray.add(3);

        return retArray;
    }

    public void GetResultsFromLookupFile(StringBuilder SearchResultString,String KeyResult,int langIndex){
        try {
            BufferedReader lookup = new BufferedReader(new InputStreamReader(getApplicationContext().getAssets().open("LookupIndex.txt")));
            String line;
            while ((line = lookup.readLine()) != null)
            {
                String[] keys=line.split("\t");
                if(keys[0].equals(String.valueOf(langIndex)) && keys[2].equals(KeyResult))
                    SearchResultString.append(keys[3]);
            }
        }
        catch(Exception e){
            //no one cares
        }
    }

    public void TraverseJSON(String searchText){
        if(searchText!=null && searchText!="" && searchText.length()>0) {
            ArrayList<String> KeyResults = new ArrayList<String>();//used to store all paths
            String KeyResult = "";//used to check path

            //This will get all keys
            for(int langIndex:langIndices(searchText))
                GetKeysFromLookupFile(KeyResults,searchText,langIndex);//1 is English, 2 is Mongolian, 3 is Russian

            //GetKeysFromJSONFile(KeyResults,KeyResult, searchText, "EnglishInit.json");
            //GetKeysFromJSONFile(KeyResults,KeyResult, searchText, "ChineseInit.json");
            //GetKeysFromJSONFile(KeyResults,KeyResult, searchText, "MongolianInit.json");
            //GetKeysFromJSONFile(KeyResults,KeyResult, searchText, "RussianInit.json");

            //Now to connect strings by key

            try {
                BufferedReader lookup = new BufferedReader(new InputStreamReader(getApplicationContext().getAssets().open("LookupIndex.txt")));
                String line;
                while ((line = lookup.readLine()) != null)
                {
                    String[] keys=line.split("\t");

                    for (int i = 0; i < KeyResults.size(); i++){ //go through all key paths
                        KeyResult=KeyResults.get(i);
                        JSONArray SearchResult = new JSONArray();
                        StringBuilder SearchResultString = new StringBuilder();

                        if(keys[0].equals(String.valueOf(langIndex)) && keys[2].equals(KeyResult))
                            SearchResultString.append(keys[3]);

                        SearchResultString.append("Eng: ");
                        GetResultsFromLookupFile(SearchResultString,KeyResult,1);
                        //GetAllElementsByKey(SearchResultString, KeyResult, "EnglishInit.json");
                        SearchResultString.append(", ");

                        //SearchResultString.append("Chn: ");
                        //GetAllElementsByKey(SearchResultString, KeyResult, "ChineseInit.json");
                        //SearchResultString.append(", ");

                        SearchResultString.append("Mng: ");
                        GetResultsFromLookupFile(SearchResultString,KeyResult,2);
                        //GetAllElementsByKey(SearchResultString, KeyResult, "MongolianInit.json");
                        SearchResultString.append(", ");

                        SearchResultString.append("Rus: ");
                        GetResultsFromLookupFile(SearchResultString,KeyResult,3);
                        //GetAllElementsByKey(SearchResultString, KeyResult, "RussianInit.json");

                        if (SearchResultString.toString() != "") {
                            SearchResult.put(SearchResultString.toString());
                        }
                        AddToResultList(SearchResult);
                    }

                }




            }
            catch(Exception e){
                //no one cares
            }
        }
    }

    public void GetKeysFromJSONFile(ArrayList<String>KeyResults,String KeyResult,String searchText,String jsonFileName){
        JSONObject thisJSONObject;
        try {
            thisJSONObject = new JSONObject(loadJSONFromAsset(this, jsonFileName));
            CheckAllKeysOfJSONObject(KeyResults,KeyResult,thisJSONObject, searchText,0);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

//    public void GetAllElementsByKey(StringBuilder SearchResultString,JSONArray KeyResult,String jsonFileName){
//        JSONObject thisJSONObject;
//        try {
//            thisJSONObject = new JSONObject(loadJSONFromAsset(this, jsonFileName));
//            JSONArray KeyResultCheck = new JSONArray();
//            CheckAllElementsByKey(SearchResultString, thisJSONObject,KeyResult,0);
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }

    public void GetAllElementsByKey(StringBuilder SearchResultString,String KeyResult,String jsonFileName){
        JSONObject thisJSONObject;
        try {
            thisJSONObject = new JSONObject(loadJSONFromAsset(this, jsonFileName));
            JSONArray KeyResultCheck = new JSONArray();
            CheckAllElementsByKey(SearchResultString, thisJSONObject,KeyResult,"",0);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

//    public void CheckAllElementsByKey(StringBuilder returnString, JSONObject thisJSONObject,JSONArray KeyResult, int recursionDepth){
//        try {
//
//            JSONArray theseJSONKeys = thisJSONObject.names();
//
//            for (int i = 0; i < theseJSONKeys.length(); i++) { //go through all keys
//                String thisKey = theseJSONKeys.getString(i); //get actual key
//                Object testType = thisJSONObject.get(thisKey); //get the object at key
//
//                String thisKeyResult = KeyResult.getString(recursionDepth);
//                if(thisKey.equals(thisKeyResult)) {
//                    if (testType instanceof JSONObject) {
//                        CheckAllElementsByKey(returnString, (JSONObject) thisJSONObject.get(thisKey),KeyResult,recursionDepth+1);
//                    }
//
//                    //if it is an array, go through array
//                    if (testType instanceof JSONArray) {
//
//                        JSONArray thisElement = thisJSONObject.getJSONArray(thisKey);
//                        for (int j = 0; j < thisElement.length(); j++) {
//                            returnString.append(thisElement.get(j));
//                            if(j<thisElement.length()-1)
//                                returnString.append(", ");
//                        }
//                    }
//
//                    if (testType instanceof String) {
//                        returnString.append(thisJSONObject.getString(thisKey));
//                    }
//                }
//            }
//        }
//        catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }

    public void CheckAllElementsByKey(StringBuilder returnString, JSONObject thisJSONObject,String KeyResult,String currentKey, int recursionDepth){
        try {

            JSONArray theseJSONKeys = thisJSONObject.names();

            for (int i = 0; i < theseJSONKeys.length(); i++) { //go through all keys
                String thisKey = theseJSONKeys.getString(i); //get actual key
                Object testType = thisJSONObject.get(thisKey); //get the object at key
                currentKey+=thisKey;

                //String thisKeyResult = KeyResult.getString(recursionDepth);
                //if(thisKey.equals(thisKeyResult)) {
                    if (testType instanceof JSONObject) {
                        CheckAllElementsByKey(returnString, (JSONObject) thisJSONObject.get(thisKey),KeyResult,currentKey,recursionDepth+1);
                    }

                    if(currentKey.equals(KeyResult)) {
                        //if it is an array, go through array
                        if (testType instanceof JSONArray) {

                            JSONArray thisElement = thisJSONObject.getJSONArray(thisKey);
                            for (int j = 0; j < thisElement.length(); j++) {
                                returnString.append(thisElement.get(j));
                                if (j < thisElement.length() - 1)
                                    returnString.append(", ");
                            }
                        }

                        if (testType instanceof String) {
                            returnString.append(thisJSONObject.getString(thisKey));
                        }


                    }

                    currentKey=currentKey.substring(0,currentKey.indexOf(thisKey)); //remove last element
                //}
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void ClearJSONArray(JSONArray theJSONArray){
        int initialLength = theJSONArray.length(); //store initial length because it will decrease
        for (int i=0; i<initialLength;i++)
            theJSONArray.remove(0); //keep popping off the original one
    }
    public static boolean isNumeric(String str)
    {
        try
        {
            double d = Double.parseDouble(str);
        }
        catch(NumberFormatException nfe)
        {
            return false;
        }
        return true;
    }

    public void CheckAllKeysOfJSONObject(ArrayList<String> returnArrays,String returnArray, JSONObject thisJSONObject, String searchText,int recursionDepth){
        //JSONArray returnArray=new JSONArray();
        try {

            JSONArray theseJSONKeys = thisJSONObject.names();
            for (int i = 0; i < theseJSONKeys.length(); i++) { //go through all keys
                String thisKey = theseJSONKeys.getString(i); //get actual key
                if(!returnArrays.contains(returnArray) && !(recursionDepth==0 && !isNumeric(thisKey))) {
                    Object testType = thisJSONObject.get(thisKey); //get the object at key
                    returnArray+=thisKey;//get path no matter what

                    //if the object has names of its own, check recursively
                    if (testType instanceof JSONObject) {
                        CheckAllKeysOfJSONObject(returnArrays,returnArray, (JSONObject) thisJSONObject.get(thisKey), searchText,recursionDepth+1);
                    }

                    //if code reaches below two sentinels there are no more objects
                    //path is at a dead end, if there are no matches this is a dead path
                    //if it is an array, go through array
                    if (testType instanceof JSONArray) {

                        JSONArray thisElement = thisJSONObject.getJSONArray(thisKey);
                        boolean searchFailed=true;
                        for (int j = 0; j < thisElement.length(); j++) {

                            if (thisElement.get(j).toString().contains(searchText) ) {// && !JSONArrayContains(returnArray,thisElement.get(j).toString())) {
                                //returnArray.put(thisElement.get(j));
                                //returnArray.put(thisKey);
                                searchFailed=false;

                            }

                        }
                        if(!searchFailed && !returnArrays.contains(returnArray))
                            returnArrays.add(returnArray); //if keyset was not deleted add it using clone (pass by value)

                        //ClearJSONArray(returnArray);//= new JSONArray();//clear array

                    }

                    if (testType instanceof String) {
                        String thisString = thisJSONObject.getString(thisKey);
                        if (thisString.contains(searchText) && !returnArrays.contains(returnArray)) { //&& !JSONArrayContains(returnArray,thisString)){
                            //returnArray.put(thisString);
                            //returnArray.put(thisKey);
                            returnArrays.add(returnArray); //if keyset was not deleted add it

                        }

                        //ClearJSONArray(returnArray);//=new JSONArray();//clear array

                    }
                    returnArray=returnArray.substring(0,returnArray.indexOf(thisKey)); //remove last element
                }
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        //return returnArray;
    }

//    public void CheckAllKeysOfJSONObject(ArrayList<JSONArray> returnArrays,JSONArray returnArray, JSONObject thisJSONObject, String searchText,int recursionDepth){
//        //JSONArray returnArray=new JSONArray();
//        try {
//
//            JSONArray theseJSONKeys = thisJSONObject.names();
//            for (int i = 0; i < theseJSONKeys.length(); i++) { //go through all keys
//                String thisKey = theseJSONKeys.getString(i); //get actual key
//                if(!JSONArrayContains(returnArray,thisKey) && !(recursionDepth==0 && !isNumeric(thisKey))) {
//                    Object testType = thisJSONObject.get(thisKey); //get the object at key
//                    returnArray.put(thisKey);//get path no matter what
//
//                    //if the object has names of its own, check recursively
//                    if (testType instanceof JSONObject) {
//                        CheckAllKeysOfJSONObject(returnArrays,returnArray, (JSONObject) thisJSONObject.get(thisKey), searchText,recursionDepth+1);
//                    }
//
//                    //if code reaches below two sentinels there are no more objects
//                    //path is at a dead end, if there are no matches this is a dead path
//                    //if it is an array, go through array
//                    if (testType instanceof JSONArray) {
//
//                        JSONArray thisElement = thisJSONObject.getJSONArray(thisKey);
//                        boolean searchFailed=true;
//                        for (int j = 0; j < thisElement.length(); j++) {
//
//                            if (thisElement.get(j).toString().contains(searchText) ) {// && !JSONArrayContains(returnArray,thisElement.get(j).toString())) {
//                                //returnArray.put(thisElement.get(j));
//                                //returnArray.put(thisKey);
//                                searchFailed=false;
//
//                            }
//
//                        }
//                        if(!searchFailed && !ListOfJSONArrayContains(returnArrays,returnArray))
//                            returnArrays.add(cloneJSONArray(returnArray)); //if keyset was not deleted add it using clone (pass by value)
//
//                        //ClearJSONArray(returnArray);//= new JSONArray();//clear array
//
//                    }
//
//                    if (testType instanceof String) {
//                        String thisString = thisJSONObject.getString(thisKey);
//                        if (thisString.contains(searchText) && !ListOfJSONArrayContains(returnArrays,returnArray)) { //&& !JSONArrayContains(returnArray,thisString)){
//                            //returnArray.put(thisString);
//                            //returnArray.put(thisKey);
//                            returnArrays.add(cloneJSONArray(returnArray)); //if keyset was not deleted add it
//
//                        }
//
//                        //ClearJSONArray(returnArray);//=new JSONArray();//clear array
//
//                    }
//
//                    returnArray.remove(returnArray.length()-1); //remove last element
//                }
//            }
//        }
//        catch (JSONException e) {
//            e.printStackTrace();
//        }
//        //return returnArray;
//    }

    public JSONArray cloneJSONArray(JSONArray sourceArray){
        JSONArray cloneArray = new JSONArray();
        try {
            for (int i=0;i < sourceArray.length(); i++)
                cloneArray.put(sourceArray.getString(i));
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return cloneArray;
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

    public boolean ListOfJSONArrayContains(ArrayList<JSONArray> theJSONArrays, JSONArray checkJSONArray){
        boolean retVal=false;
        for(int i=0; i<theJSONArrays.size(); i++){
            boolean matches=true;
            JSONArray thisJSONArray=theJSONArrays.get(i);
            if(thisJSONArray.length()==checkJSONArray.length()) {
                for (int j = 0; j < thisJSONArray.length(); j++) {
                    try {
                        if (!thisJSONArray.get(j).equals(checkJSONArray.get(j)))
                            matches=false;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if(matches)
                    retVal=true;
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
