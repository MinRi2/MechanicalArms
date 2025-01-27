package mechanicalArms.logic;

import arc.func.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mechanicalArms.*;
import mechanicalArms.logic.ArmsLInstructions.*;
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
    PickupStatement::new,
    RotateStatement::new,
    DumpStatement::new
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

        @Override
        public LCategory category(){
            return ArmsLogic.armsCategory;
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

        private final Table extraTable = new Table();

        public PickupStatement(){
            super("pickup");
        }

        @Override
        public void build(Table table){
            table.defaults().padLeft(10f);

            fields(table, "Picker", picker, string -> picker = string);

            table.add("type");
            table.button(b -> {
                b.label(() -> type.name());

                b.clicked(() -> showSelect(b, ArmsPickupType.all, type, type -> {
                    this.type = type;
                    rebuildExtraTable();
                }));
            }, Styles.logict, () -> {}).size(40f).color(category().color);

            table.add(extraTable);

            rebuildExtraTable();
        }

        private void rebuildExtraTable(){
            Table table = extraTable;

            table.defaults().padLeft(10f);
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
            if(type == ArmsPickupType.item){
                writer.write(item); // 3
            }
        }

        @Override
        public LStatement read(String[] tokens){
            PickupStatement statement = new PickupStatement();

            String picker = this.picker;
            ArmsPickupType type = this.type;
            String item = this.item;

            int params = tokens.length - 1;
            if(params >= 1) picker = tokens[1];
            if(params >= 2){
                type = Structs.find(ArmsPickupType.all, t -> t.name().equals(tokens[2]));

                // reset
                if(type == null){
                    type = ArmsPickupType.item;
                    item = "@copper";
                }else if(type == ArmsPickupType.item){
                    item = tokens[3];
                }
            }

            statement.picker = picker;
            statement.type = type;
            statement.item = item;

            return statement;
        }

        @Override
        public LInstruction build(LAssembler builder){
            return new PickupInstruction(builder.var(picker), type, builder.var(item));
        }
    }

    public static class RotateStatement extends ArmsLStatement{
        public String picker = "picker1";
        public String x = "0", y = "0";

        public String finishedOut = "finished";

        public RotateStatement(){
            super("Rotate");
        }

        @Override
        public void build(Table table){
            table.defaults().padLeft(10f);
            fields(table, "Picker", picker, string -> picker = string);

            fields(table, "x", x, string -> x = string);
            fields(table, "y", y, string -> y = string);

            fields(table, "finished", finishedOut, string -> finishedOut = string);
        }

        @Override
        public LInstruction build(LAssembler builder){
            return new RotateInstruction(builder.var(finishedOut), builder.var(picker), builder.var(x), builder.var(y));
        }

        @Override
        public void write(ArmsLStatementWriter writer){
            writer.write(finishedOut); // 1
            writer.write(picker); // 2
            writer.write(x); // 3
            writer.write(y); // 4
        }

        @Override
        public LStatement read(String[] tokens){
            RotateStatement statement = new RotateStatement();

            String picker = this.picker;
            String x = this.x, y = this.y;
            String finishedOut = this.finishedOut;

            int params = tokens.length - 1;
            if(params >= 1) finishedOut = tokens[1];
            if(params >= 2) picker = tokens[2];
            if(params >= 3) x = tokens[3];
            if(params >= 4) y = tokens[4];

            statement.finishedOut = finishedOut;
            statement.picker = picker;
            statement.x = x;
            statement.y = y;

            return statement;
        }
    }

    public static class DumpStatement extends ArmsLStatement{
        public String picker = "picker1";

        public DumpStatement(){
            super("Dump");
        }

        @Override
        public void build(Table table){
            table.defaults().padLeft(10f);
            fields(table, "Picker", picker, string -> picker = string);
        }

        @Override
        public LInstruction build(LAssembler builder){
            return new DumpInstruction(builder.var(picker));
        }

        @Override
        public void write(ArmsLStatementWriter writer){
            writer.write(picker); // 1
        }

        @Override
        public LStatement read(String[] tokens){
            DumpStatement statement = new DumpStatement();

            String picker = this.picker;

            int params = tokens.length - 1;
            if(params >= 1) picker = tokens[1];

            statement.picker = picker;

            return statement;
        }
    }
}
