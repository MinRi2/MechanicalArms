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
        public int picker;
        public boolean wait;
        public int finishedOut;

        protected ArmsCommand command;

        public ArmsLControlInstruction set(int picker, boolean wait, int finishedOut){
            this.picker = picker;
            this.wait = wait;
            this.finishedOut = finishedOut;
            return this;
        }

        @Override
        public final void run(LExecutor exec){
            if(command != null){
                boolean finished = command.released; // wait for released
                if(finishedOut != -1){
                    exec.setbool(finishedOut, finished);
                }

                if(finished){
                    command = null;
                }else if(wait){
                    exec.counter.numval--;
                }
                return;
            }

            if(finishedOut != -1){
                exec.setbool(finishedOut, false);
            }

            execute(exec);

            if(finishedOut != -1 && command == null){
                exec.setbool(finishedOut, true);
            }

            if(wait && command != null){
                exec.counter.numval--;
            }
        }

        public abstract void execute(LExecutor exec);

    }

    public static class PickupInstruction extends ArmsLControlInstruction{
        public ArmsPickupType type;
        public int p1;

        protected ArmsCommand command;

        public PickupInstruction(ArmsPickupType type, int p1){
            this.type = type;
            this.p1 = p1;
        }

        @Override
        public void execute(LExecutor exec){
            Object obj = exec.obj(picker);
            if(!(obj instanceof MechanicalArmsBuild pickerBuild)) return;

            ArmsController controller = pickerBuild.controller;
            if(!(controller.worker instanceof ArmPicker armPicker)) return;

            switch(type){
                case item -> {
                    if(exec.obj(p1) instanceof Item item){
                        command = new PickerCommand(armPicker, type, item);
                    }
                }
                case build, unit -> command = new PickerCommand(armPicker, type, null);
            }

            controller.addCommand(command);
        }
    }

    public static class RotateInstruction extends ArmsLControlInstruction{
        public int x, y;

        public RotateInstruction(int x, int y){
            this.x = x;
            this.y = y;
        }

        @Override
        public void execute(LExecutor exec){
            if(!(exec.obj(picker) instanceof MechanicalArmsBuild pickerBuild)) return;

            float rx = exec.numi(x) * Vars.tilesize;
            float ry = exec.numi(y) * Vars.tilesize;

            command = pickerBuild.controller.rotateTo(rx, ry);
        }
    }

    public static class DumpInstruction extends ArmsLControlInstruction{
        public int picker;

        public DumpInstruction(int picker){
            this.picker = picker;
        }

        @Override
        public void execute(LExecutor exec){
            Object obj = exec.obj(picker);
            if(!(obj instanceof MechanicalArmsBuild pickerBuild)) return;

            ArmsController controller = pickerBuild.controller;
            if(!(controller.worker instanceof ArmPicker armPicker)) return;

            command = new DumpCommand(armPicker);
            controller.addCommand(command);
        }
    }
}
