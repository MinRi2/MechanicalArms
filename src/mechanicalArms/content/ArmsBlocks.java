package mechanicalArms.content;

import mechanicalArms.entity.arms.*;
import mechanicalArms.world.block.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.type.*;
import mindustry.world.*;

/**
 * @author minri2
 * Create by 2025/1/25
 */
public class ArmsBlocks{
    public static Block picker;

    public static void load(){
        picker = new MechanicalArmsBlock("picker"){{
            arms.addAll(
            new ArmPart(){{
                length = 4.5f * Vars.tilesize;
                rotateSpeed = 210f / 60;
            }},
            new ArmPart(){{
                length = 4.5f * Vars.tilesize;
                rotateSpeed = 210f / 60;
            }},
            new ArmPart(){{
                length = 4.5f * Vars.tilesize;
                rotateSpeed = 210f / 60;
            }},
            new ArmPicker(){{
                length = 3.5f * Vars.tilesize;
                rotateSpeed = 190f / 60;
                rotation = 30;

                armWidth *= 0.8f;
            }}
            );

            setupRequirements(Category.distribution, ItemStack.with(Items.copper, 2000, Items.lead, 1500, Items.silicon, 750));
        }};
    }
}
