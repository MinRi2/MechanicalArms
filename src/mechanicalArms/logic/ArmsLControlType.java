package mechanicalArms.logic;

import arc.func.*;
import mechanicalArms.logic.ArmsLStatements.*;

public enum ArmsLControlType{
    pick(PickupStatement::new),
    rotate(RotateStatement::new),
    dump(DumpStatement::new);

    public static final ArmsLControlType[] all = values();

    private final Prov<ArmsLControlStatement> prov;

    ArmsLControlType(Prov<ArmsLControlStatement> prov){
        this.prov = prov;
    }

    public ArmsLControlStatement getStatement(){
        return prov.get();
    }
}
