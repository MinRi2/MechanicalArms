package mechanicalArms.entity.arms;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.core.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.io.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.payloads.*;

import static mindustry.Vars.state;

/**
 * @author minri2
 * Create by 2025/1/25
 */
public class ArmPicker extends ArmPart{
    public float radius = 1.5f * Vars.tilesize;
    public float fraction = 0.5f;
    public float payloadCapacity = radius * radius;

    public Payload payload;

    public ItemStack itemStack = new ItemStack();
    public int itemCapacity = 60;

    public float itemTime = 0;

    // TODO: better picker arm.
    public Color color = Color.valueOf("#d3dee6");
    public Color backColor = Color.valueOf("#b0b9c0");

    public ArmPicker(){
        super();
    }

    public ArmPicker(float length, float rotateSpeed, float radius, float fraction, int itemCapacity){
        super(length, rotateSpeed);
        this.radius = radius;
        this.fraction = fraction;
        this.itemCapacity = itemCapacity;
    }

    public boolean employed(){
        return payload != null || itemStack.amount != 0;
    }

    public void pickupItem(float wx, float wy, Item item){
        if(!canPickupItem(item) || payload != null) return;

        Building build = Vars.world.buildWorld(wx, wy);

        if(build == null || !build.block.hasItems || !build.items.has(item)) return;

        int addAmount = addItem(item, build.items.get(item));

        build.items.remove(item, addAmount);
        itemStack.set(item, itemStack.amount + addAmount);

        Fx.itemTransfer.at(wx, wy, addAmount, item.color, entity);
    }

    public void pickupBuild(float x, float y){
        if(itemStack.amount != 0 || payload != null) return;

        Building build = Vars.world.buildWorld(x, y);
        if(build == null) return;

        if(build.getPayload() != null && state.teams.canInteract(entity.team(), build.team)){
            //pick up block's payload
            Payload current = build.getPayload();
            if(current != null && canPickup(current)){
                Payload taken = build.takePayload();
                if(taken != null){
                    payload = taken;
                    Fx.unitPickup.at(build);
                }
            }
        }else if(canPickup(build)){
            build.pickedUp();
            build.remove();
            build.afterPickedUp();
            payload = new BuildPayload(build);
            Fx.unitPickup.at(build);
        }
    }

    public void pickupUnit(float x, float y){
        if(itemStack.amount != 0 || payload != null) return;

        Unit unit = Units.closest(entity.team(), x, y, radius, this::canPickup);
        if(unit == null) return;

        if(unit.isAdded()){
            unit.team.data().updateCount(unit.type, 1);
        }

        unit.remove();
        payload = new UnitPayload(unit);
        Fx.unitPickup.at(unit);
        if(Vars.net.client()){
            Vars.netClient.clearRemovedEntity(unit.id);
        }
    }

    public void dump(float wx, float wy){
        if(itemStack.amount != 0){
            Building build = Vars.world.buildWorld(wx, wy);

            if(build != null && build.block.hasItems){
                Item item = itemStack.item;
                int acceptAmount = build.acceptStack(item, itemStack.amount, null);

                if(acceptAmount == 0) return;

                build.handleStack(item, acceptAmount, entity);
                itemStack.set(item, itemStack.amount - acceptAmount);

                Fx.itemTransfer.at(wx, wy, acceptAmount, item.color, build);
            }
        }else if(payload != null){
            Tile tile = Vars.world.tileWorld(wx, wy);

            if(tile == null) return;

            boolean dropped = false;

            // code from InputHandler
            if(tile.build != null && tile.build.team == entity.team() && tile.build.acceptPayload(tile.build, payload)){
                Fx.unitDrop.at(tile.build);
                tile.build.handlePayload(tile.build, payload);
                dropped = true;
            }else if(payload instanceof BuildPayload b){
                Building build = b.build;
                int tx = World.toTile(wx - build.block.offset);
                int ty = World.toTile(wy - build.block.offset);
                if (Build.validPlace(build.block, build.team, tx, ty, build.rotation, false)) {
                    b.place(tile, build.rotation);

                    Fx.unitDrop.at(build);
                    build.block.placeEffect.at(tile.drawx(), tile.drawy(), tile.block().size);
                    dropped = true;
                }
            }else if(payload instanceof UnitPayload p){
                Unit u = p.unit;

                if(Vars.net.client()){
                    Vars.netClient.clearRemovedEntity(u.id);
                }

                if(u.canPass(tile.x, tile.y) && Units.count(wx, wy, u.physicSize(), o -> o.isGrounded() && o.hitSize > 14.0f) <= 1){
                    Fx.unitDrop.at(wx, wy);
                    u.set(wx, wy);
                    u.rotation(rotation);
                    u.id = EntityGroup.nextId();
                    if(!u.isAdded()){
                        u.team.data().updateCount(u.type, -1);
                    }

                    u.add();
                    u.unloaded();
                    dropped = true;
                }
            }

            if(dropped){
                payload = null;
            }
        }
    }

    public int addItem(Item item, int amount){
        if(canPickupItem(item)){
            int spareAmount = itemCapacity - itemStack.amount;
            int addAmount = Math.min(amount, spareAmount);

            itemStack.set(item, itemStack.amount + addAmount);
            return addAmount;
        }
        return 0;
    }

    public boolean canPickupItem(Item item){
        return item != itemStack.item ? itemStack.amount == 0 : itemStack.amount < itemCapacity;
    }

    public boolean canPickup(Building building){
        return building.canPickup() && building.hitSize() * building.hitSize() < payloadCapacity;
    }

    public boolean canPickup(Unit unit){
        return unit.isAI() && unit.isGrounded() && unit.hitSize() * unit.hitSize() < payloadCapacity;
    }

    public boolean canPickup(Payload payload){
        return payload instanceof BuildPayload buildPayload ? canPickup(buildPayload.build) :
        payload instanceof UnitPayload unitPayload && canPickup(unitPayload.unit);
    }

    @Override
    public float getWorkRadius(){
        return length + radius;
    }

    @Override
    public void draw(float x, float y){
        super.draw(x, y);

        Vec2 workPoint = getWorkPoint(x, y);
        float wx = workPoint.x, wy = workPoint.y;

        Item item = itemStack.item;
        int amount = itemStack.amount;

        if(itemTime > 0.01f){
            float sin = Mathf.absin(Time.time, 5f, 1f / 4);
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
        }

        if(payload != null){
            float colorTime = Mathf.absin(Time.time, 8f, 0.75f);
            float wrapSize = radius;

            payload.draw();

            Fill.light(wx, wy, Lines.circleVertices(wrapSize), wrapSize,
            Color.clear,
            Tmp.c2.set(entity.team().color).lerp(Color.white, colorTime).a(0.7f));
        }

        Lines.stroke(2f, backColor);
        Lines.arc(wx, wy, radius, fraction, 90 + rotation);
        Lines.stroke(0.7f, color);
        Lines.arc(wx, wy, radius, fraction, 90 + rotation);
        Lines.stroke(1f);
    }

    @Override
    public void update(float x, float y){
        super.update(x, y);

        Vec2 workPoint = getWorkPoint(x, y);

        itemTime = Mathf.lerpDelta(itemTime, Mathf.num(itemStack.amount > 0), 0.1f);

        if(payload != null){
            payload.set(workPoint.x, workPoint.y, rotation);
        }
    }

    @Override
    public void write(Writes writes){
        super.write(writes);

        boolean hasItem = itemStack.amount != 0;
        boolean hasPayload = payload != null;

        writes.f(itemTime);

        writes.bool(hasItem);
        writes.bool(hasPayload);

        if(hasItem){
            TypeIO.writeItems(writes, itemStack);
        }

        if(hasPayload){
            TypeIO.writePayload(writes, payload);
        }
    }

    @Override
    public void read(Reads reads){
        super.read(reads);

        itemTime = reads.f();

        boolean hasItem = reads.bool();
        boolean hasPayload = reads.bool();

        if(hasItem){
            TypeIO.readItems(reads, itemStack);
        }

        if(hasPayload){
            payload = TypeIO.readPayload(reads);
        }
    }

    @Override
    public ArmPart clone(){
        ArmPicker picker = (ArmPicker)super.clone();
        picker.itemStack = new ItemStack();
        picker.payload = null;
        return picker;
    }
}
