package mechanicalArms.logic;

import arc.util.*;
import mechanicalArms.entity.arms.*;
import mechanicalArms.entity.arms.ArmsCommand.*;
import mechanicalArms.world.block.MechanicalArmsBlock.*;
import mindustry.*;
import mindustry.logic.*;
import mindustry.logic.LExecutor.*;
import mindustry.type.*;

/**
 * @author minri2
 * Create by 2025/1/26
 */
public class ArmsLInstructions{

    public static abstract class ArmsLControlInstruction implements LInstruction{
        public LVar picker;
        public boolean wait;
        public @Nullable LVar finishedOut;

        protected ArmsCommand command;

        public ArmsLControlInstruction set(LVar picker, boolean wait, LVar finishedOut){
            this.picker = picker;
            this.wait = wait;
            this.finishedOut = finishedOut;
            return this;
        }

        @Override
        public final void run(LExecutor exec){
            if(command != null){
                boolean finished = command.released; // wait for released

                if(finishedOut != null){
                    finishedOut.setbool(finished);
                }

                if(finished){
                    command = null;
                }else if(wait){
                    exec.counter.numval--;
                    exec.yield = true;
                }
                return;
            }

            if(finishedOut != null){
                finishedOut.setbool(false);
            }

            execute(exec);

            if(finishedOut != null && command == null){
                finishedOut.setbool(true);
            }

            if(wait && command != null){
                exec.counter.numval--;
                exec.yield = true;
            }
        }

        public abstract void execute(LExecutor exec);

    }

    public static class PickupInstruction extends ArmsLControlInstruction{
        public ArmsPickupType type;
        public LVar p1;

        public PickupInstruction(ArmsPickupType type, LVar p1){
            this.type = type;
            this.p1 = p1;
        }

        @Override
        public void execute(LExecutor exec){
            Object obj = picker.obj();
            if(!(obj instanceof MechanicalArmsBuild pickerBuild)) return;

            ArmsController controller = pickerBuild.controller;
            if(!(controller.worker instanceof ArmPicker armPicker)) return;

            switch(type){
                case item -> {
                    if(p1.obj() instanceof Item item){
                        command = new PickerCommand(armPicker, type, item);
                    }
                }
                case build, unit -> command = new PickerCommand(armPicker, type, null);
            }

            controller.addCommand(command);
        }
    }

    public static class RotateInstruction extends ArmsLControlInstruction{
        public LVar x, y;

        public RotateInstruction(LVar x, LVar y){
            this.x = x;
            this.y = y;
        }

        @Override
        public void execute(LExecutor exec){
            if(!(picker.obj() instanceof MechanicalArmsBuild pickerBuild)) return;

            float rx = x.numi() * Vars.tilesize;
            float ry = y.numi() * Vars.tilesize;

            command = pickerBuild.controller.rotateTo(rx, ry);
        }
    }

    public static class DumpInstruction extends ArmsLControlInstruction{

        @Override
        public void execute(LExecutor exec){
            Object obj = picker.obj();
            if(!(obj instanceof MechanicalArmsBuild pickerBuild)) return;

            ArmsController controller = pickerBuild.controller;
            if(!(controller.worker instanceof ArmPicker armPicker)) return;

            command = new DumpCommand(armPicker);
            controller.addCommand(command);
        }
    }
}
