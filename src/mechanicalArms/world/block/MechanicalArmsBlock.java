package mechanicalArms.world.block;

import arc.graphics.g2d.*;
import arc.struct.*;
import arc.util.*;
import mechanicalArms.entity.arms.*;
import mechanicalArms.entity.arms.ArmsCommand.*;
import mechanicalArms.logic.*;
import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.logic.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.*;
import mindustry.world.blocks.payloads.*;

import java.util.*;

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
        public double sense(Content content){
            if(content instanceof Item item && controller.worker instanceof ArmPicker picker){
                if(picker.itemStack.item == item){
                    return picker.itemStack.amount;
                }
            }
            return Double.NaN;
        }

        @Override
        public double sense(LAccess sensor){
            double result = Double.NaN;
            if(controller.worker instanceof ArmPicker picker){
                switch(sensor){
                    case itemCapacity -> result = picker.itemCapacity;
                    case totalItems -> result = picker.itemStack.amount;
                    case payloadCount -> result = picker.payload == null ? 0 : 1;
                }
            }
            return Double.isNaN(result) ? super.sense(sensor) : result;
        }

        @Override
        public Object senseObject(LAccess sensor){
            Object result = null;
            if(controller.worker instanceof ArmPicker picker){
                switch(sensor){
                    case payloadType -> {
                        if(picker.payload instanceof BuildPayload bp){
                            result = bp.build.block;
                        }else if(picker.payload instanceof UnitPayload up){
                            result = up.unit.type;
                        }
                    }
                    case firstItem -> result = picker.itemStack.item;
                }
            }
            return result == null ? super.senseObject(sensor) : result;
        }

        @Override
        public Unit unit(){
            if(unit == null){
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
