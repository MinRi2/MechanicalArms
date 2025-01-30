package mechanicalArms.entity.arms;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.world.*;

/**
 * @author minri2
 * Create by 2025/1/25
 */
public class ArmPart implements Cloneable{
    private static final Vec2 v1 = new Vec2();

    // find region.

    public float length = 3.5f * Vars.tilesize;

    /** unit: deg per second */
    public float rotateSpeed = 180f / 60;
    /** unit: deg */
    public float rotation = 0;
    /** unit: deg */
    protected float rotateTo = 0;
    public boolean rotating;

    // base ---arm--- joint
    public String armRegionName = "normal-arm", baseRegionName = "normal-base", jointRegionName = "normal-joint";
    private TextureRegion baseRegion, armRegion, jointRegion;

    public float armWidth = 0.8f * Vars.tilesize;

    private float lastRotation;

    protected Teamc entity;

    public ArmPart(){
    }

    public ArmPart(float length, float rotateSpeed){
        this.length = length;
        this.rotateSpeed = rotateSpeed;

        lastRotation = rotation;
    }

    public void loadRegion(Block block){
        baseRegion = Core.atlas.find(block.name + "-" + baseRegionName);
        armRegion = Core.atlas.find(block.name + "-" + armRegionName);
        jointRegion = jointRegionName != null ? Core.atlas.find(block.name + "-" + jointRegionName) : null;
    }

    public float getWorkRadius(){
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

    public void rotateTo(float to){
        to = Mathf.mod(to, 360f);
        if(Mathf.equal(rotateTo, to)) return;

        lastRotation = rotation;

        rotateTo = to;
        rotating = true;
    }

    public float rotateProgress(){
        return rotating ? (rotation - rotateTo) / (rotateTo - lastRotation) : 0;
    }

    public void setEntity(Teamc entity){
        this.entity = entity;
    }

    /**
     * @param x previous part joint x
     * @param y previous part joint y
     */
    public void draw(float x, float y){
        Vec2 v = getJointPoint();

        Draw.rect(armRegion, x + v.x / 2, y + v.y / 2, length, armWidth, rotation);
        Draw.rect(baseRegion, x, y, armWidth, armWidth, rotation);
        Draw.rect(jointRegion, x + v.x, y + v.y, armWidth, armWidth, rotation);
    }

    /**
     * @param x previous part joint x
     * @param y previous part joint y
     */
    public void update(float x, float y){
        if(rotating){
            rotation = Angles.moveToward(rotation, rotateTo, Time.delta * rotateSpeed);
            rotating = !Mathf.equal(rotation, rotateTo, 0.001f);
        }
    }

    public void write(Writes writes){
        writes.f(rotation);
        writes.f(rotateTo);
        writes.bool(rotating);
    }

    public void read(Reads reads){
        rotation = reads.f();
        rotateTo = reads.f();
        rotating = reads.bool();
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
