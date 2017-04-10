package net.tangentmc.nmsUtils.resourcepacks;

public class InvalidBlockException extends RuntimeException {
    public InvalidBlockException(String block) {
        super(String.format("The block with id \"%s\" does not exist!",block));
    }
}
