package mechanicalArms.entity.arms;

import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mechanicalArms.entity.arms.ArmsCommand.*;
import mindustry.gen.*;

/**
 * @author minri2
 * Create by 2025/1/25
 */
public class ArmsController{
    // Test Only
    private static final Interval interval = new Interval();

    private static final Vec2 v1 = new Vec2();

    protected Seq<ArmPart> arms;

    public float x;
    public float y;

    public ArmPart rotator;
    public ArmPart worker;

    protected Teamc entity;

    private float lastRotateX;
    private float lastRotateY;

    private final Queue<ArmsCommand> commands = new Queue<>();
    private ArmsCommand currentCommand;

    public void set(Teamc entity, Seq<ArmPart> seq){
        this.entity = entity;

        x = entity.getX();
        y = entity.getY();

        arms = seq.map(ArmPart::clone);

        rotator = arms.get(0);
        worker = arms.peek();

        for(ArmPart arm : arms){
            arm.setEntity(entity);
        }
    }

    public boolean addCommand(ArmsCommand command){
        if(checkCommand(command)){
            commands.addLast(command);
            return true;
        }
        return false;
    }

    private boolean checkCommand(ArmsCommand command){
        if(command instanceof RotateCommand rotc){
            if(!Mathf.equal(lastRotateX, rotc.destX) || !Mathf.equal(lastRotateY, rotc.destY)){
                lastRotateX = rotc.destX;
                lastRotateY = rotc.destY;
                return true;
            }

            return false;
        }
        return true;
    }

    public RotateCommand rotateTo(float rx, float ry){
        RotateCommand command = new RotateCommand(arms, rx, ry);
        return addCommand(command) ? command : null;
    }

    public void draw(){
        Vec2 drawPos = v1.set(x, y);

        for(ArmPart arm : arms){
            arm.draw(drawPos.x, drawPos.y);
            drawPos.add(arm.getJointPoint());
        }
    }

    public void update(){
        x = entity.getX();
        y = entity.getY();

        updateCommand();

        Vec2 jointPos = v1.set(x, y);
        for(ArmPart arm : arms){
            arm.update(jointPos.x, jointPos.y);
            jointPos.add(arm.getJointPoint());
        }
    }

    private void updateCommand(){
        if(currentCommand == null){
            if(commands.isEmpty()) return;

            currentCommand = commands.removeFirst();
        }

        if(!currentCommand.updateOnce || !currentCommand.updated){
            Vec2 workPoint = v1.set(x, y);
            for(ArmPart arm : arms){
                workPoint.add(arm.getWorkPoint());
            }

            currentCommand.update(workPoint.x, workPoint.y, entity);
            currentCommand.updated = true;
        }

        if(currentCommand.finished()){
            currentCommand.released = true;
            currentCommand = null;
        }
    }
}
