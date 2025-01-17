package aquaticmod.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.ui.panels.EnergyPanel;
import aquaticmod.fields.FrozenField;
import aquaticmod.actions.FreezeCardAction;

import java.util.ArrayList;

@SpirePatch(
        cls="com.megacrit.cardcrawl.characters.AbstractPlayer",
        method="useCard"
)
public class UseCardPatch
{
    public static SpireReturn<Void> Prefix(AbstractPlayer __instance, AbstractCard c, AbstractMonster monster, int energyOnUse)
    {
        FrozenField.FrozenDetails d = FrozenField.get(c);

        if ((monster == null)
        && (d.target == AbstractCard.CardTarget.ENEMY || d.target == AbstractCard.CardTarget.ALL_ENEMY)) {
            monster = AbstractDungeon.getMonsters().getRandomMonster(null, true, AbstractDungeon.cardRandomRng);
        }

        if (c.type == AbstractCard.CardType.ATTACK) {
            __instance.useFastAttackAnimation();
        }

        c.calculateCardDamage(monster);
        if (c.cost == -1 && EnergyPanel.totalCount < energyOnUse && !c.ignoreEnergyOnUse) {
            c.energyOnUse = EnergyPanel.totalCount;
        }

        if (c.cost == -1 && c.isInAutoplay) {
            c.freeToPlayOnce = true;
        }

        if(!d.frozen) {
            c.use(__instance, monster);
        }


        AbstractDungeon.actionManager.addToBottom(new UseCardAction(c, monster));
        if (!c.dontTriggerOnUseCard) {
            __instance.hand.triggerOnOtherCardPlayed(c);
        }

        __instance.hand.removeCard(c);
        __instance.cardInUse = c;

        c.target_x = Settings.WIDTH / 2;
        c.target_y = Settings.HEIGHT / 2;

        if (!(c.costForTurn <= 0 || c.freeToPlay() || c.isInAutoplay || __instance.hasPower("Corruption") && c.type == AbstractCard.CardType.SKILL)) {
            __instance.energy.use(c.costForTurn);
        }
        if (!__instance.hand.canUseAnyCard() && !__instance.endTurnQueued) {
            AbstractDungeon.overlayMenu.endTurnButton.isGlowing = true;
        }

        return SpireReturn.Return(null);
    }
}
