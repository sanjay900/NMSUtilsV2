package net.tangentmc.nmsUtils.resourcepacks.predicates;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class BowPullPullingPredicate implements Predicate {
    double pull;
    int pulling;
}
