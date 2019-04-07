// Tyokohdetta edustava luokka.
public class Tyokohde {
    private String osoite;
    private Asiakas omistaja;
    
    public Tyokohde(String hetu, String etunimi, String sukunimi, String asiakasOsoite, String tyokohdeOsoite) {
        this.omistaja = new Asiakas(hetu, etunimi, sukunimi, asiakasOsoite);
        osoite(tyokohdeOsoite);
    }

    public String osoite() { return osoite; }
    public void osoite(String o) {
        if (o.length() <= 32 && o != null)
            osoite = o;
    }
    public Asiakas omistaja() { return omistaja; }
}