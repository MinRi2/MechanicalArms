package mechanicalArms.logic;

import arc.func.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mechanicalArms.*;
import mechanicalArms.entity.arms.*;
import mechanicalArms.entity.arms.ArmsCommand.*;
import mechanicalArms.world.block.MechanicalArmsBlock.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.logic.*;
import mindustry.logic.LExecutor.*;
import mindustry.type.*;
import mindustry.ui.*;

import static mindustry.Vars.*;

/**
 * @author minri2
 * Create by 2025/1/25
 */
public class ArmsLStatements{
    private static final ArmsLStatementWriter writer = new ArmsLStatementWriter();

    @SuppressWarnings("unchecked")
    private static final Seq<Prov<ArmsLStatement>> armsAllStatements = Seq.with(
    PickupStatement::new
    );

    /** Register to LogicIO
     * this will be invoked on added to {@link LCanvas} as example
     * and make customized statement iterable by {@link LogicDialog}
     */
    public static void register(){
        Seq<Prov<LStatement>> seq = armsAllStatements.map(prov -> prov::get);
        LogicIO.allStatements.addAll(seq);

        // Customized LStatements will register customized reader automatically.
        for(Prov<ArmsLStatement> prov : armsAllStatements){
            ArmsLStatement example = prov.get();
            LAssembler.customParsers.put(example.name, example::read);
        }
    }

    private static abstract class ArmsLStatement extends LStatement{
        public final String name;

        public ArmsLStatement(String name){
            this.name = ArmsVars.logicArmsToken + "." + name;
        }

        public final void write(StringBuilder builder){
            writer.start(builder);

            writer.write(name);
            write(writer);

            writer.end();
        }

        // Customized LStatements must implement read/write by itself.
        public abstract void write(ArmsLStatementWriter writer);

        public abstract LStatement read(String[] tokens);
    }

    public static class PickupStatement extends ArmsLStatement{
        public String picker = "picker1";

        public ArmsPickupType type = ArmsPickupType.item;
        public String item = "@copper";

        public String finishedOut = "isFinished";

        private final Table extraTable = new Table();

        public PickupStatement(){
            super("pickup");
        }

        public PickupStatement set(String picker, ArmsPickupType type, String item, String finishedOut){
            this.picker = picker;
            this.type = type;
            this.item = item;
            this.finishedOut = finishedOut;
            return this;
        }

        @Override
        public LCategory category(){
            return ArmsLogic.armsCategory;
        }

        @Override
        public void build(Table table){
            table.defaults().padLeft(8f);
            fields(table, "Picker", picker, string -> picker = string);

            table.add("type").padLeft(16f);
            table.button(b -> {
                b.label(() -> type.name());

                b.clicked(() -> showSelect(b, ArmsPickupType.all, type, type -> {
                    this.type = type;
                    rebuildExtraTable();
                }));
            }, Styles.logict, () -> {}).size(40f).color(category().color);

            table.add(extraTable).padLeft(16f);

            fields(table, "finished", finishedOut,string -> {
                finishedOut = string;
            }).padRight(0).color(category().color).get();

            rebuildExtraTable();
        }

        private void rebuildExtraTable(){
            Table table = extraTable;

            table.defaults().padLeft(8f);
            table.clearChildren();

            if(type == ArmsPickupType.item){
                TextField field = fields(table, "item", item,string -> {
                    item = string;
                    rebuildExtraTable();
                }).padRight(0).color(category().color).get();

                table.button(b -> {
                    b.image(Icon.pencilSmall);

                    b.clicked(() -> showSelectTable(b, (t, hide) -> {
                        t.row();
                        t.table(i -> {
                            i.left();
                            int c = 0;
                            for(Item item : Vars.content.items()){
                                if(!item.unlockedNow()) continue;

                                i.button(new TextureRegionDrawable(item.uiIcon), Styles.flati, iconSmall, () -> {
                                    this.item = "@" + item.name;
                                    field.setText(this.item);
                                    hide.run();
                                }).size(40f);

                                if(++c % 6 == 0) i.row();
                            }
                        }).colspan(3).width(240f).left();
                    }));
                }, Styles.logict, () -> {}).size(40f).padLeft(-1).color(category().color);
            }
        }

        @Override
        public void write(ArmsLStatementWriter writer){
            writer.write(picker); // 1
            writer.write(type.name()); // 2
            writer.write(finishedOut); // 3
            if(type == ArmsPickupType.item){
                writer.write(item); // 4
            }
        }

        @Override
        public LStatement read(String[] tokens){
            picker = tokens[1];

            if(tokens.length >= 3){
                finishedOut = tokens[3];
            }

            if(tokens.length >= 4){
                type = Structs.find(ArmsPickupType.all, t -> t.name().equals(tokens[2]));

                // reset
                if(type == null){
                    type = ArmsPickupType.item;
                    item = "@copper";
                }else if(type == ArmsPickupType.item){
                    item = tokens[4];
                }
            }
            return new PickupStatement().set(picker, type, item, finishedOut);
        }

        @Override
        public LInstruction build(LAssembler builder){
            return new PickupInstruction(builder.var(picker), type, builder.var(item), builder.var(finishedOut));
        }

        private static class PickupInstruction implements LInstruction{
            public LVar picker;
            public ArmsPickupType type;
            public LVar p1;
            public LVar finishedOut;

            private ArmsCommand command;

            public PickupInstruction(LVar picker, ArmsPickupType type, LVar p1, LVar finishedOut){
                this.picker = picker;
                this.type = type;
                this.p1 = p1;
                this.finishedOut = finishedOut;
            }

            @Override
            public void run(LExecutor exec){
                if(command != null){
                    boolean finished = command.finished();
                    finishedOut.setbool(finished);

                    if(finished){
                        command = null;
                    }
                    return;
                }

                Building build = picker.building();
                if(!(build instanceof MechanicalArmsBuild pickerBuild)) return;

                ArmsController controller = pickerBuild.controller;
                if(!(controller.worker instanceof ArmPicker armPicker)) return;

                finishedOut.setbool(false);

                switch(type){
                    case item -> {
                        if(p1.obj() instanceof Item item){
                            ArmsCommand command = new PickerCommand(armPicker, type, item);
                            boolean added = controller.addCommand(command);
                            if(added){
                                this.command = command;
                                finishedOut.setbool(true);
                            }
                        }
                    }
                    case build, unit -> controller.addCommand(new PickerCommand(armPicker, type, null));
                }
            }
        }
    }
}
