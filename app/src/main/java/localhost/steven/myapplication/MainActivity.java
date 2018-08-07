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
import java.util.HashMap;
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

    public int getAmountOfLetters(int langIndex){
        switch(langIndex){
            case 1:
                return EnglishLetters.length/2;
            case 2:
                return MongolianLetters.length/2;
            case 3:
                return RussianLetters.length/2;
        }

        return 0;
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

    public String getLetterIndex(String theWord,int langIndex) {
        char firstLetter = theWord.charAt(0);
        switch (langIndex) {
            case 1:
                return getLetterIndexFromArray(firstLetter, EnglishLetters);

            case 2:
                return getLetterIndexFromArray(firstLetter, MongolianLetters);

            case 3:
                return getLetterIndexFromArray(firstLetter, RussianLetters);
        }
        return "None";

    }
    public String getLetterIndexFromArray(char firstLetter,char[] LetterArray) {
        int count = 0;
        for (char letter : LetterArray){
            if (firstLetter == letter)
                return String.valueOf(count / 2 + 1);

            count += 1;
        }


        return "None";
    }

    public BufferedReader getIndexFile(int langIndex, String letterIndex){
        try {
            return new BufferedReader(
                    new InputStreamReader(
                            getApplicationContext().getAssets().
                                    open("LookupIndex" + String.valueOf(langIndex) + "-" +
                                            letterIndex + ".txt")
                    )
            );
        }
        catch(Exception e){
            return null;
        }
    }

    public void AddToResultMap(HashMap<Integer,HashMap<String,String>>KeyResultMap,
                               int langIndex, String keyValue, String actualWord){
        HashMap<String, String> innerMap = KeyResultMap.get(langIndex);
        if(!KeyResultMap.get(langIndex).containsKey(keyValue))
            innerMap.put(keyValue, "");//initialize the map entry if it doesn't exist

        innerMap.put(keyValue, innerMap.get(keyValue) + actualWord); //put Json Key + word
        KeyResultMap.put(langIndex, innerMap); //empty string for now
    }

    //this file has indices for quick lookup
    public void GetKeysFromLookupFile(HashMap<Integer,HashMap<String,String>>KeyResultMap,
                                      ArrayList<String> KeyResults,String searchText,int langIndex){
        try {
            String letterIndex=getLetterIndex(searchText,langIndex);
            BufferedReader lookup = getIndexFile(langIndex,letterIndex);

            String line;
            while ((line = lookup.readLine()) != null)
            {
                String[] keys=line.split("\t");
                if(keys[0].equals(String.valueOf(langIndex)) && keys[1].equals(String.valueOf(letterIndex))
                        && keys[3].contains(searchText)
                        ) {
                    KeyResults.add(keys[2]);
                    AddToResultMap(KeyResultMap, Integer.valueOf(keys[0]), keys[2], keys[3]);
                }
            }
        }
        catch(Exception e){
            //no one cares
        }
    }

    public ArrayList<Integer> langIndices(String searchText){
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

    public void CheckThisFile(HashMap<Integer,HashMap<String,String>>KeyResultMap,
                              ArrayList<String> KeyResults,int langIndex, String letterIndex){
        try {

            BufferedReader lookup = getIndexFile(langIndex,letterIndex);
            String line;

            while ((line = lookup.readLine()) != null) {
                //keys[0] is lang index
                //keys[1] is letter index (A and a are "0" in English, B and b are "1" in English etc)
                //keys[2] is the json key
                //keys[3] is the actual word
                String[] keys = line.split("\t");
                if (KeyResults.contains(keys[2])) {
                    AddToResultMap(KeyResultMap,Integer.valueOf(keys[0]),keys[2],keys[3]);
                }

            }

        } catch (Exception e) {
            //no one cares
        }
    }

    private ArrayList<String> KeyResultsFirstRound;//hold first round search results
    private HashMap<Integer, HashMap<String, String>> KeyResultMapFirstRound; //hold first round search results

    private ArrayList<String> KeyResults;
    private HashMap<Integer, HashMap<String, String>> KeyResultMap;


    public void TraverseJSON(String searchText){
        if(searchText!=null && searchText!="" && searchText.length()>0) {
            ArrayList<Integer> searchTextLangIndices=langIndices(searchText);
            String KeyResult = "";//used to check path

            int langStart = 1; //English
            int langEnd = 3;//Russian

            //only need to get the keys when the search text is one letter
            if(searchText.length()<2) {

                KeyResultMap = new HashMap<>();

                for (int langIndex = langStart; langIndex <= langEnd; langIndex++)
                    KeyResultMap.put(langIndex, new HashMap<String, String>()); //new hashmap for each lang

                KeyResults = new ArrayList<String>();//used to store all paths
                //This will get all keys
                for (int langIndex : searchTextLangIndices)
                    GetKeysFromLookupFile(KeyResultMap,KeyResults, searchText, langIndex);//1 is English, 2 is Mongolian, 3 is Russian

                //Now to connect strings by key
                for (int langIndex = langStart; langIndex <= langEnd; langIndex++){
                    //KeyResultMap.put(langIndex, new HashMap<String, String>()); //new hashmap for each lang

                    //When getting the keys the search Text language already has the values added
                    if(!searchTextLangIndices.contains(langIndex)) {
                        //the language files we're looking in do not match the search text
                        //but instead are target languages
                        //For example we searched in English but now we're looking in Mongolian files
                        //We have to go through all of the Mongolian files

                        for(int eachLetterIndex=1;eachLetterIndex<=getAmountOfLetters(langIndex);eachLetterIndex++) {
                            CheckThisFile(KeyResultMap, KeyResults, langIndex, String.valueOf(eachLetterIndex));
                        }

                        CheckThisFile(KeyResultMap, KeyResults, langIndex, "None"); //non letter searches will match to "None"
                    }
                }//go through each lang

                KeyResultsFirstRound=(ArrayList<String>)KeyResults.clone();
                KeyResultMapFirstRound=(HashMap<Integer,HashMap<String,String>>)KeyResultMap.clone();

            }
            else{
                //KeyResults have been initialized, eliminate ones without searchText

                KeyResults=(ArrayList<String>)KeyResultsFirstRound.clone();
                KeyResultMap=(HashMap<Integer,HashMap<String,String>>)KeyResultMapFirstRound.clone();
                for(int i=0;i<KeyResults.size();i++)
                {
                    String thisKeyResult=KeyResults.get(i);
                    boolean keyRemoved=false;

                    for (int langIndex : searchTextLangIndices) {
                        if (!KeyResultMap.get(langIndex).get(thisKeyResult).contains(searchText)) {
                            if(KeyResultMap.get(langIndex).containsKey(thisKeyResult)) {
                                KeyResults.remove(thisKeyResult);

                                for (int langIndex2 = langStart; langIndex2 <= langEnd; langIndex2++)
                                    KeyResultMap.get(langIndex2).remove(thisKeyResult);

                                keyRemoved=true;
                            }
                        }
                    }

                    if(keyRemoved)
                        i--; //check the new key at this position
                }
            }



            for (int i = 0; i < KeyResults.size(); i++) {
                KeyResult = KeyResults.get(i);
                JSONArray SearchResult = new JSONArray();

                StringBuilder SearchResultString = new StringBuilder();
                SearchResultString.append("Eng: ");
                //append this JSON key's search results for English
                SearchResultString.append(KeyResultMap.get(1).get(KeyResult));
                SearchResultString.append(", ");

                //SearchResultString.append("Chn: ");
                //SearchResultString.append(", ");

                SearchResultString.append("Mng: ");
                SearchResultString.append(KeyResultMap.get(2).get(KeyResult));
                SearchResultString.append(", ");

                SearchResultString.append("Rus: ");
                SearchResultString.append(KeyResultMap.get(3).get(KeyResult));

                if (SearchResultString.toString() != "") {
                    SearchResult.put(SearchResultString.toString());
                }
                AddToResultList(SearchResult);
            } //put all results in blocks
        }
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
