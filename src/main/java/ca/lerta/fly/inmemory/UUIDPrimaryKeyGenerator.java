package ca.lerta.fly.inmemory;

import java.util.UUID;

public class UUIDPrimaryKeyGenerator implements PrimaryKeyGenerator<UUID> {

    @Override
    public UUID nextPrimaryKey(UUID previousMax) {  
        return UUID.randomUUID();
    }
    
}
