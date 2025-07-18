package alexis.practice.event.games.types.tournament;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TournamentState {
    GROUP_STAGE("Group Stage"),
    ROUND_OF_16("Round of 16"),
    QUARTER_FINALS("Quarter Finals"),
    SEMI_FINALS("Semi Finals"),
    FINALS("Finals");

    private final String name;
}
