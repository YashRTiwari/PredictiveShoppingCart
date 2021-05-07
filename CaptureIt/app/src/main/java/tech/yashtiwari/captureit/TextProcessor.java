package tech.yashtiwari.captureit;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Comparator.comparing;

public class TextProcessor {

    public static class P{

        private String text;
        private int top;
        P(String text){
            this.text = text;
        }
        P(String text, int top){
            this.text = text;
            this.top = top;
        }

        public String getText() {
            return text;
        }

        public int getTop() {
            return top;
        }


    }



    @RequiresApi(api = Build.VERSION_CODES.N)
    public static List<P> processBill(FirebaseVisionText billImage){

        List<P> ps = new ArrayList<>();

        List<FirebaseVisionText.TextBlock> blocks = billImage.getTextBlocks();
        if (blocks.size() == 0) {
            Log.d("TextProcessor", "getWords: null");
            return null;
        }

        int itemCount = 0;
        for (int i = 0; i < blocks.size(); i++) {
            List<FirebaseVisionText.Line> lines = blocks.get(i).getLines();

            for (int j = 0; j < lines.size(); j++) {
                List<FirebaseVisionText.Element> elements = lines.get(j).getElements();

//                if (lines.get(j).getText().length() <=2)
//                    continue;

                P p = new P(lines.get(j).getText());
                ps.add(p);
                if (lines.get(j).getText().startsWith("#")){
                    String [] z = lines.get(j).getText().split(" ");
                    itemCount = Integer.parseInt(z[z.length -1]);
                }
            }
        }

        Collections.sort(ps, comparing(P::getTop));

        for (P p : ps){
            Log.d("TSD", p.getText());
        }

        BillModal modal = new BillModal();
        modal.setShop(ps.get(0).getText());
        modal.setShopAddress(ps.get(1).getText()+"\n"+ps.get(2).getText());
        modal.setShopContact(ps.get(3).getText());
        //String dtime = ps.get(4).getText().split(" ")[2]+ps.get(4).getText().split(" ")[3];
        //modal.setDateTime(dtime);
        List<P> pList = new ArrayList<>();
        for (int x = 5; x<=5+itemCount; x++){
            if (ps.get(x).getText().contains("Discount")){
                continue;
            }

            String pr = ps.get(x).getText().replaceAll("[0-9]","");
            P prs = new P(pr);

            pList.add(prs);
        }

        return pList;
    }

}
