package gbw.sp3.OpcClient.client;

public enum KnownNodes {

    //admin: {
        CurrentRecipe("ns=6;s=::Program:Cube.Admin.Parameter[0].Value"),
        ProductsProduced("ns=6;s=::Program:Cube.Admin.ProdProcessedCount"),
        ProductsFailed("ns=6;s=::Program:Cube.Admin.ProdDefectiveCount"),
        StopReason("ns=6;s=::Program:Cube.Admin.StopReason.ID"),

    //status({
        CurrentState("ns=6;s=::Program:Cube.Status.StateCurrent"),
        CurrentProductionSpeed("ns=6;s=::Program:Cube.Status.CurMachSpeed"),
        ProductionSpeed("ns=6;s=::Program:Cube.Status.MachSpeed"),
        BatchId("ns=6;s=::Program:Cube.Status.Parameter[0].Value"),
        BatchQuantity("ns=6;s=::Program:Cube.Status.Parameter[1].Value"),
        BatchHumidity("ns=6;s=::Program:Cube.Status.Parameter[2].Value"),
        BatchTemperature("ns=6;s=::Program:Cube.Status.Parameter[3].Value"),
        Vibrations("ns=6;s=::Program:Cube.Status.Parameter[4].Value"),

    //command({
        SetSpeed("ns=6;s=::Program:Cube.Command.MachSpeed"),
        SetCommand("ns=6;s=::Program:Cube.Command.CntrlCmd"),
        ExecuteCommands("ns=6;s=::Program:Cube.Command.CmdChangeRequest"),
        SetBatchId("ns=6;s=::Program:Cube.Command.Parameter[0].Value"),
        SetRecipe("ns=6;s=::Program:Cube.Command.Parameter[1].Value"),
        SetQuantity("ns=6;s=::Program:Cube.Command.Parameter[2].Value"),

    //inventory({
        InventoryIsFilling("ns=6;s=::Program:FillingInventory"),
        Barley("ns=6;s=::Program:Inventory.Barley"),
        Hops("ns=6;s=::Program:Inventory.Hops"),
        Malt("ns=6;s=::Program:Inventory.Malt"),
        Wheat("ns=6;s=::Program:Inventory.Wheat"),
        Yeast("ns=6;s=::Program:Inventory.Yeast"),

    //maintenance({
        MaintenanceCounter("ns=6;s=::Program:Maintenance.Counter"),
        MaintenanceUrgent("ns=6;s=::Program:Maintenance.Trigger");


    final String id;
    KnownNodes(String id){
        this.id = id;
    }
    @Override
    public String toString()
    {
        return this.id;
    }
}
