package mechanicalArms.logic;

import arc.func.*;
import arc.struct.*;
import mechanicalArms.logic.ArmsLStatements.*;

public class ArmsLControlType{
    public static final Seq<ArmsLControlType> all = new Seq<>();

    public static final ArmsLControlType
    pick = new ArmsLControlType("pick", PickupStatement::new),
    rotate = new ArmsLControlType("rotate", RotateStatement::new),
    dump = new ArmsLControlType("dump", DumpStatement::new);

    public final String name;
    private final Prov<ArmsLControlStatement> prov;

    public ArmsLControlType(String name, Prov<ArmsLControlStatement> prov){
        this.name = name;
        this.prov = prov;

        all.add(this);
    }

    public static ArmsLControlType get(String name){
        return all.find(type -> type.name.equals(name));
    }

    public ArmsLControlStatement getStatement(){
        return prov.get();
    }
}
