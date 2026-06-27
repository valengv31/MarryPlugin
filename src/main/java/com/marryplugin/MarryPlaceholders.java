package com.marryplugin;

import com.marryplugin.model.Marriage;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Expone el estado civil de los jugadores como placeholders de PlaceholderAPI,
 * para poder usarlos en scoreboards, hologramas, tab, etc.
 *
 * Placeholders disponibles:
 *   %marry_partner%  -> nombre de la pareja, o el texto configurado si está soltero/a
 *   %marry_status%   -> "Casado/a" o "Soltero/a" (textos configurables)
 *   %marry_since%    -> fecha de casamiento, o vacío si no está casado/a
 *
 * Esta clase solo se carga si PlaceholderAPI está instalado y habilitado en
 * el servidor (ver MarryPlugin#onEnable). Si no está, el plugin funciona
 * exactamente igual, simplemente sin estos placeholders disponibles.
 */
public class MarryPlaceholders extends PlaceholderExpansion {

    private final MarryPlugin plugin;

    public MarryPlaceholders(MarryPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        // Esto define el prefijo: %marry_<lo que sea>%
        return "marry";
    }

    @Override
    public String getAuthor() {
        return "MarryPlugin";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        // true = la expansión sigue registrada aunque se haga /papi reload
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) {
            return "";
        }

        Marriage marriage = plugin.getMarriageManager().getMarriage(player.getUniqueId());
        String notMarriedText = plugin.getConfig().getString("placeholders.not-married-text", "Soltero/a");

        switch (identifier.toLowerCase()) {
            case "partner":
                return marriage != null ? marriage.getPartnerName(player.getUniqueId()) : notMarriedText;

            case "status":
                if (marriage != null) {
                    return plugin.getConfig().getString("placeholders.married-text", "Casado/a");
                }
                return notMarriedText;

            case "since":
                if (marriage == null) return "";
                String format = plugin.getConfig().getString("placeholders.date-format", "dd/MM/yyyy");
                return new SimpleDateFormat(format).format(new Date(marriage.getMarriedAt()));

            default:
                return null;
        }
    }
}
