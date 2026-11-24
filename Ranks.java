package com.plusultra.mission;

/** Hero ranks, unlocked by hero points earned from missions. */
public final class Ranks {
    public record Rank(String name, int minPoints) {}

    public static final Rank[] ALL = {
            new Rank("Trainee", 0),
            new Rank("Rookie Hero", 50),
            new Rank("Pro Hero", 150),
            new Rank("Top 10 Hero", 400),
            new Rank("Number One Hero", 800)
    };

    private Ranks() {}

    public static Rank of(int points) {
        Rank best = ALL[0];
        for (Rank r : ALL) if (points >= r.minPoints()) best = r;
        return best;
    }

    public static Rank next(int points) {
        for (Rank r : ALL) if (points < r.minPoints()) return r;
        return null;
    }
}
