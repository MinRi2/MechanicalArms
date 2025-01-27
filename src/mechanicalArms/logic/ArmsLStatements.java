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
    ControlSwitchStatement::new
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

        protected Table statementTable = new Table();

        public ControlSwitchStatement(){
            super("ArmsControl");
        }

        @Override
        public void build(Table table){
            table.defaults().padLeft(10f).left();

            table.table(top -> {
                top.setColor(category().color);

                fields(top, "Picker", picker, string -> picker = string).width(88f / Scl.scl());

                top.add("do");

                top.button(b -> {
                    b.label(() -> type.name());

                    b.clicked(() -> showSelect(b, ArmsLControlType.all, type, type -> {
                        this.type = type;
                        rebuildStatementTable();
                    }));
                }, Styles.logict, () -> {}).width(88f / Scl.scl()).color(category().color);
            });

            table.row();

            table.add(statementTable).colspan(table.getColumns()).color(category().color);

            rebuildStatementTable();
        }

        private void rebuildStatementTable(){
            statementTable.clearChildren();
            statementTable.defaults().padLeft(10f).left();
            getStatement(type).build(statementTable);
        }

        @Override
        public void write(ArmsLStatementWriter writer){
            writer.write(picker); // 1
            writer.write(type.name()); // 2
            ArmsLControlStatement statement = getStatement(type);
            statement.write(writer); // 3...
        }

        @Override
        public LStatement read(String[] tokens){
            ControlSwitchStatement statement = new ControlSwitchStatement();

            String picker = this.picker;
            ArmsLControlType type = this.type;

            if(tokens.length >= 2){
                picker = tokens[1];
            }

            if(tokens.length >= 3){
                type = ArmsLControlType.valueOf(tokens[2]);
            }

            ArmsLControlStatement selected = statement.getStatement(type);

            if(tokens.length >= 4){
                String[] paramsTokens = new String[tokens.length - 2];
                System.arraycopy(tokens, 3, paramsTokens, 0, paramsTokens.length - 1);

                selected.read(paramsTokens);
            }else{
                selected.read(null);
            }

            statement.type = type;
            statement.picker = picker;

            return statement;
        }

        @Override
        public LInstruction build(LAssembler builder){
            return getStatement(type).build(builder, builder.var(picker));
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
        @Deprecated
        public final LInstruction build(LAssembler builder){
            return null;
        }

        public abstract LInstruction build(LAssembler builder, LVar picker);

        @Override
        @Deprecated
        public final void write(StringBuilder builder){}

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
            }, Styles.logict, () -> {}).width(88f / Scl.scl()).color(category().color);

            extraTable.setColor(table.color);
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
                }).width(88f / Scl.scl()).padRight(0).color(category().color).get();

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
        public LInstruction build(LAssembler builder, LVar picker){
            return new PickupInstruction(picker, type, builder.var(item));
        }
    }

    public static class RotateStatement extends ArmsLControlStatement{
        public String x = "0", y = "0";

        public String finishedOut = "finished";

        @Override
        public void build(Table table){
            fields(table, "x", x, string -> x = string);
            fields(table, "y", y, string -> y = string);

            fields(table, "finished", finishedOut, string -> finishedOut = string).width(88f / Scl.scl());
        }

        @Override
        public LInstruction build(LAssembler builder, LVar picker){
            return new RotateInstruction(picker, builder.var(x), builder.var(y), builder.var(finishedOut));
        }

        @Override
        public void write(ArmsLStatementWriter writer){
            writer.write(x); // 0
            writer.write(y); // 1
            writer.write(finishedOut); // 2
        }

        @Override
        public void read(String[] paramTokens){
            int params = paramTokens.length - 1;
            if(params >= 0) x = paramTokens[0];
            if(params >= 1) y = paramTokens[1];
            if(params >= 2) finishedOut = paramTokens[2];
        }
    }

    public static class DumpStatement extends ArmsLControlStatement{

        @Override
        public LInstruction build(LAssembler builder, LVar picker){
            return new DumpInstruction(picker);
        }
    }
}
