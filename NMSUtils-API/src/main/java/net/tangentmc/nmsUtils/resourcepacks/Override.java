package net.tangentmc.nmsUtils.resourcepacks;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.tangentmc.nmsUtils.resourcepacks.predicates.Predicate;

@AllArgsConstructor
@Getter
@Setter
public class Override {
    private Predicate predicate;
    private String model;
}
