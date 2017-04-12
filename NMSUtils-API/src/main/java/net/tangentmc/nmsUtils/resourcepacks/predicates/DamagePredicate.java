package net.tangentmc.nmsUtils.resourcepacks.predicates;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.tangentmc.nmsUtils.resourcepacks.predicates.Predicate;

@AllArgsConstructor
@Getter
@Setter
public class DamagePredicate implements Predicate {
    int damaged;
    double damage;
}
