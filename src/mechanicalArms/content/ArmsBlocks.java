package mechanicalArms.content;

import mechanicalArms.entity.arms.*;
import mechanicalArms.world.block.*;
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
                rotateSpeed = 160f / 60;
            }},
            new ArmPicker(){{
                rotation = 30;
                rotateSpeed = 130f / 60;
            }}
            );

            setupRequirements(Category.distribution, ItemStack.with(Items.copper, 2000, Items.lead, 1500, Items.silicon, 750));
        }};
    }
}
