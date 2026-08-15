package io.lumine.mythic.lib.skill.handler.def.target;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.BuiltinSkillHandler;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.TargetSkillResult;
import io.lumine.mythic.lib.util.TemporaryHandler;
import io.lumine.mythic.lib.version.Sounds;
import io.lumine.mythic.lib.version.VParticle;
import io.lumine.mythic.lib.version.VPotionEffectType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import cn.yvmou.ylib.scheduler.UniversalRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@BuiltinSkillHandler(mods = {"damage", "duration", "amplifier"})
public class Death_Mark extends SkillHandler<TargetSkillResult> {
    private final List<DamageType> damageTypes;

    public Death_Mark(ConfigurationSection config) {
        super(config);

        damageTypes = DamageType.listFromConfig(List.of(DamageType.MAGIC, DamageType.SKILL), config.get("damage_types"));
    }

    @Override
    public @NotNull TargetSkillResult getResult(SkillMetadata meta) {
        return new TargetSkillResult(meta);
    }

    @Override
    public void whenCast(TargetSkillResult result, SkillMetadata skillMeta) {
        LivingEntity target = result.getTarget();

        double duration = skillMeta.getParameter("duration") * 20;
        double dps = skillMeta.getParameter("damage") / duration * 20;

        TemporaryHandler.task(skillMeta.getCaster().getData(), target, 0, 1, handler -> new UniversalRunnable() {
            int ti = 0;

            public void run() {
                if (++ti > duration || target.isDead()) {
                    handler.close();
                    return;
                }

                VParticle.INSTANT_EFFECT.spawnSafeSpell(target.getLocation(), 4, .2, 0, .2, 0);

                if (ti % 20 == 0)
                    skillMeta.getCaster().attack(target, dps, false, damageTypes);
            }
        });

        target.getWorld().playSound(target.getLocation(), Sounds.ENTITY_BLAZE_HURT, 1, 2);
        MythicLib.applyOn(target, () -> {
            target.removePotionEffect(VPotionEffectType.SLOWNESS.get());
            target.addPotionEffect(new PotionEffect(VPotionEffectType.SLOWNESS.get(), (int) duration, (int) skillMeta.getParameter("amplifier")));
        });
    }
}
