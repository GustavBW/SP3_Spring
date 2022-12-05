package gbw.sp3.OpcClient.client;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.Serializable;

@JsonSerialize
public class MachineStatus implements Serializable {

    public static long serialVersionUID = 293847298734L;

    private final int machineStatus;
    private final String translation;
    private final String errorMessage;
    private final boolean faulty;

    public MachineStatus(int machineStatus, String error)
    {
        this(machineStatus, ProductionState.from(machineStatus).name,error);
    }
    public MachineStatus(int machineStatus, String translation, String error)
    {
        this(machineStatus,translation,error,false);
    }
    public MachineStatus(int machineStatus, String translation, String error, boolean faulty)
    {
        this.machineStatus = machineStatus;
        this.translation = translation;
        this.errorMessage = error;
        this.faulty = faulty;
    }



    @JsonProperty
    public boolean isFaulty(){
        return faulty;
    }

    @JsonProperty
    public int getMachineStatus(){
        return machineStatus;
    }
    @JsonProperty
    public String getTranslation(){
        return translation;
    }
    @JsonProperty
    public String getErrorMessage(){
        return errorMessage;
    }

}

