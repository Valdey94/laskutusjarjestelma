// Laskua edustava luokka.
public class Lasku {
    private int lNro;
    private int monesko;
    private double verollinenSumma;
    private double verotonSumma;
    private String toimituspvm;
    private String maksupvm;
    private double viivastyskorko;
    private double ktvOsuus;
    private String erapvm;

    public Lasku() {
        verollinenSumma();
		verotonSumma();
		toimituspvm();
		maksupvm();
		viivastyskorko();
		ktvOsuus();
		monesko(1);
    }
    
    public int lNro() { return lNro; }
    
    public int monesko() {  return monesko;
    }
    public void monesko(int m) {
        if (m > 0)
            monesko = m;
    }
    
    public double verollinenSumma() { return verollinenSumma; }
    public void verollinenSumma(double v) {
        if (v > 0)
            verollinenSumma = v;
    }
    
    public double verotonSumma() { return verotonSumma; }
    public void verotonSumma(double v) {
        if (v > 0)
            verotonSumma = v;
    }
    
    public String toimituspvm() { return toimituspvm; }
    public void toimituspvm(String t) {
        if (t != null && t.length() > 0)
            toimituspvm = t;
    }
    
    public String maksupvm() { return maksupvm; }
    public void maksupvm(String m) {
        maksupvm = m;
    }
    
    public double viivastyskorko() { return viivastyskorko; }
    public void viivastyskorko(double v) {
        if (v > 0)
            viivastyskorko = v;
    }
    
    public double ktvOsuus() { return ktvOsuus; }
    public void ktvOsuus(double k) {
        if (k > 0)
            ktvOsuus = k;
    } 
 
    public String erapvm() { return erapvm; }
    public void erapvm(String e) {
        if (e != null && e.length() > 0)
            erapvm = e;
    }
}