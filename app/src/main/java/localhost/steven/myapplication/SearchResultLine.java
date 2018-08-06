package localhost.steven.myapplication;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by 10084 on 4/10/2018.
 */

public class SearchResultLine {
    public SearchResultLine(Context context,int pos, RelativeLayout theLayout, String theMainText){
        lineParam=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 100);
        position=pos;
        int offset;
        offset=pos*100+150;
        lineParam.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        lineParam.setMargins(0, offset, 0, 0);

        gd = new GradientDrawable();
        gd.setColor(0x00000000); // Changes this drawbale to use a single color instead of a gradient
        gd.setCornerRadius(1);
        gd.setStroke(1, 0xFF000000);
        //gd.setStroke(1,0x000000);
        //gd.setSize(RelativeLayout.LayoutParams.MATCH_PARENT,150);
        //gd.setSize(RelativeLayout.LayoutParams.WRAP_CONTENT,150);

        mainText = new TextView(context);
        mainText.setTextSize(12);
        mainText.setLayoutParams(lineParam);
        mainText.setText(theMainText);
        mainText.setBackground(gd);

        theLayout.addView(mainText);
    }
    int position;
    TextView mainText;
    RelativeLayout.LayoutParams lineParam;
    GradientDrawable gd;
}
