package bet.astral.fluffy.hooks.npc.sentinel;

import org.bukkit.entity.LivingEntity;
import org.mcmonkey.sentinel.SentinelIntegration;

public class SentinelFluffy extends SentinelIntegration {

    @Override
    public boolean isTarget(LivingEntity ent, String prefix, String value) {
        return super.isTarget(ent, prefix, value);
    }

    @Override
    public String[] getTargetPrefixes() {
        return new String[]{"fluffy"};
    }

    @Override
    public String getTargetHelp() {
        return "fluffy";
    }
}
