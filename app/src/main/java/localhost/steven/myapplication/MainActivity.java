package localhost.steven.myapplication;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
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
        initSearchResults=new ArrayList<>();
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

        wordCount=3221;
        indexSeparationPoint=25;//words of each lang in each file
        indexCount=wordCount/indexSeparationPoint+1;
        langCount=3;
        KeyCheckedFlag=new boolean[indexCount];//for each lang

        IndexMapFull=new HashMap<>();
        for (int langIndex = 1; langIndex <= langCount; langIndex++) {
            IndexMapFull.put(langIndex, new HashMap<String, HashMap<String, String>>()); //new hashmap for each lang
            for(int eachLetter=1;eachLetter<=getAmountOfLetters(langIndex);eachLetter++)
            {
                HashMap<String, HashMap<String, String>> innerMap=IndexMapFull.get(langIndex);
                innerMap.put(String.valueOf(eachLetter),new HashMap<String,String>());//add for each letter
                innerMap.put("None",new HashMap<String,String>());
                IndexMapFull.put(langIndex,innerMap);
            }
        }
        IndexMapKeyFirst=new HashMap();
        PullIndicesIntoMemory();
    }
    private int indexCount;
    private int wordCount;
    private int langCount;
    private int indexSeparationPoint;

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
            //first part of filename
            String firstPart="";
            if(langIndex>0) {
                firstPart=String.valueOf(langIndex) + "-";
            }
            if(Arrays.asList(getResources().getAssets().list("")).contains("LookupIndex" + firstPart + letterIndex + ".txt"))
            return new BufferedReader(
                    new InputStreamReader(
                            getApplicationContext().getAssets().
                                    open("LookupIndex" + firstPart + letterIndex + ".txt")
                    )
            );

            return null;
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

    //get all keys from memory map
    public void GetKeysFromMemoryMap(String searchText,int langIndex){
        try {
            //check which letter of alphabet of this language it belongs to
            String letterIndex=getLetterIndex(searchText,langIndex);
            for(String s: IndexMapFull.get(langIndex).get(letterIndex).keySet())
            {
                if(!KeyResultMap.get(langIndex).containsKey(s) &&
                    IndexMapFull.get(langIndex).get(letterIndex).get(s).contains(searchText)) {

                    //add to key results
                    KeyResults.add(s);

                    //add lang index, key, then word
                    AddToResultMap(KeyResultMap, langIndex, s,
                                   IndexMapKeyFirst.get(s).get(langIndex));

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

    public boolean[] KeyCheckedFlag;

    //used to time code
    public static class TimeIt{
        static long startTime;
        static long endTime;
        static long duration;

        public static void start(){
            startTime=System.nanoTime();
        }

        public static void stop(String section){
            endTime=System.nanoTime();
            duration=(endTime-startTime)/1000; //to microseconds
            Log.d(" Runtime, Sec-"+section+" :",Long.toString(duration));
        }
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

    public String GetKeyNumber(String key){
        int iter=0;
        String finalKey="";

        while(isNumeric(Character.toString(key.charAt(iter)))
                && iter<key.length()) {
            finalKey += Character.toString(key.charAt(iter));
            iter++;
        }
        return finalKey;
    }

    public void PullIndicesIntoMemory(){
        try {
                //code timing indicates lookup can take up to 1 millisecond
                //by far the slowest single operation in this method.
                //Reduce file lookup.
                BufferedReader lookup = getIndexFile(-1, "");//look up the fully put together index
                String line;

                while ((line = lookup.readLine()) != null) {
                    //keys[0] is lang index
                    //keys[1] is letter index (A and a are "0" in English, B and b are "1" in English etc)
                    //keys[2] is the json key
                    //keys[3] is the actual word
                    //TimeIt.start();
                    String[] keys = line.split("\t");
                    String keyValue=keys[2];
                    String actualWord=keys[3];
                    int langIndex = Integer.valueOf(keys[0]);

                    String[] allWords=actualWord.split(",");//if word has commas, get all of them
                    for(String word:allWords) {

                        String letterIndex = getLetterIndex(word, langIndex);

                        //letter pointing at key
                        HashMap<String, HashMap<String, String>> innerMap = IndexMapFull.get(langIndex);
                        //key pointing at actual word
                        HashMap<String, String> innerInnerMap = innerMap.get(letterIndex);


                        if (!innerInnerMap.containsKey(keyValue))
                            innerInnerMap.put(keyValue, "");//initialize the map entry if it doesn't exist

                        innerInnerMap.put(keyValue, innerInnerMap.get(keyValue) + word);
                        innerMap.put(letterIndex, innerInnerMap);

                        IndexMapFull.put(langIndex, innerMap); //empty string for now
                    }

                    //now add to key first map for lookup
                    //add key if it doesn't exist
                    if(!IndexMapKeyFirst.containsKey(keyValue))
                    {
                        IndexMapKeyFirst.put(keyValue,new HashMap<Integer,String>());
                    }
                    HashMap<Integer,String> KFInnerMap=IndexMapKeyFirst.get(keyValue);
                    KFInnerMap.put(langIndex,actualWord);
                    IndexMapKeyFirst.put(keyValue,KFInnerMap);

                }

                lookup.close();



        } catch (Exception e) {
            System.out.println(e);
        }
    }


    //get word based on key and language from memory map
    public void CheckMemoryMap(ArrayList<Integer> searchTextLangIndices){
        try {

            //memory search
            for(String s : KeyResults)
                for(int i=1; i<=langCount;i++)
                    //the search text language results have already been added
                    //during the key find stage. only add the other languages
                    if(!searchTextLangIndices.contains(i))
                        AddToResultMap(KeyResultMap, i, s, IndexMapKeyFirst.get(s).get(i));

        } catch (Exception e) {
            System.out.println(e);
        }
    }


    private ArrayList<String> KeyResultsFirstRound;//hold first round search results
    private HashMap<Integer, HashMap<String, String>> KeyResultMapFirstRound; //hold first round search results

    private ArrayList<String> KeyResults;
    private HashMap<Integer, HashMap<String, String>> KeyResultMap;

    //letter may be "None" so second index must be String not int
    private HashMap<Integer, HashMap<String,HashMap<String, String>>> IndexMapFull;
    private HashMap<String, HashMap<Integer, String>> IndexMapKeyFirst;

    public void TraverseJSON(String searchText){
        if(searchText.length()==0) {
            KeyResultMap = null;
            KeyResults = null;//used to store all paths
        }
        else {
            ArrayList<Integer> searchTextLangIndices = langIndices(searchText);
            String KeyResult = "";//used to check path

            int langStart = 1; //1 is English
            int langEnd = langCount;//3 is Russian

            //only need to get the keys when the search text is one letter

            if (searchText.length() == 1 && KeyResults == null) { // key finding is very expensive, only do it on the first letter
                KeyCheckedFlag=new boolean[indexCount]; //reset KeyCheckedFlag
                KeyResultMap = new HashMap<>();

                for (int langIndex = langStart; langIndex <= langEnd; langIndex++)
                    KeyResultMap.put(langIndex, new HashMap<String, String>()); //new hashmap for each lang

                KeyResults = new ArrayList<>();//used to store all paths
                //This will get all keys
                for (int langIndex : searchTextLangIndices)
                    GetKeysFromMemoryMap(searchText, langIndex);//1 is English, 2 is Mongolian, 3 is Russian

                //Now to connect strings by key
                CheckMemoryMap(searchTextLangIndices);

                //copy this first round search into memory so that it will be faster if
                //letters are added or deleted. This prevents having to reopen files
                KeyResultsFirstRound = CloneKeyResults(KeyResults);
                KeyResultMapFirstRound = CloneKeyResultMap(KeyResultMap);

            } else {
                //Use in memory results
                //KeyResults have been initialized, eliminate ones without searchText
                KeyResults = CloneKeyResults(KeyResultsFirstRound);
                KeyResultMap = CloneKeyResultMap(KeyResultMapFirstRound);

                //eliminate only if length is greater than 1, otherwise just restore as above
                if(searchText.length() > 1) {
                    for (int i = 0; i < KeyResults.size(); i++) {
                        String thisKeyResult = KeyResults.get(i);
                        boolean keyRemoved = false;

                        //only delete if all langs in searchTextLang don't match this new word
                        int deleteFlag=0;
                        for (int langIndex : searchTextLangIndices) {
                            if (KeyResultMap.get(langIndex).get(thisKeyResult)==null)
                                deleteFlag++;
                            else if(!KeyResultMap.get(langIndex).get(thisKeyResult).contains(searchText))
                                deleteFlag++;

                        }

                        if (deleteFlag==searchTextLangIndices.size()){
                            KeyResults.remove(thisKeyResult);

                            for (int langIndex2 = langStart; langIndex2 <= langEnd; langIndex2++)
                                KeyResultMap.get(langIndex2).remove(thisKeyResult);

                            keyRemoved = true;
                        }



                        if (keyRemoved)
                            i--; //check the new key at this position
                    }
                }
            }

            //JSONArray SearchResult = new JSONArray();
            ArrayList<String> SearchResult=new ArrayList<>();
            for (int i = 0; i < KeyResults.size(); i++) {
                KeyResult = KeyResults.get(i);


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
                    //SearchResult.put(SearchResultString.toString());
                    //SearchResult.add(SearchResultString.toString());
                    initSearchResults.add(new SearchResultLine(this, resultIndex, mainLayout, SearchResultString.toString()));
                    resultIndex++;
                }

            } //put all results in blocks

            //AddToResultList(SearchResult);
        }

    }

    public ArrayList<String> CloneKeyResults(ArrayList<String> source){
        ArrayList<String> target=new ArrayList<>();
        for(String s:source)
            target.add(s);

        return target;
    }

    public HashMap<Integer,HashMap<String,String>> CloneKeyResultMap(HashMap<Integer,HashMap<String,String>> source){
        HashMap<Integer,HashMap<String,String>> target=new HashMap<>();
        for(int i: source.keySet()) {
            target.put(i, new HashMap<String, String>());

            for(String s: source.get(i).keySet()){
                HashMap<String,String> innerMap=target.get(i);
                innerMap.put(s,source.get(i).get(s));
                target.put(i,innerMap);
            }

        }
        return target;
    }



//    public void AddToResultList(JSONArray theJSONArray) {
//        try {
//            for(int i=0;i<theJSONArray.length();i++) {
//                initSearchResults.add(new SearchResultLine(this, resultIndex, mainLayout, theJSONArray.get(i).toString()));
//                resultIndex++;
//            }
//        }
//        catch(JSONException e){
//            e.printStackTrace();
//        }
//    }


    public void AddToResultList(ArrayList<String> theResults) {
        //try {
            for(int i=0;i<theResults.size();i++) {
                initSearchResults.add(new SearchResultLine(this, resultIndex, mainLayout, theResults.get(i)));
                resultIndex++;
            }
        //}
        //catch(JSONException e){
        //    e.printStackTrace();
        //}
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
