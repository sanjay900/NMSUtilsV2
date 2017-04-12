package net.tangentmc.nmsUtils.resourcepacks.predicates;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BowDamagePredicate extends DamagePredicate {
    public BowDamagePredicate(int damaged, double damage, int pulling, double pull) {
        super(damaged, damage);
        this.pull = pull;
        this.pulling = pulling;
    }
    double pull;
    int pulling;
}
