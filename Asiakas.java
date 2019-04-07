// Asiakasta edustava luokka.
public class Asiakas {
    private String hetu;
    private String etunimi;
    private String sukunimi;
    private String osoite;
    
    public Asiakas(String h, String e, String s, String o) {
        hetu(h);
        etunimi(e);
        sukunimi(s);
        osoite(o);
    }

    public String hetu() { return hetu; }
    public void hetu(String h) {
        if (h != null)
            hetu = h;
    }

    public String etunimi() { return etunimi; }
    public void etunimi(String e) {
        if (e.length() <= 32 && e != null)
            etunimi = e;
    }
    
    public String sukunimi() { return sukunimi; }
    public void sukunimi(String s) {
        if (s.length() <= 32 && s != null)
            sukunimi = s;
    }
    
    public String osoite() { return osoite; }
    public void osoite(String o) {
        if (o.length() <= 32 && o != null)
            osoite = o;
    }
}