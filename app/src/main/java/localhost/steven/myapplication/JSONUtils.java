package localhost.steven.myapplication;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class JSONUtils {

    public static void ClearJSONArray(JSONArray theJSONArray){
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

    public static JSONArray cloneJSONArray(JSONArray sourceArray){
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

    public static boolean JSONArrayContains(JSONArray theJSONArray,String checkString){
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

    public static boolean ListOfJSONArrayContains(ArrayList<JSONArray> theJSONArrays, JSONArray checkJSONArray){
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


}
