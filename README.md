# Practice PvP - Nukkit MOT Plugin

A comprehensive PvP practice plugin for Nukkit MOT servers featuring duels, parties, events, divisions, and free-for-all combat.

### Duel System
- **1v1 Duels**: Challenge players to various PvP game modes
- **2v2 Duels**: Team-based combat with friends
- **Queue System**: Automatic matchmaking based on skill level
- **Spectator Mode**: Watch ongoing duels
- **Multiple Game Modes**: 14+ different PvP kits and modes

### Game Modes
- **NoDebuff**: Classic PvP with healing potions
- **UHC Variants**: FinalUHC, BuildUHC, CaveUHC
- **Sumo**: Knockback-based combat
- **Bridge**: Strategic block placement and PvP
- **BedFight**: Bed destruction combat
- **Boxing**: Hand-to-hand combat
- **Combo**: High-knockback PvP
- **And more**: Fireball, BattleRush, MidFight, HG, TNTSumo

### Party System
- **Create Parties**: Form groups with friends
- **Party Duels**: Challenge other parties
- **Party Events**: Internal party competitions
- **Premium Features**: Larger party sizes for VIP players

### Events System
- **Tournaments**: Bracket-style competitions
- **SkyWars**: Battle royale in the sky
- **Sumo Events**: Last player standing
- **Meetup**: Large-scale PvP battles
- **Host Events**: Staff can create custom events

### Division & Ranking
- **ELO System**: Skill-based ranking
- **5 Divisions**: Bronze, Gold, Platinum, Diamond, Sapphire
- **Subdivisions**: Multiple tiers within each division
- **Leaderboards**: Track top players
- **Statistics**: Detailed win/loss records

### Free-For-All (FFA)
- **Multiple FFA Arenas**: Different game modes
- **SkyWars FFA**: Aerial combat
- **Build FFA**: Combat with building mechanics
- **Continuous Action**: Jump in and fight anytime

### Customization
- **Kit Loadouts**: Customize your preferred setups
- **Cosmetics**: Premium visual enhancements
- **Settings**: Personalize your experience
- **Disguise System**: Staff moderation tools

## Configuration

### Database Setup
```yaml
database:
  host: "localhost:3306"
  username: "your_username"
  password: "your_password"
  schema: "practice_db"
```

### Division Configuration
```yaml
divisions:
  bronze:
    color: "&6"
    range-increase: 50
    subdivisions: 3
  # ... more divisions
```

### Hologram Setup
```yaml
holograms:
  lobby:
    enabled: true
    location: "world:0:100:0"
    text:
      - "&eWelcome to Practice"
      - "&7Fight and climb the ranks!"
```

## Commands

### Player Commands
- `/duel <player> [kit]` - Challenge a player to a duel
- `/spectate <player>` - Spectate an ongoing match
- `/stats [player]` - View player statistics
- `/hub` - Return to the lobby
- `/host` - Create an event (requires permission)
- `/ping` - Check your connection latency
- `/rekit` - Reset your kit loadout

### Staff Commands
- `/setup` - Configure plugin settings
- `/staff` - Enter staff mode
- `/hologram` - Manage holograms
- `/division` - See all divisions and their requirements
- `/globalmute` - Toggle server-wide mute
- `/disguise` - Change your appearance
- `/stop` - Stop ongoing matches

## Permissions

### Player Permissions
- `build.permission` - Build in lobby areas
- `chat.cooldown.permission` - Bypass chat cooldown
- `partypremium.permission` - Create larger parties
- `settings.permission` - Access premium settings
- `cosmetics.permission` - Use premium cosmetics

### Staff Permissions
- `setup.command` - Access setup commands
- `staffmode.permission` - Use staff mode
- `globalmute.command` - Control global mute
- `event.permission` - Create events
- `selector.permission` - Select maps for duels
- `disguise.command` - Use disguise system

## 📝 License

This project is licensed under the GNU General Public License v3.0 - see the [LICENSE](LICENSE) file for details.