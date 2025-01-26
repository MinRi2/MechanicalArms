package mechanicalArms.logic;

import arc.graphics.*;
import mechanicalArms.*;
import mindustry.logic.*;

/**
 * @author minri2
 * Create by 2025/1/25
 */
public class ArmsLogic{
    public static LCategory armsCategory;

    public static void init(){
        armsCategory = new LCategory(ArmsVars.armsToken, Color.valueOf("#6A80B9"));

        ArmsLStatements.register();
    }
}
