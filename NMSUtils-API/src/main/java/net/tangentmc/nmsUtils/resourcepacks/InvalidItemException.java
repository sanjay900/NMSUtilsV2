package net.tangentmc.nmsUtils.resourcepacks;

public class InvalidItemException extends RuntimeException {
    public InvalidItemException(String item) {
        super(String.format("The item with id \"%s\" does not exist!",item));
    }
}
