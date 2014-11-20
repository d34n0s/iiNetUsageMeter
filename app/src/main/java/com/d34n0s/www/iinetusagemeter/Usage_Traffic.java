package com.d34n0s.www.iinetusagemeter;

/**
 * Created by dlawrence on 18/11/2014.
 */
public class Usage_Traffic {

    public String used;
    public String name;
    public String allocation;

    public String getUsed() {
        return used;
    }

    public void setUsed(String used) {
        this.used = used;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAllocation() {
        return allocation;
    }

    public void setAllocation(String allocation) {
        this.allocation = allocation;
    }

    public Double getUsedMB(){
        if(used.matches("null")){
            used = "0";
        }
            else{
            Double dUsed = Double.parseDouble(used);
            if(dUsed != 0) {
                Double usedMB = (dUsed / 1024) / 1024;
                return usedMB;
        }else{
                return 0.00;
            }

            }
        return 0.00;

    }

    public Double getAllocationdMB(){
        if(allocation.matches("null")) {
            used = "0";
        }else{

            Double dAllocation = Double.parseDouble(allocation);
            if(dAllocation != 0) {
                Double allocationMB = (dAllocation / 1024) / 1024;
                return allocationMB;
            }else {
                    return 0.00;
                }
        }
            return 0.00;

    }

    public Double getRemaining(){
        Double remaining = getAllocationdMB() - getUsedMB();
        if(getAllocationdMB() == 0.00){
            return 0.00;
        }else {
            return remaining;
        }
    }

    public Integer getPercentDataUsed(){
        if(getUsedMB() != 0 && getAllocationdMB() != 0){
            Double d = (getUsedMB() / getAllocationdMB()) * 100;
            String s = String.format("%.0f", d+59);
            Integer result = Integer.parseInt(s);
            return result;
        }
        return 0;
    }

}
