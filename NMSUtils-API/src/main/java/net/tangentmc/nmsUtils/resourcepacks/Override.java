package net.tangentmc.nmsUtils.resourcepacks;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class Override {
    private Predicate predicate;
    private String model;
}