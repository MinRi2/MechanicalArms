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

    public static abstract class ArmsLAsyncInstruction implements LInstruction{
        protected ArmsCommand command;
        public int finishedOut;

        public ArmsLAsyncInstruction(int finishedOut){
            this.finishedOut = finishedOut;
        }

        @Override
        public final void run(LExecutor exec){
            if(command != null){
                boolean finished = command.released; // wait for released
                exec.setbool(finishedOut, finished);

                if(finished){
                    command = null;
                }
                return;
            }

            runLogicAsync(exec);

            if(command == null){
                exec.setbool(finishedOut, true);
            }
        }

        public abstract void runLogicAsync(LExecutor exec);
    }

    public static class PickupInstruction implements LInstruction{
        public int picker;
        public ArmsPickupType type;
        public int p1;

        protected ArmsCommand command;

        public PickupInstruction(int picker, ArmsPickupType type, int p1){
            this.picker = picker;
            this.type = type;
            this.p1 = p1;
        }

        @Override
        public void run(LExecutor exec){
            if(command != null){
                if(!command.released) return;
                command = null;
            }

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

    public static class RotateInstruction extends ArmsLAsyncInstruction{
        public int picker;
        public int x, y;

        public RotateInstruction(int finishedOut, int picker, int x, int y){
            super(finishedOut);

            this.picker = picker;
            this.x = x;
            this.y = y;
        }

        @Override
        public void runLogicAsync(LExecutor exec){
            if(!(exec.obj(picker) instanceof MechanicalArmsBuild pickerBuild)) return;

            float rx = exec.numi(x) * Vars.tilesize;
            float ry = exec.numi(y) * Vars.tilesize;

            command = pickerBuild.controller.rotateTo(rx, ry);
        }
    }

    public static class DumpInstruction implements LInstruction{
        public int picker;

        protected ArmsCommand command;

        public DumpInstruction(int picker){
            this.picker = picker;
        }

        @Override
        public void run(LExecutor exec){
            if(command != null){
                if(!command.released) return;
                command = null;
            }

            Object obj = exec.obj(picker);
            if(!(obj instanceof MechanicalArmsBuild pickerBuild)) return;

            ArmsController controller = pickerBuild.controller;
            if(!(controller.worker instanceof ArmPicker armPicker)) return;

            command = new DumpCommand(armPicker);
            controller.addCommand(command);
        }
    }
}
