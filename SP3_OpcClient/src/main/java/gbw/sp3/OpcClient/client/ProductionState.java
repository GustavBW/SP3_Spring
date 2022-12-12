package gbw.sp3.OpcClient.client;

public enum ProductionState {

    DEACTIVATED(0,"deactivating"),
    CLEARING(1,"clearing"),
    STOPPED(2,"stopped"),
    STARTING(3,"starting"),
    IDLE(4,"idle"),
    SUSPENDED(5,"suspended"),
    EXECUTE(6,"execute"),
    STOPPING(7,"stopping"),
    ABORTING(8,"aborting"),
    ABORTED(9,"aborted"),
    HOLDING(10,"holding"),
    HELD(11,"held"),
    RESETTING(15,"resetting"),
    COMPLETING(16,"completing"),
    COMPLETE(17,"complete"),
    DEACTIVATING(18,"deactivating"),
    ACTIVATING(19,"activating"),
    INVALID_STATE(20,"invalid_state");
    
    public final int value;
    public final String name;
    ProductionState(int value, String name)
    {
        this.name = name;
        this.value = value;
    }
    
    public static ProductionState from(int i){
        if(i < 0){
            return ProductionState.INVALID_STATE;
        }
        ProductionState[] values = ProductionState.values();

        if(i >= values.length){
            return ProductionState.INVALID_STATE;
        }
        return values[i];
    }
}
