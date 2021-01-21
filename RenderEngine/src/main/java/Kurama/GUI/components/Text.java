package Kurama.GUI.components;

import Kurama.GUI.automations.CheckForTextUpdate;
import Kurama.GUI.constraints.PosXYTopLeftAttachPix;
import Kurama.font.FontTexture;
import Kurama.utils.Utils;

import java.util.ArrayList;

public class Text extends Component {

    public String text = "";
    public FontTexture fontTexture;
    public boolean shouldUpdate = true;

    public Text(Component parent, FontTexture fontTexture, String identifier) {
        super(parent, identifier);
        this.fontTexture = fontTexture;
        isContainerVisible = false;
        addAutomation(new CheckForTextUpdate(this));
    }

    public Component setFontTexture(FontTexture fontTexture) {
        this.fontTexture = fontTexture;
        shouldUpdate = true;
        return this;
    }

    public Component setText(String text) {
        this.text = text;
        shouldUpdate = true;
        return this;
    }

    public void updateText() {

        if(text == null || text.length() == 0) {
            return;
        }

        children = new ArrayList<>();

        int curPos = 0;
        this.width = 0;

        for(char c: text.toCharArray()) {
            var charInfo = fontTexture.charMap.get(c);
            var newComp = new Rectangle(this, Utils.getUniqueID());

            newComp.constraints.add(new PosXYTopLeftAttachPix(curPos, 0));

            if(charInfo != null) {
                newComp.texUL = charInfo.texUL;
                newComp.texBL = charInfo.texBL;
                newComp.texUR = charInfo.texUR;
                newComp.texBR = charInfo.texBR;
                newComp.width = charInfo.width;
                newComp.height = fontTexture.height;
                newComp.texture = fontTexture.texture;

                newComp.setOverlayColor(this.overlayColor);
                curPos += newComp.width;
                width += newComp.width;
                children.add(newComp);
            }
            // Check for special characters
            else if((int)c == 32) {
                newComp.width = fontTexture.charMap.get('O').width;
                newComp.height = fontTexture.height;
                newComp.isContainerVisible = false;

                newComp.setOverlayColor(this.overlayColor);
                curPos += newComp.width;
                width += newComp.width;
                children.add(newComp);
            }

            else {
//                newComp.width = fontTexture.charMap.get('a').width;
//                newComp.height = fontTexture.height;
//                newComp.isContainerVisible = false;
            }

        }

        this.height = fontTexture.height;
        shouldUpdate = false;
    }

}
