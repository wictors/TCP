package kopr;

public class Priebeh {
    
private int priebeh = 0;

    public int dajPriebeh(){
        synchronized(this){
            return priebeh;
        }
    }
    public void zvysPriebeh(int prijal){
        synchronized(this){
            priebeh += prijal;
        }
    }
}
