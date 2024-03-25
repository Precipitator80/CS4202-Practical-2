/**
 * Class to store and get parts of a binary address.
 * Conversion: Hex String → Binary long.
 */
public class BinaryAddress {
    /**
     * Initialises a binary address by converting a hex string into a long.
     * @param hexString The hexadecimal string representation of a binary address.
     */
    public BinaryAddress(String hexString) {
        this(Long.parseLong(hexString, 16));
    }

    /**
     * Initialises a binary address with the a long representation of a binary address.
     * @param binaryLong The long representation of a binary address.
     */
    public BinaryAddress(long binaryLong) {
        this.address = binaryLong;
    }

    long address; // A long representing a binary address. Longs use 64-bits, which lets masking be used to check individual parts of the address.
}
