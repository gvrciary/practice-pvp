package alexis.practice.profile.settings.types;

import alexis.practice.profile.Profile;
import alexis.practice.profile.settings.Setting;
import alexis.practice.util.Utils;
import cn.nukkit.Player;
import cn.nukkit.entity.data.Skin;
import lombok.Getter;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class Disguise extends Setting {

    @Getter
    private static final String[] randomNames = {
            "Trapzies", "ghxsty", "LuckyXTapz", "obeseGamerGirl", "UnknownXzzz", "zAnthonyyy",
            "FannityPE", "Vatitelc", "StudSport", "MCCaffier", "Keepuphulk8181", "LittleComfy",
            "Decdarle", "mythic_d4nger", "gambling life", "BASIC x VIBES", "lawlogic", "hutteric",
            "BiggerCobra_1181", "Lextech817717", "Chnixxor", "AloneShun", "AddictedToYou", "Board",
            "Javail", "MusicPqt", "REYESOOKIE", "Asaurus Rex", "Popperrr", "oopsimSorry_",
            "lessthan greaterthan", "Regrexxx", "adam 22", "NotCqnadian", "brtineyMCPE", "samanthaplayzmc",
            "ShaniquaLOL", "OptimusPrimeXD", "BouttaBust", "GamingNut66", "NoIdkbruh",
            "ThisIsWhyYoure___", "voLT_811", "Sekrum", "Artificial_", "ReadMyBook", "urmum__77",
            "idkwhatiatetoday", "udkA77161", "Stimpy", "Adviser", "St1pmyPVP", "GangGangGg",
            "CoolKid888", "AcornChaser78109", "anon171717", "AnonymousYT", "Sintress Balline",
            "Daviecrusha", "HeatedBot46", "CobraKiller2828", "KingPVPYT", "TempestG", "ThePVPGod",
            "McProGangYT", "lmaonocap", "NoClipXD", "ImHqcking", "undercoverbot", "reswoownss199q",
            "diego91881", "CindyPlayz", "HeyItzMe", "iTzSkittlesMC", "NOHACKJUSTPRO", "idkHowToPlay",
            "Bum Bummm", "Bigumslol", "Skilumsszz", "SuperGamer756", "ProPVPer2k20", "N0S3_P1CK3R84",
            "PhoenixXD", "Ft MePro", "NotHaqing", "aababah_a", "badbtch4life", "serumxxx",
            "bigdogoo_", "william18187", "ZeroLxck", "Gamer dan", "SuperSAIN", "DefNoHax", "GoldFox",
            "ClxpKxng", "AdamIsPro", "XXXPRO655", "proshtGGxD", "T0PL543", "GamerKid9000",
            "SphericalAxeum", "ImABot"
    };

    private final Map<String, Skin> skin = new HashMap<>();

    public Disguise(String name, boolean enabled) {
        super(name, enabled);
    }

    public Disguise(String name) {
        super(name);
    }

    @Override
    public void execute(Profile profile, boolean value) {
        if (!profile.isOnline()) return;

        try {
            Player player = profile.getPlayer();

            if (value) {
                skin.put(profile.getIdentifier(), player.getSkin());

                Skin skin = new Skin();
                skin.setSkinId(profile.getPlayer().getSkin().getSkinId());
                skin.setSkinData(Base64.getDecoder().decode(Skin.STEVE_SKIN));
                player.setSkin(skin);

                profile.setName(randomNames[Utils.randomInteger(0, randomNames.length - 1)]);
                return;
            }

            if (skin.containsKey(profile.getIdentifier())) player.setSkin(skin.get(profile.getIdentifier()));

            profile.setName(profile.getName());
        } catch (Exception ignored) {}
    }

    @Override
    public void clearCache(Profile profile) {
        skin.remove(profile.getIdentifier());
    }

}
