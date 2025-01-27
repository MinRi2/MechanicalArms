package mechanicalArms.entity.arms;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.blocks.payloads.*;

/**
 * @author minri2
 * Create by 2025/1/25
 */
public class ArmPicker extends ArmPart{
    public float radius = 1.5f * Vars.tilesize;
    public float fraction = 0.5f;
    
    public Payload payload;

    public ItemStack itemStack = new ItemStack();
    public int itemCapacity = 60;
    
    public float itemTime = 0;

    public ArmPicker(){
    }

    public ArmPicker(float length, float rotateSpeed, float radius, float fraction, int itemCapacity){
        super(length, rotateSpeed);
        this.radius = radius;
        this.fraction = fraction;
        this.itemCapacity = itemCapacity;
    }

    public void pickupItem(float wx, float wy, Item item){
        if(item != itemStack.item && itemStack.amount != 0) return;

        Building build = Vars.world.buildWorld(wx, wy);

        if(build == null || !build.items.has(item)) return;

        int spareAmount = itemCapacity - itemStack.amount;
        int amount = Math.min(build.items.get(item), spareAmount);

        build.items.remove(item, amount);
        itemStack.set(item, itemStack.amount + amount);

        Fx.itemTransfer.at(wx, wy, amount, item.color, entity);
    }

    public void pickupBuild(float x, float y){

    }

    public void pickupUnit(float x, float y){

    }

    public void dump(float wx, float wy){
        if(itemStack.amount == 0){
            return;
        }

        Building build = Vars.world.buildWorld(wx, wy);

        if(build != null && build.block.hasItems){
            Item item = itemStack.item;
            int acceptAmount = build.acceptStack(item, itemStack.amount, null);

            if(acceptAmount == 0) return;

            build.handleStack(item, acceptAmount, entity);
            itemStack.set(item, itemStack.amount - acceptAmount);

            Fx.itemTransfer.at(wx, wy, acceptAmount, item.color, build);
        }
    }

    @Override
    protected float getWorkRadius(){
        return length + radius;
    }

    @Override
    public void draw(float x, float y){
        super.draw(x, y);

        Vec2 workPoint = getWorkPoint(x, y);
        float wx = workPoint.x, wy = workPoint.y;

        Lines.stroke(1.5f);
        Lines.arc(wx, wy, radius, fraction, 90 + rotation);
        Draw.reset();

        Item item = itemStack.item;
        int amount = itemStack.amount;

        if(itemTime > 0.01f){
            float sin = Mathf.absin(Time.time, 5f, 1f/4);
            float colorTime = Mathf.absin(Time.time, 8f, 0.75f);

            float wrapSize = radius * itemTime;
            float itemSize = wrapSize * (2f / 3 + sin) * itemTime;

            Draw.mixcol(Pal.accent, sin * 0.1f);
            Draw.rect(item.fullIcon, wx, wy, itemSize, itemSize);
            Draw.mixcol();

            Fill.light(wx, wy, Lines.circleVertices(wrapSize), wrapSize,
                Color.clear,
                Tmp.c2.set(entity.team().color).lerp(Color.white, colorTime).a(0.7f));

            if(!Vars.renderer.pixelator.enabled()){
                int displayAmount = Mathf.round(amount * itemTime);
                Fonts.outline.draw(displayAmount + "", wx, wy - 5f, Pal.accent, 0.25f * itemTime / Scl.scl(1f), false, Align.center);
            }

            Draw.reset();
        }
    }

    @Override
    public void update(){
        super.update();

        itemTime = Mathf.lerpDelta(itemTime, Mathf.num(itemStack.amount > 0), 0.1f);
    }

    @Override
    public ArmPart clone(){
        ArmPicker picker = (ArmPicker)super.clone();
        picker.itemStack = new ItemStack();
        return picker;
    }
}
