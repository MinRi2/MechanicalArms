package mechanicalArms;

import mechanicalArms.content.*;
import mechanicalArms.logic.*;
import mechanicalArms.logic.ArmsLStatements.*;
import mindustry.mod.*;

/**
 * @author minri2
 * Create by 2025/1/25
 */
public class Main extends Mod{

    @Override
    public void init(){
        super.init();

        ArmsLogic.init();
    }

    @Override
    public void loadContent(){
        super.loadContent();

        ArmsBlocks.load();
    }
}
