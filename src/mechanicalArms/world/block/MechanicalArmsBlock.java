package mechanicalArms.world.block;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mechanicalArms.entity.arms.*;
import mechanicalArms.entity.arms.ArmsCommand.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.logic.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.*;
import mindustry.world.blocks.payloads.*;
import mindustry.world.meta.*;

/**
 * @author minri2
 * Create by 2025/1/25
 */
public class MechanicalArmsBlock extends Block{
    public final Seq<ArmPart> arms = new Seq<>();

    public MechanicalArmsBlock(String name){
        super(name);

        update = true;
        solid = true;
        size = 3;
        health = 500;

        group = BlockGroup.transportation;
        buildType = MechanicalArmsBuild::new;
    }

    @Override
    public void load(){
        super.load();

        for(ArmPart arm : arms){
            arm.loadRegion(this);
        }
    }

    private void drawWorkRange(float x, float y, Color color){
        float radius = arms.sumf(ArmPart::getWorkRadius);

        Fill.light(x, y, Lines.circleVertices(radius), radius,
        Color.clear, Tmp.c1.set(color).a(0.15f));

        Drawf.dashCircle(x, y, radius, color);
    }

    private void drawArms(float x, float y){
        Vec2 drawPos = Tmp.v1.set(x, y);

        for(ArmPart arm : arms){
            arm.draw(drawPos.x, drawPos.y);
            drawPos.add(arm.getJointPoint());
        }
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        super.drawPlace(x, y, rotation, valid);

        drawArms(x * Vars.tilesize, y * Vars.tilesize);
        drawWorkRange(x * Vars.tilesize, y * Vars.tilesize, Vars.player.team().color);
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        super.drawPlanRegion(plan, list);

        drawArms(plan.x * Vars.tilesize, plan.y * Vars.tilesize);
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
        public boolean acceptItem(Building source, Item item){
            return controller.worker instanceof ArmPicker picker && picker.canPickupItem(item);
        }

        @Override
        public void handleItem(Building source, Item item){
            if(controller.worker instanceof ArmPicker picker){
                picker.addItem(item, 1);
            }
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

            Draw.z(Layer.power - 1f);
            controller.draw();
        }

        @Override
        public void drawSelect(){
            super.drawSelect();

            drawWorkRange(x, y, team.color);
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

        @Override
        public void write(Writes write){
            super.write(write);
            controller.write(write);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            controller.read(read);
        }
    }

}
