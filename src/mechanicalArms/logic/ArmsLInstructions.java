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
        public LVar finishedOut;

        public ArmsLAsyncInstruction(LVar finishedOut){
            this.finishedOut = finishedOut;
        }

        @Override
        public final void run(LExecutor exec){
            if(command != null){
                boolean finished = command.released; // wait for released
                finishedOut.setbool(finished);

                if(finished){
                    command = null;
                }
                return;
            }

            runLogicAsync(exec);

            if(command == null){
                finishedOut.setbool(true);
            }
        }

        public abstract void runLogicAsync(LExecutor exec);
    }

    public static class PickupInstruction implements LInstruction{
        public LVar picker;
        public ArmsPickupType type;
        public LVar p1;

        protected ArmsCommand command;

        public PickupInstruction(LVar picker, ArmsPickupType type, LVar p1){
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

    public static class RotateInstruction extends ArmsLAsyncInstruction{
        public LVar picker;
        public LVar x, y;

        public RotateInstruction(LVar finishedOut, LVar picker, LVar x, LVar y){
            super(finishedOut);

            this.picker = picker;
            this.x = x;
            this.y = y;
        }

        @Override
        public void runLogicAsync(LExecutor exec){
            if(!(picker.obj() instanceof MechanicalArmsBuild pickerBuild)) return;

            float rx = x.numi() * Vars.tilesize;
            float ry = y.numi() * Vars.tilesize;

            command = pickerBuild.controller.rotateTo(rx, ry);
        }
    }

    public static class DumpInstruction implements LInstruction{
        public LVar picker;

        protected ArmsCommand command;

        public DumpInstruction(LVar picker){
            this.picker = picker;
        }

        @Override
        public void run(LExecutor exec){
            if(command != null){
                if(!command.released) return;
                command = null;
            }

            Object obj = picker.obj();
            if(!(obj instanceof MechanicalArmsBuild pickerBuild)) return;

            ArmsController controller = pickerBuild.controller;
            if(!(controller.worker instanceof ArmPicker armPicker)) return;

            command = new DumpCommand(armPicker);
            controller.addCommand(command);
        }
    }
}
