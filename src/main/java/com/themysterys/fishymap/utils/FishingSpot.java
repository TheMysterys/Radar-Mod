class FishingSpot {
    private final String cords;
    private final List<String> perks;

    public FishingSpot(String cords, List<String> perks) {
        this.cords = cords;
        this.perks = perks;
    }

    public String getCords() {
        return cords;
    }

    public List<String> getPerks() {
        return perks;
    }

}