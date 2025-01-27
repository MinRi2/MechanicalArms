package mechanicalArms.entity.arms;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.*;
import mindustry.gen.*;

/**
 * @author minri2
 * Create by 2025/1/25
 */
public class ArmPart implements Cloneable{
    private static final Vec2 v1 = new Vec2();

    public float length = 4.5f * Vars.tilesize;
    /** unit: deg per second */
    public float rotateSpeed = 180f / 60;
    /** unit: deg */
    public float rotation = 0;
    /** unit: deg */
    protected float rotateTo = 0;
    public boolean rotating;

    protected Teamc entity;

    public ArmPart(){
    }

    public ArmPart(float length, float rotateSpeed){
        this.length = length;
        this.rotateSpeed = rotateSpeed;
    }

    protected float getWorkRadius(){
        return length;
    }

    public Vec2 getJointPoint(){
        return v1.trns(rotation, length);
    }

    public Vec2 getWorkPoint(){
        return v1.trns(rotation, getWorkRadius());
    }

    public Vec2 getJointPoint(float x, float y){
        return v1.trns(rotation, length).add(x, y);
    }

    public Vec2 getWorkPoint(float x, float y){
        return v1.trns(rotation, getWorkRadius()).add(x, y);
    }

    public void rotateTo(float rotation){
        if(Mathf.equal(rotateTo, rotation)) return;

        rotateTo = rotation;
        rotating = true;
    }

    public void setEntity(Teamc entity){
        this.entity = entity;
    }

    /**
     * @param x previous part joint x
     * @param y previous part joint y
     */
    public void draw(float x, float y){
        Vec2 v = getJointPoint(x, y);

        // draw arm
        Lines.stroke(1.5f);
        Lines.line(x, y, v.x, v.y);

        Draw.reset();
    }

    /**
     * @param x previous part joint x
     * @param y previous part joint y
     */
    public void update(float x, float y){
        if(rotating){
            rotation = Angles.moveToward(rotation, rotateTo, Time.delta * rotateSpeed);
            rotating = !Mathf.equal(rotation, rotateTo);
        }
    }

    @Override
    public String toString(){
        return "ArmPart{" +
        "rotation=" + rotation +
        ", rotateTo=" + rotateTo +
        ", rotating=" + rotating +
        '}';
    }

    @Override
    public ArmPart clone(){
        try{
            return (ArmPart)super.clone();
        }catch(CloneNotSupportedException e){
            throw new AssertionError();
        }
    }
}
