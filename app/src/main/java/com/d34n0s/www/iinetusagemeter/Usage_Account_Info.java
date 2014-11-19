package com.d34n0s.www.iinetusagemeter;

/**
 * Created by dean on 18/11/2014.
 */
public class Usage_Account_Info {

    public String days_so_far;
    public String anniversary;
    public String days_remaining;
    public String ip;
    public String on_since;
    public String plan;
    public String product;


    public Double getPercentageDaysUsed(){
        if(days_so_far.matches("0") || days_remaining.matches("0")){
            return 0.00;
        }else{
            Double dsf = Double.parseDouble(days_so_far);
            Double dr = Double.parseDouble(days_remaining);
            Double totalDays = dsf+dr-1;
            Double result = dsf / totalDays * 100.0;
            return result;
        }

    }

}
