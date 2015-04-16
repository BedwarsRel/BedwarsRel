package io.github.yannici.bedwars.Game;

public enum GameLobbyCountdownRule {
    TEAMS_HAVE_PLAYERS(0),
    PLAYERS_IN_GAME(1);
    
    private int type = 0;
    
    GameLobbyCountdownRule(int type) {
        this.type = type;
    }
    
    public int getTypeId() {
        return this.type;
    }
    
    public boolean isRuleMet(Game game) {
        switch(this) {
            case TEAMS_HAVE_PLAYERS:
                for(Team team : game.getTeams().values()) {
                    if(team.getPlayers().size() == 0) {
                        return false;
                    }
                }
                break;
            case PLAYERS_IN_GAME:
                if(game.getMinPlayers() > game.getPlayers().size()) {
                    return false;
                }
                break;
        }
        
        return true;
    }

    public static GameLobbyCountdownRule getById(int id) {
        for(GameLobbyCountdownRule rule : GameLobbyCountdownRule.values()) {
            if(rule.getTypeId() == id) {
                return rule;
            }
        }
        
        return GameLobbyCountdownRule.TEAMS_HAVE_PLAYERS;
    }
} 
