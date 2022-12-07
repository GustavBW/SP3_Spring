package gbw.sp3.OpcClient.client;

import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;

import java.util.*;

public enum KnownNodes {

//admin: {
    CurrentRecipe("CurrentRecipe","ns=6;s=::Program:Cube.Admin.Parameter[0].Value", NodeId.parse("ns=6;s=::Program:Cube.Admin.Parameter[0].Value")),
    ProductsProduced("ProductsProduced","ns=6;s=::Program:Cube.Admin.ProdProcessedCount", NodeId.parse("ns=6;s=::Program:Cube.Admin.ProdProcessedCount")),
    ProductsFailed("ProductsFailed","ns=6;s=::Program:Cube.Admin.ProdDefectiveCount", NodeId.parse("ns=6;s=::Program:Cube.Admin.ProdDefectiveCount")),
    StopReason("StopReason","ns=6;s=::Program:Cube.Admin.StopReason.ID", NodeId.parse("ns=6;s=::Program:Cube.Admin.StopReason.ID")),

//status({
    CurrentState("CurrentState","ns=6;s=::Program:Cube.Status.StateCurrent", NodeId.parse("ns=6;s=::Program:Cube.Status.StateCurrent")),
    CurrentProductionSpeed("CurrentProductionSpeed","ns=6;s=::Program:Cube.Status.CurMachSpeed", NodeId.parse("ns=6;s=::Program:Cube.Status.CurMachSpeed")),
    ProductionSpeed("ProductionSpeed","ns=6;s=::Program:Cube.Status.MachSpeed", NodeId.parse("ns=6;s=::Program:Cube.Status.MachSpeed")),
    BatchId("BatchId","ns=6;s=::Program:Cube.Status.Parameter[0].Value",NodeId.parse("ns=6;s=::Program:Cube.Status.Parameter[0].Value")),
    BatchQuantity("BatchQuantity","ns=6;s=::Program:Cube.Status.Parameter[1].Value", NodeId.parse("ns=6;s=::Program:Cube.Status.Parameter[1].Value")),
    BatchHumidity("BatchHumidity","ns=6;s=::Program:Cube.Status.Parameter[2].Value", NodeId.parse("ns=6;s=::Program:Cube.Status.Parameter[2].Value")),
    BatchTemperature("BatchTemperature","ns=6;s=::Program:Cube.Status.Parameter[3].Value", NodeId.parse("ns=6;s=::Program:Cube.Status.Parameter[3].Value")),
    Vibrations("Vibrations","ns=6;s=::Program:Cube.Status.Parameter[4].Value", NodeId.parse("ns=6;s=::Program:Cube.Status.Parameter[4].Value")),

//command({
    SetSpeed("SetSpeed","ns=6;s=::Program:Cube.Command.MachSpeed", NodeId.parse("ns=6;s=::Program:Cube.Command.MachSpeed")),
    SetCommand("SetCommand","ns=6;s=::Program:Cube.Command.CntrlCmd", NodeId.parse("ns=6;s=::Program:Cube.Command.CntrlCmd")),
    ExecuteCommands("ExecuteCommands","ns=6;s=::Program:Cube.Command.CmdChangeRequest", NodeId.parse("ns=6;s=::Program:Cube.Command.CmdChangeRequest")),
    SetBatchId("SetBatchId","ns=6;s=::Program:Cube.Command.Parameter[0].Value", NodeId.parse("ns=6;s=::Program:Cube.Command.Parameter[0].Value")),
    SetRecipe("SetRecipe","ns=6;s=::Program:Cube.Command.Parameter[1].Value", NodeId.parse("ns=6;s=::Program:Cube.Command.Parameter[1].Value")),
    SetQuantity("SetQuantity","ns=6;s=::Program:Cube.Command.Parameter[2].Value", NodeId.parse("ns=6;s=::Program:Cube.Command.Parameter[2].Value")),

//inventory({
    InventoryIsFilling("InventoryIsFilling","ns=6;s=::Program:FillingInventory", NodeId.parse("ns=6;s=::Program:FillingInventory")),
    Barley("Barley","ns=6;s=::Program:Inventory.Barley", NodeId.parse("ns=6;s=::Program:Inventory.Barley")),
    Hops("Hops","ns=6;s=::Program:Inventory.Hops", NodeId.parse("ns=6;s=::Program:Inventory.Hops")),
    Malt("Malt","ns=6;s=::Program:Inventory.Malt", NodeId.parse("ns=6;s=::Program:Inventory.Malt")),
    Wheat("Wheat","ns=6;s=::Program:Inventory.Wheat", NodeId.parse("ns=6;s=::Program:Inventory.Wheat")),
    Yeast("Yeast","ns=6;s=::Program:Inventory.Yeast", NodeId.parse("ns=6;s=::Program:Inventory.Yeast")),

//maintenance({
    MaintenanceCounter("MaintenanceCounter","ns=6;s=::Program:Maintenance.Counter", NodeId.parse("ns=6;s=::Program:Maintenance.Counter")),
    MaintenanceUrgent("MaintenanceUrgent","ns=6;s=::Program:Maintenance.Trigger", NodeId.parse("ns=6;s=::Program:Maintenance.Trigger"));

    private static String[] VALID_NAMES = null;

    public static String[] getValidNames(){
        if(VALID_NAMES == null){
            KnownNodes[] values = KnownNodes.values();
            VALID_NAMES = new String[values.length];
            for(int i = 0; i < VALID_NAMES.length; i++){
                VALID_NAMES[i] = values[i].displayName;
            }
        }
        return VALID_NAMES;
    }

    public final String id;
    public final NodeId nodeId;
    public final String displayName;
    KnownNodes(String displayName, String id,NodeId nodeId){
        this.id = id;
        this.nodeId = nodeId;
        this.displayName = displayName;
    }
    @Override
    public String toString()
    {
        return this.id;
    }

    public static KnownNodes parse(String name){
        for(KnownNodes node : KnownNodes.values()){
            if(node.displayName.equals(name)){
                return node;
            }
        }
        return null;
    }

    public static List<KnownNodes> parseList(String[] names){
        Set<String> namesToLookFor = new HashSet<>(Arrays.stream(names).toList());
        List<KnownNodes> parsed = new ArrayList<>();

        for(KnownNodes node : KnownNodes.values()){
            if(namesToLookFor.contains(node.displayName)){
                parsed.add(node);
            }
        }

        return parsed;
    }
}
