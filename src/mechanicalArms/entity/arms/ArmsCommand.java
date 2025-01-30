package mechanicalArms.entity.arms;

import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mechanicalArms.logic.*;
import mechanicalArms.math.*;
import mindustry.gen.*;
import mindustry.type.*;

/**
 * @author minri2
 * Create by 2025/1/26
 */
public abstract class ArmsCommand{
    public boolean updated;
    public boolean released;

    /* Only run one time then only check whether finished. */
    protected boolean updateOnce;

    /**
     * @param x work x
     * @param y work y
     */
    public abstract void update(float x, float y, Teamc entity);

    public boolean finished(){
        return updated;
    }

    public static class CommandSequence extends ArmsCommand{
        public final Seq<ArmsCommand> commands;
        public ArmsCommand currentCommand;

        public CommandSequence(Seq<ArmsCommand> commands, ArmsCommand currentCommand){
            this.commands = commands;
            this.currentCommand = currentCommand;
        }

        @Override
        public void update(float x, float y, Teamc entity){
            if(currentCommand == null){
                if(commands.isEmpty()) return;

                currentCommand = commands.remove(0);
            }

            if(!currentCommand.updateOnce || !currentCommand.updated){
                currentCommand.update(x, y, entity);
            }

            if(currentCommand.finished()){
                currentCommand = null;
            }
        }

        @Override
        public boolean finished(){
            return commands.isEmpty();
        }
    }

    public static class RotateCommand extends ArmsCommand{
        public static final Seq<Vec2> tmpVec = new Seq<>();
        public static final float tolerance = 0.001f;
        public static final int maxIteration = 100;

        public Seq<ArmPart> armParts;
        public float destX, destY;

        private float[] destAngles;

        public RotateCommand(Seq<ArmPart> armParts, float destX, float destY){
            this.armParts = armParts;
            this.destX = destX;
            this.destY = destY;
        }

        @Override
        public void update(float wx, float wy, Teamc entity){
            if(destAngles == null){
                destAngles = new float[armParts.size];

                calculateAngles(entity);
            }

            for(int i = 0; i < armParts.size; i++){
                armParts.get(i).rotateTo(destAngles[i]);
            }
        }

        private void calculateAngles(Teamc entity){
            float x = entity.getX(), y = entity.getY();

            if(armParts.size == 2){
                ArmPart arm1 = armParts.get(0), arm2 = armParts.get(1);

                float r1 = arm1.getWorkRadius(), r2 = arm2.getWorkRadius();

                Vec2 p1 = Tmp.v1, p2 = Tmp.v2;
                int pointCount = ArmsGeometry.getCircleInspectedPoints(x, y, r1, destX, destY, r2, p1, p2);

                float angle1, angle2;
                if(pointCount != 2){
                    angle1 = angle2 = Angles.angle(x, y, destX, destY);
                }else{
                    Vec2 p = p1;

                    Vec2 dv1 = Tmp.v3.set(x, y).sub(p1),
                    dv2 = Tmp.v4.set(x, y).sub(p2);

                    Vec2 armv1 = arm1.getWorkPoint(), armv2 = arm2.getWorkPoint();

                    float pcos1 = armv1.dot(dv1) + armv2.dot(dv1);
                    float pcos2 = armv2.dot(dv2) + armv2.dot(dv2);
                    if(pcos2 > pcos1){
                        p = p2;
                    }

                    angle1 = Angles.angle(x, y, p.x, p.y);
                    angle2 = Angles.angle(p.x, p.y, destX, destY);
                }

                destAngles[0] = angle1;
                destAngles[1] = angle2;
            }else{
                Vec2 jointPos = Tmp.v1.setZero();
                for(ArmPart arm : armParts){
                    jointPos.add(arm.getJointPoint());
                    tmpVec.add(jointPos.cpy());
                }

                ArmsInverseKinematics.solveFabrik(tmpVec, Tmp.v1.setZero(), Tmp.v2.set(destX, destY).sub(x, y), tolerance, maxIteration);

                Vec2 last = Tmp.v2.setZero();
                for(int i = 0; i < tmpVec.size; i++){
                    Vec2 v = tmpVec.get(i);
                    destAngles[i] = Tmp.v3.set(v).sub(last).angle();
                    last = v;
                }

                tmpVec.clear();
            }
        }

        @Override
        public boolean finished(){
            if(!super.finished()) return false;

            for(int i = 0; i < armParts.size; i++){
                if(!Mathf.equal(armParts.get(i).rotation, destAngles[i], 0.001f)){
                    return false;
                }
            }
            return true;
        }

        @Override
        public String toString(){
            return "RotateCommand{" +
            "destX=" + destX +
            ", destY=" + destY +
            '}';
        }
    }

    public static class PickerCommand extends ArmsCommand{
        public ArmPicker picker;
        public ArmsPickupType type;
        public Item item;

        public PickerCommand(ArmPicker picker, ArmsPickupType type, Item item){
            this.picker = picker;
            this.type = type;
            this.item = item;

            updateOnce = true;
        }

        @Override
        public void update(float x, float y, Teamc entity){
            switch(type){
                case item -> picker.pickupItem(x, y, item);
                case build -> picker.pickupBuild(x, y);
                case unit -> picker.pickupUnit(x, y);
            }
        }

        @Override
        public String toString(){
            return "PickerCommand{" +
            "item=" + item +
            ", type=" + type +
            '}';
        }
    }

    public static class DumpCommand extends ArmsCommand{
        public ArmPicker picker;

        public DumpCommand(ArmPicker picker){
            this.picker = picker;

            updateOnce = true;
        }

        @Override
        public void update(float x, float y, Teamc entity){
            if(picker.employed()){
                picker.dump(x, y);
            }
        }

        @Override
        public String toString(){
            return "DumpCommand{}";
        }
    }
}
