package raid24contribution.sc_model;

public enum SCREFERENCETYPE {
    
    BYVALUE(""), BYREFERENCE("&");
    
    private final String symbol;
    
    private SCREFERENCETYPE(String symbol) {
        this.symbol = symbol;
    }
    
    public String getSymbol() {
        return this.symbol;
    }
}
