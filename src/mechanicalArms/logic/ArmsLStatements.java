package mechanicalArms.logic;

import arc.func.*;
import arc.scene.*;
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
    ControlSwitchStatement::new
    );

    /**
     * Register to LogicIO
     * this will be invoked on added to {@link LCanvas} as example
     * and make customized statement iterable by {@link LogicDialog}
     */
    public static void register(){
        Seq<Prov<LStatement>> seq = armsAllStatements.map(prov -> prov::get);
        LogicIO.allStatements.addAll(seq);

        // Customized LStatements will register customized reader automatically.
        for(Prov<ArmsLStatement> prov : armsAllStatements){
            ArmsLStatement example = prov.get();
            LAssembler.customParsers.put(example.markName, example::read);
        }
    }

    private static abstract class ArmsLStatement extends LStatement{
        public final String name;
        public final String markName;

        public ArmsLStatement(String name){
            this.name = name;
            this.markName = ArmsVars.logicArmsToken + "." + name;
        }

        @Override
        public LCategory category(){
            return ArmsLogic.armsCategory;
        }

        @Override
        public final void write(StringBuilder builder){
            writer.start(builder);

            writer.write(markName);
            write(writer);

            writer.end();
        }

        // Customized LStatements must implement read/write by itself.
        public abstract void write(ArmsLStatementWriter writer);

        public abstract LStatement read(String[] tokens);

        @Override
        public String name(){
            return name;
        }
    }

    public static class ControlSwitchStatement extends ArmsLStatement{
        private final ObjectMap<ArmsLControlType, ArmsLControlStatement> map = new ObjectMap<>();

        public String picker = "picker1";
        public ArmsLControlType type = ArmsLControlType.pick;

        public static final Boolean[] booleans = {true, false};

        // All the control instructions are logic async. Provide some ways to await it;
        public boolean wait = true;
        public String finishedOut = "finished";

        private final Table finishedTable = new Table();
        protected Table statementTable = new Table();

        public ControlSwitchStatement(){
            super("ArmsControl");
        }

        public ControlSwitchStatement set(String picker, ArmsLControlType type, boolean wait, String finishedOut){
            this.picker = picker;
            this.type = type;
            this.wait = wait;
            this.finishedOut = finishedOut;
            return this;
        }

        @Override
        public void build(Table table){
            table.defaults().padLeft(10f).left();

            table.table(top -> {
                top.setColor(category().color);

                fields(top, "Picker", picker, string -> picker = string).width(88f / Scl.scl());

                top.add("do");

                top.button(b -> {
                    b.label(() -> type.name);

                    b.clicked(() -> showSelect(b, ArmsLControlType.all.toArray(ArmsLControlType.class), type, type -> {
                        this.type = type;
                        rebuildStatementTable();
                    }));
                }, Styles.logict, () -> {
                }).width(88f / Scl.scl()).color(category().color);
            });

            table.add("wait");

            table.button(b -> {
                b.label(() -> wait + "");

                b.clicked(() -> showSelect(b, booleans, wait, wait -> {
                    this.wait = wait;
                    rebuildFinishedTable();
                }));
            }, Styles.logict, () -> {}).width(88f / Scl.scl()).color(category().color);

            finishedTable.setColor(category().color);
            table.add(finishedTable);

            table.row();

            table.add(statementTable).colspan(table.getColumns()).color(category().color);

            rebuildFinishedTable();
            rebuildStatementTable();
        }

        private void rebuildFinishedTable(){
            finishedTable.clearChildren();

            finishedTable.defaults().padLeft(10f);
            if(!wait){
                fields(finishedTable, "finished", finishedOut, string -> finishedOut = string).width(88f / Scl.scl());
            }
        }

        private void rebuildStatementTable(){
            statementTable.clearChildren();
            statementTable.defaults().padLeft(10f).left();
            getStatement(type).build(statementTable);
        }

        @Override
        public void write(ArmsLStatementWriter writer){
            writer.write(picker); // 1
            writer.write(type.name); // 2
            writer.write(wait); // 3
            writer.write(finishedOut); // 4
            ArmsLControlStatement statement = getStatement(type);
            statement.write(writer); // 5...
        }

        @Override
        public LStatement read(String[] tokens){
            ControlSwitchStatement statement = new ControlSwitchStatement();

            String picker = this.picker;
            ArmsLControlType type = this.type;
            boolean wait = this.wait;
            String finishedOut = this.finishedOut;

            int params = tokens.length - 1;
            if(params >= 1){
                picker = tokens[1];
            }

            if(params >= 2){
                type = ArmsLControlType.get(tokens[2]);
            }

            if(params >= 3){
                wait = Boolean.parseBoolean(tokens[3]);
            }

            if(params >= 4){
                finishedOut = tokens[4];
            }

            ArmsLControlStatement selected = statement.getStatement(type);

            if(params >= 5){
                String[] paramsTokens = new String[params - 4];
                System.arraycopy(tokens, 5, paramsTokens, 0, paramsTokens.length - 1);

                selected.read(paramsTokens);
            }else{
                selected.read(null);
            }

            return statement.set(picker, type, wait, finishedOut);
        }

        @Override
        public LInstruction build(LAssembler builder){
            int finishedOut = !wait ? builder.var(this.finishedOut) : -1;
            return getStatement(type).build(builder).set(builder.var(picker), wait, finishedOut);
        }

        private ArmsLControlStatement getStatement(ArmsLControlType type){
            return map.get(type, type::getStatement);
        }
    }

    public abstract static class ArmsLControlStatement extends LStatement{

        @Override
        public LCategory category(){
            return ArmsLogic.armsCategory;
        }

        @Override
        public void build(Table table){
        }

        @Override
        public abstract ArmsLControlInstruction build(LAssembler builder);

        @Override
        @Deprecated
        public final void write(StringBuilder builder){
        }

        // Customized LStatements must implement read/write by itself.
        public void write(ArmsLStatementWriter writer){
        }

        public void read(String[] paramTokens){
        }
    }

    public static class PickupStatement extends ArmsLControlStatement{
        public ArmsPickupType type = ArmsPickupType.item;
        public String item = "@copper";

        private final Table extraTable = new Table();

        @Override
        public void build(Table table){
            table.add("type");
            table.button(b -> {
                b.label(() -> type.name());

                b.clicked(() -> showSelect(b, ArmsPickupType.all, type, type -> {
                    this.type = type;
                    rebuildExtraTable();
                }));
            }, Styles.logict, () -> {
            }).width(88f / Scl.scl()).color(category().color);

            super.build(table);

            extraTable.setColor(table.color);
            table.add(extraTable);

            rebuildExtraTable();
        }

        private void rebuildExtraTable(){
            Table table = extraTable;

            table.defaults().padLeft(10f);
            table.clearChildren();

            if(type == ArmsPickupType.item){
                TextField field = fields(table, "item", item, string -> {
                    item = string;
                    rebuildExtraTable();
                }).width(88f / Scl.scl()).padRight(0).color(category().color).get();

                table.button(b -> {
                    b.image(Icon.pencilSmall);

                    b.clicked(() -> showSelectTable(b, (t, hide) -> {
                        t.row();
                        t.table(i -> {
                            i.left();
                            int c = 0;
                            for(Item item : content.items()){
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
                }, Styles.logict, () -> {
                }).size(40f).padLeft(-1).color(category().color);
            }
        }

        @Override
        public void write(ArmsLStatementWriter writer){
            writer.write(type.name()); // 0
            if(type == ArmsPickupType.item){
                writer.write(item); // 1
            }
        }

        @Override
        public void read(String[] paramTokens){
            int params = paramTokens.length - 1;
            if(params >= 0){
                type = Structs.find(ArmsPickupType.all, t -> t.name().equals(paramTokens[0]));

                // reset
                if(type == null){
                    type = ArmsPickupType.item;
                    item = "@copper";
                }else if(type == ArmsPickupType.item){
                    item = paramTokens[1];
                }
            }
        }

        @Override
        public ArmsLControlInstruction build(LAssembler builder){
            return new PickupInstruction(type, builder.var(item));
        }
    }

    public static class RotateStatement extends ArmsLControlStatement{
        public String x = "0", y = "0";

        @Override
        public void build(Table table){
            fields(table, "x", x, string -> x = string);
            fields(table, "y", y, string -> y = string);

            super.build(table);
        }

        @Override
        public ArmsLControlInstruction build(LAssembler builder){
            return new RotateInstruction(builder.var(x), builder.var(y));
        }

        @Override
        public void write(ArmsLStatementWriter writer){
            writer.write(x); // 0
            writer.write(y); // 1
        }

        @Override
        public void read(String[] paramTokens){
            int params = paramTokens.length - 1;
            if(params >= 0) x = paramTokens[0];
            if(params >= 1) y = paramTokens[1];
        }
    }

    public static class DumpStatement extends ArmsLControlStatement{

        @Override
        public ArmsLControlInstruction build(LAssembler builder){
            return new DumpInstruction();
        }
    }
}
