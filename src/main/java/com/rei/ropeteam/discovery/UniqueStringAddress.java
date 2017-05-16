package com.rei.ropeteam.discovery;

import java.io.DataInput;
import java.io.DataOutput;
import java.util.function.Supplier;

import org.jgroups.Address;
import org.jgroups.conf.ClassConfigurator;
import org.jgroups.stack.IpAddress;

public class UniqueStringAddress extends IpAddress {

    public static final short STRING_ADDRESS_ID = 2117;
    static {
        ClassConfigurator.add(STRING_ADDRESS_ID, UniqueStringAddress.class);
    }

    private String uniqueString;

    public UniqueStringAddress() {}

    public UniqueStringAddress(String uniqueString, IpAddress ipAddress) {
        super(ipAddress.getIpAddress(), ipAddress.getPort());
        this.uniqueString = uniqueString;
    }

    public String getUniqueString() {
        return uniqueString;
    }

    public void setUniqueString(String uniqueString) {
        this.uniqueString = uniqueString;
    }

    @Override
    public Supplier<? extends IpAddress> create() {
        return UniqueStringAddress::new;
    }

    @Override
    public int compareTo(Address o) {
        return uniqueString.compareTo(((UniqueStringAddress)o).uniqueString);
    }

    @Override
    public int serializedSize() {
        return super.serializedSize() + uniqueString.getBytes().length;
    }

    @Override
    public void writeTo(DataOutput out) throws Exception {
        super.writeTo(out);
        out.writeUTF(uniqueString);
    }

    @Override
    public void readFrom(DataInput in) throws Exception {
        super.readFrom(in);
        uniqueString = in.readUTF();
    }

    @Override
    public String toString() {
        return uniqueString;
    }
}
