public class RussetPotato extends Potato {
    public RussetPotato(String potatoId, String name) {
        super(potatoId, name, "RUSSET");
    }

    @Override
    public int calculateScore(int payloadKg, int flightRating) {
        return 2 * payloadKg + flightRating;
    }
}