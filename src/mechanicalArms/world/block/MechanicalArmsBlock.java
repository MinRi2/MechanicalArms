package mechanicalArms.world.block;

import arc.graphics.g2d.*;
import arc.struct.*;
import arc.util.*;
import mechanicalArms.entity.arms.*;
import mechanicalArms.entity.arms.ArmsCommand.*;
import mindustry.content.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.*;
import mindustry.world.blocks.*;

/**
 * @author minri2
 * Create by 2025/1/25
 */
public class MechanicalArmsBlock extends Block{
    public final Seq<ArmPart> arms = new Seq<>();

    public MechanicalArmsBlock(String name){
        super(name);

        update = true;
        size = 3;
        health = 500;

        buildType = MechanicalArmsBuild::new;
    }

    public class MechanicalArmsBuild extends Building implements ControlBlock{
        private final Interval interval = new Interval();

        public ArmsController controller = new ArmsController();
        public BlockUnitUnit unit;

        @Override
        public void created(){
            super.created();

            controller.set(this, arms);
        }

        @Override
        public Unit unit(){
            if (unit == null) {
                unit = (BlockUnitUnit)UnitTypes.block.create(this.team);
                unit.tile(this);
            }

            return unit;
        }

        @Override
        public void draw(){
            super.draw();


            Draw.z(Layer.power - 0.1f);
            controller.draw();
        }

        @Override
        public void update(){
            super.update();

            if(unit != null && isControlled() && unit.isShooting){
                if(interval.get(60)){
                    controller.rotateTo(unit.aimX, unit.aimY);
                    controller.addCommand(new DumpCommand((ArmPicker)controller.worker));
                }
            }

            controller.update();
        }
    }

}
